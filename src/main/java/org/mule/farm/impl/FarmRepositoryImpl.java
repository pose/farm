package org.mule.farm.impl;

import static org.mule.farm.util.Util.copy;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.maven.repository.internal.DefaultServiceLocator;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
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
import org.mule.farm.api.Animal;
import org.mule.farm.api.ArtifactNotRegisteredException;
import org.mule.farm.api.FarmRepository;
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

public class FarmRepositoryImpl implements FarmRepository {

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

	public static RepositorySystem newRepositorySystem() {
		DefaultServiceLocator locator = new DefaultServiceLocator();

		locator.setServices(WagonProvider.class, new ManualWagonProvider());

		locator.addService(RepositoryConnectorFactory.class,
				WagonRepositoryConnectorFactory.class);

		return locator.getService(RepositorySystem.class);
	}

	public static Installer fetchRemote(String name, String url)
			throws MalformedURLException {

		Installer installer = new ZipURLInstaller(
				new URL(
						!(url.startsWith("http://") || url
								.startsWith("https://")) ? ("file://" + url)
								: url));
		Logger logger = new SimpleLogger();
		logger.setLevel(LogLevel.DEBUG);
		installer.setLogger(logger);

		installer.install();

		return installer;
	}

	private final class NoHiddenFilesFilter implements FilenameFilter {
		@Override
		public boolean accept(File arg0, String arg1) {
			if (arg1.startsWith("_")) {
				return false;
			}
			return true;
		}
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
	private Map<String, Animal> animals;
	private String workingDirectory;

	public static FarmRepositoryImpl createFarmRepository(String repoPath) {
		return new FarmRepositoryImpl(repoPath, ".");
	}

	private FarmRepositoryImpl(String repoPath, String workingDirectory) {
		Validate.notNull(repoPath);
		Validate.notNull(workingDirectory);

		this.workingDirectory = workingDirectory;
		session = new MavenRepositorySystemSession();

		LocalRepository localRepo = new LocalRepository(repoPath);
		session.setLocalRepositoryManager(repoSystem
				.newLocalRepositoryManager(localRepo));

		animals = new HashMap<String, Animal>();

		File animalsPath = new File(StringUtils.join(new String[] {
				localRepo.getBasedir().getAbsolutePath(), "org", "mule",
				"farm", "animals" }, File.separator));

		animalsPath.mkdirs();

		File[] animals = animalsPath.listFiles(new DirectoryFilter());

		for (File animal : animals) {
			for (File version : animal.listFiles(new DirectoryFilter())) {
				for (File file : version.listFiles(new NoHiddenFilesFilter())) {

					String[] splittedPath = file.getAbsolutePath().split("/");
					String[] artifactData = splittedPath[splittedPath.length - 1]
							.split("-");
					String artifactId = artifactData[0];
					String[] arrayWithVersionAndExtension = artifactData[1]
							.split("\\.");

					Artifact artifact = new DefaultArtifact(
							"org.mule.farm.animals", artifactId,
							parseArtifactType(arrayWithVersionAndExtension),
							parseArtifactVersion(arrayWithVersionAndExtension));

					add(artifactId,
							AnimalImpl
									.createAnimalFromArtifact(retrieveArtifactWithRequest(artifact)));
				}
			}
		}

	}

	private String parseArtifactType(String[] arrayWithVersionAndExtension) {
		String artifactType = StringUtils.join(Arrays.copyOfRange(
				arrayWithVersionAndExtension,
				arrayWithVersionAndExtension.length - 1,
				arrayWithVersionAndExtension.length), "");
		return artifactType;
	}

	private String parseArtifactVersion(String[] arrayWithVersionAndExtension) {
		String artifactVersion = StringUtils.join(Arrays.copyOfRange(
				arrayWithVersionAndExtension, 0,
				arrayWithVersionAndExtension.length - 1), ".");
		return artifactVersion;
	}

	private void add(String name, Animal animal) {
		animals.put(name, animal);
	}

	private String downloadAndGetFileUrl(String url) {
		ExecutorService pool = Executors.newFixedThreadPool(1);
		URL realUrl = null;
		try {
			realUrl = new URL(url);
		} catch (MalformedURLException e1) {
			throw new RuntimeException(e1);
		}
		final ReadableByteChannel rbc;
		final File tmp;

		try {
			rbc = Channels.newChannel(realUrl.openStream());
			tmp = File.createTempFile("farmdownload", null);
			tmp.deleteOnExit();
			final FileOutputStream fos = new FileOutputStream(tmp);
			Future<String> f = pool.submit(new Callable<String>() {

				@Override
				public String call() {
					try {
						fos.getChannel().transferFrom(rbc, 0, 1 << 24);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}

					return tmp.getAbsolutePath();
				}
			});

			System.err.print("Downloading");
			while (!f.isDone()) {
				System.err.print(".");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
			System.err.println(" done");
			return f.get();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private Animal addAndInstall(String name, String url, Animal animal) {
		animals.put(name, animal);

		if (url.startsWith("http://") || url.startsWith("https://")) {
			url = downloadAndGetFileUrl(url);
		}

		Artifact jarArtifact = animal.getArtifact();
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

		return AnimalImpl.createAnimalFromArtifact(artifacts.iterator().next());
	}

	private Artifact doHerd(String alias) throws ArtifactNotRegisteredException {
		Animal animal = animals.get(alias);

		if (animal == null) {
			throw new ArtifactNotRegisteredException();
		}

		Artifact artifact = animal.getArtifact();

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.farm.main.FarmRepository#breed(java.lang.String)
	 */
	@Override
	public Animal install(String alias) throws ArtifactNotRegisteredException {
		Artifact artifact = get(alias).getArtifact();

		try {
			fetchRemote(artifact.getArtifactId(), artifact.getFile().toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}

		return AnimalImpl.createAnimalFromArtifact(artifact);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.farm.main.FarmRepository#get(java.lang.String)
	 */
	@Override
	public Animal get(String alias) throws ArtifactNotRegisteredException {
		Artifact artifact = doHerd(alias);

		String path = artifact.getFile().getPath();
		String[] splittedPath = path.split(File.separator);

		copy(artifact.getFile().toString(), workingDirectory + File.separator
				+ splittedPath[splittedPath.length - 1]);

		return AnimalImpl.createAnimalFromArtifact(artifact);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.farm.main.FarmRepository#summon(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public Animal put(String alias, String version, String url) {
		return addAndInstall(alias, url,
				AnimalImpl.createAnimalFromArtifact(new DefaultArtifact(
						"org.mule.farm.animals", alias, "zip", version)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.farm.main.FarmRepository#list()
	 */
	@Override
	public Collection<Animal> list() {
		return animals.size() == 0 ? new ArrayList<Animal>() : animals.values();
	}

}