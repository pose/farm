package org.mule.farm.main;

import static org.mule.farm.util.Util.copy;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.repository.internal.DefaultServiceLocator;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.providers.http.HttpWagon;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.installer.Installer;
import org.codehaus.cargo.container.installer.ZipURLInstaller;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.util.log.LogLevel;
import org.codehaus.cargo.util.log.Logger;
import org.codehaus.cargo.util.log.SimpleLogger;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.installation.InstallRequest;
import org.sonatype.aether.installation.InstallResult;
import org.sonatype.aether.installation.InstallationException;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.util.artifact.DefaultArtifact;

public class FarmRepo {

	public static void start(String path) {
		Logger logger = new SimpleLogger();
		LocalConfiguration configuration = (LocalConfiguration) new DefaultConfigurationFactory()
				.createConfiguration("tomcat6x", ContainerType.INSTALLED,
						ConfigurationType.STANDALONE);
		configuration.setLogger(logger);
		InstalledLocalContainer container = (InstalledLocalContainer) new DefaultContainerFactory()
				.createContainer("tomcat6x", ContainerType.INSTALLED,
						configuration);
		container.setHome(path);
		container.start();

	}

	public static class ManualWagonProvider implements WagonProvider {

		public Wagon lookup(String roleHint) throws Exception {
			if ("http".equals(roleHint)) {
				return new HttpWagon();
			}
			return null;
		}

		public void release(Wagon wagon) {
		}

	}

	static RepositorySystem newRepositorySystem() {
		DefaultServiceLocator locator = new DefaultServiceLocator();

		locator.setServices(WagonProvider.class, new ManualWagonProvider());

		locator.addService(RepositoryConnectorFactory.class,
				WagonRepositoryConnectorFactory.class);

		return locator.getService(RepositorySystem.class);
	}

	public static Installer fetchRemote(String name, String url)
			throws MalformedURLException {
		Installer installer = new ZipURLInstaller(new URL("file://" + url));
		Logger logger = new SimpleLogger();
		logger.setLevel(LogLevel.DEBUG);
		installer.setLogger(logger);

		installer.install();

		return installer;
	}

	private final class DirectoryFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			if (pathname.isDirectory())
				return true;
			return false;
		}
	}

	private RepositorySystem repoSystem = newRepositorySystem();
	private MavenRepositorySystemSession session;
	private Map<String, Artifact> artifacts;

	public FarmRepo(String repoPath) {

		session = new MavenRepositorySystemSession();

		LocalRepository localRepo = new LocalRepository(repoPath);
		session.setLocalRepositoryManager(repoSystem
				.newLocalRepositoryManager(localRepo));

		artifacts = new HashMap<String, Artifact>();

		File animalsPath = new File(localRepo.getBasedir().getAbsolutePath()
				+ File.separator + "org" + File.separator + "mule"
				+ File.separator + "farm" + File.separator + "animals");

		animalsPath.mkdirs();

		File[] animals = animalsPath.listFiles(new DirectoryFilter());

		for (File animal : animals) {
			for (File version : animal.listFiles(new DirectoryFilter())) {
				for (File file : version.listFiles(new FilenameFilter() {

					@Override
					public boolean accept(File arg0, String arg1) {
						if (arg1.startsWith("_")) {
							return false;
						}
						return true;
					}
				})) {
					String[] splittedPath = file.getAbsolutePath().split("/");
					String[] artifactData = splittedPath[splittedPath.length - 1]
							.split("-");
					String artifactId = artifactData[0];
					String[] arrayWithVersionAndExtension = artifactData[1]
							.split("\\.");
					String artifactVersion = StringUtils.join(Arrays
							.copyOfRange(arrayWithVersionAndExtension, 0,
									arrayWithVersionAndExtension.length - 1),
							".");
					String artifactType = StringUtils.join(Arrays.copyOfRange(
							arrayWithVersionAndExtension,
							arrayWithVersionAndExtension.length - 1,
							arrayWithVersionAndExtension.length), "");

					Artifact artifact = new DefaultArtifact(
							"org.mule.farm.animals", artifactId, artifactType,
							artifactVersion);

					add(artifactId, retrieveArtifactWithRequest(artifact));
				}
			}
		}

	}

	private void add(String name, Artifact artifact) {
		artifacts.put(name, artifact);
	}

	private Artifact addAndInstall(String name, String url, Artifact artifact) {
		artifacts.put(name, artifact);

		Artifact jarArtifact = artifact;
		jarArtifact = jarArtifact.setFile(new File(url));

		InstallRequest installRequest = new InstallRequest();
		installRequest.addArtifact(jarArtifact);

		InstallResult installResult = null;

		try {
			installResult = repoSystem.install(session, installRequest);
		} catch (InstallationException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		Collection<Artifact> artifacts = installResult.getArtifacts();
		if (artifacts.isEmpty()) {
			throw new RuntimeException();
		}
		return artifacts.iterator().next();
	}

	private Artifact doHerd(String alias) throws ArtifactNotRegisteredException {
		Artifact artifact = artifacts.get(alias);

		if (artifact == null) {
			throw new ArtifactNotRegisteredException();
		}

		return retrieveArtifactWithRequest(artifact);
	}

	private Artifact retrieveArtifactWithRequest(Artifact artifact) {
		ArtifactRequest artifactRequest = new ArtifactRequest();

		artifactRequest.setArtifact(artifact);
		artifactRequest
				.addRepository(new RemoteRepository("mule", "default",
						"http://dev.ee.mulesource.com/repository/content/repositories/snapshots"));

		try {
			ArtifactResult artifactResult = repoSystem.resolveArtifact(session,
					artifactRequest);
			artifact = artifactResult.getArtifact();
		} catch (ArtifactResolutionException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}

		return artifact;
	}

	public Artifact breed(String alias) throws ArtifactNotRegisteredException {
		Artifact artifact = herd(alias);

		try {
			fetchRemote(artifact.getArtifactId(), artifact.getFile().toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}

		return artifact;
	}

	public Artifact herd(String alias) throws ArtifactNotRegisteredException {
		Artifact artifact = doHerd(alias);

		String path = artifact.getFile().getPath();
		String[] splittedPath = path.split(File.separator);

		copy(artifact.getFile().toString(), "." + File.separator
				+ splittedPath[splittedPath.length - 1]);

		return artifact;
	}

	public Artifact summon(String alias, String version, String url) {
		return addAndInstall(alias, url, new DefaultArtifact(
				"org.mule.farm.animals", alias, "zip", version));
	}

	public Collection<Artifact> list() {
		return artifacts.size() == 0 ? new ArrayList<Artifact>() : artifacts
				.values();
	}

}