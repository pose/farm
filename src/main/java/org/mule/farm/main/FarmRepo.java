package org.mule.farm.main;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.installation.InstallRequest;
import org.sonatype.aether.installation.InstallResult;
import org.sonatype.aether.installation.InstallationException;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

public class FarmRepo {

	private final class DirectoryFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			if (pathname.isDirectory())
				return true;
			return false;
		}
	}

	private RepositorySystem repoSystem = FarmApp.newRepositorySystem();
	private MavenRepositorySystemSession session;
	private Map<String, Artifact> artifacts;

	public FarmRepo(String repoPath) throws EmptyOrInvalidRepoException {

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

		if (animals == null) {
			throw new EmptyOrInvalidRepoException("Empty or Invalid repo");
		}

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

					add(artifactId, new DefaultArtifact(
							"org.mule.farm.animals", artifactId, artifactType,
							artifactVersion));
				}
			}
		}

		// addAndInstall("tomcat6x", "./apache-tomcat-6.0.32.zip",
		// new DefaultArtifact("org.mule.farm", "tomcat6x", "zip",
		// "6.0.32"));
		// addAndInstall("tomcat7x", "./apache-tomcat-7.0.20.zip",
		// new DefaultArtifact("org.mule.farm", "tomcat7x", "zip",
		// "7.0.20"));
		//
		// addAndInstall("jboss6x", "./jboss-as-distribution-6.1.0.Final.zip",
		// new DefaultArtifact("org.mule.farm", "jboss6x", "zip", "6.1.0"));
		// addAndInstall("jboss5x", "./jboss-5.1.0.GA.zip", new DefaultArtifact(
		// "org.mule.farm", "jboss5x", "zip", "5.1.0"));
		// // addAndInstall("jboss4x", "./jboss-4.2.3.GA.zip",
		// // new DefaultArtifact("org.mule.farm", "jboss4x", "zip",
		// // "4.2.3"));
		//
		// add("mmc-mule-app", new DefaultArtifact("com.mulesoft.mmc",
		// "mmc-mule-app", "zip", "3.2.0-SNAPSHOT"));
		//
		// add("mmc-server", new DefaultArtifact("com.mulesoft.mmc",
		// "mmc-server",
		// "war", "3.2.0-SNAPSHOT"));
		//
		// add("mule-ee-distribution-standalone-mmc", new DefaultArtifact(
		// "com.mulesoft.mmc", "mule-ee-distribution-standalone-mmc",
		// "zip", "3.2.0-SNAPSHOT"));

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
			FarmApp.fetchRemote(artifact.getArtifactId(), artifact.getFile()
					.toString());
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

		FarmApp.copy(artifact.getFile().toString(), "." + File.separator
				+ splittedPath[splittedPath.length - 1]);

		return artifact;
	}

	public Artifact summon(String alias, String version, String url) {
		return addAndInstall(alias, url, new DefaultArtifact(
				"org.mule.farm.animals", alias, "zip", version));
	}

}