package org.mule.farm.main;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
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
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.util.artifact.DefaultArtifact;

/**
 * Hello world!
 * 
 */

public class FarmApp {
	public static String fetchRemote(String name, String url)
			throws MalformedURLException {
		Installer installer = new ZipURLInstaller(new URL(url));
		Logger logger = new SimpleLogger();
		logger.setLevel(LogLevel.DEBUG);
		installer.setLogger(logger);
		installer.install();

		String tomcatHome = "." + File.separator + name;

		new File(installer.getHome()).renameTo(new File(tomcatHome));

		return tomcatHome;
	}

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

	private static RepositorySystem newRepositorySystem() {
		DefaultServiceLocator locator = new DefaultServiceLocator();
		locator.setServices(WagonProvider.class, new ManualWagonProvider());

		locator.addService(RepositoryConnectorFactory.class,
				WagonRepositoryConnectorFactory.class);

		return locator.getService(RepositorySystem.class);
	}

	private static RepositorySystemSession newSession(RepositorySystem system) {
		MavenRepositorySystemSession session = new MavenRepositorySystemSession();

		LocalRepository localRepo = new LocalRepository(
				"/Users/apose/.m2/repository");
		session.setLocalRepositoryManager(system
				.newLocalRepositoryManager(localRepo));

		return session;
	}

	public static Artifact fetch(RepositorySystem repoSystem,
			RepositorySystemSession session, Artifact artifact) {

		if (artifact == null) {
			throw new IllegalArgumentException();
		}		
		
		ArtifactRequest artifactRequest = new ArtifactRequest();
		
		artifactRequest.setArtifact(artifact);
		// artifactRequest.addRepository( new RemoteRepository( "mule",
		// "default",
		// "http://dev.ee.mulesource.com/repository/content/repositories/releases"
		// ) );

		try {
			ArtifactResult artifactResult = repoSystem.resolveArtifact(session,
					artifactRequest);
			artifact = artifactResult.getArtifact();
		} catch (ArtifactResolutionException e) {
			fetchRemote()
		}

		return artifact;
	}

	public static void main(String[] args) {
		RepositorySystem repoSystem = newRepositorySystem();
		
		RepositorySystemSession session = newSession(repoSystem);
		Artifact tomcatArtifact = fetch(repoSystem, session, new DefaultArtifact("org.mule.farm",
				"tomcat-packed", "zip", "6.0.32"));

		Artifact artifact = fetch(repoSystem, session, new DefaultArtifact("com.mulesoft.mmc",
				"mule-ee-distribution-standalone-mmc", "zip", "3.1.1"));
		
		//
		// Artifact jarArtifact = new DefaultArtifact( "org.mule.farm",
		// "tomcat-packed", "zip", "6.0.32" );
		// jarArtifact = jarArtifact.setFile( new File(
		// "apache-tomcat-6.0.32.zip" ) );
		//
		// InstallRequest installRequest = new InstallRequest();
		// installRequest.addArtifact( jarArtifact ).addArtifact( jarArtifact );
		//
		// try {
		// repoSystem.install( session, installRequest );
		// } catch (InstallationException e) {
		// e.printStackTrace();
		// }
		System.out.println(tomcatArtifact.getFile());
		System.out.println(artifact.getFile());

		// Map<String, String> urlMap = new HashMap<String, String>();
		//
		// //
		// http://apache.xmundo.com.ar/tomcat/tomcat-6/v6.0.32/bin/apache-tomcat-6.0.32.zip
		// urlMap.put("tomcat6x", "file://.Ê/apache-tomcat-6.0.32.zip");
		// urlMap.put(
		// "jboss5x",
		// "http://downloads.sourceforge.net/project/jboss/JBoss/JBoss-5.0.1.GA/jboss-5.0.1.GA.zip");
		// urlMap.put(
		// "jboss4x",
		// "http://sourceforge.net/projects/jboss/files/JBoss/JBoss-4.2.2.GA/jboss-4.2.2.GA.zip");
		// String url = urlMap.get("tomcat6x");
		// Logger logger = new SimpleLogger();
		// try {
		// start(install("tomcat6x", url));
		//
		// // configuration.addDeployable(new WAR("cargo.war"));
		//
		// } catch (MalformedURLException e) {
		// e.printStackTrace();
		// }

	}
}
