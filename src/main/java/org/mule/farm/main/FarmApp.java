package org.mule.farm.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.apache.maven.repository.internal.DefaultServiceLocator;
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
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;

public class FarmApp {
	private static final int VERSION_NOT_SPECIFIED_ERROR = -2;
	public static final int ANIMAL_NOT_FOUND_ERROR = -1;
	public static final int SUCCESS = 0;

	public static Installer fetchRemote(String name, String url)
			throws MalformedURLException {
		Installer installer = new ZipURLInstaller(new URL("file://" + url));
		Logger logger = new SimpleLogger();
		logger.setLevel(LogLevel.DEBUG);
		installer.setLogger(logger);

		installer.install();

		return installer;
	}

	public static void copy(String origin, String destiny) {
		File inputFile = new File(origin);
		File outputFile = new File(destiny);

		FileReader in = null;
		FileWriter out = null;

		try {
			in = new FileReader(inputFile);
			out = new FileWriter(outputFile);

			int c;

			while ((c = in.read()) != ANIMAL_NOT_FOUND_ERROR)
				out.write(c);

			in.close();
			out.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}

	}

	public static void move(String origin, String destiny) {
		new File(origin).renameTo(new File(destiny));
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

	static RepositorySystem newRepositorySystem() {
		DefaultServiceLocator locator = new DefaultServiceLocator();

		locator.setServices(WagonProvider.class, new ManualWagonProvider());

		locator.addService(RepositoryConnectorFactory.class,
				WagonRepositoryConnectorFactory.class);

		return locator.getService(RepositorySystem.class);
	}

	public static int trueMainWithRepo(String[] args, String repoPath) {

		if (args.length < 2) {
			System.err.println("Invalid parameters");
			return -3;
		}
		FarmRepo farmRepo;
		farmRepo = new FarmRepo(repoPath);
		Artifact artifact = null;

		try {
			if ("breed".equals(args[1])) {
				artifact = farmRepo.breed(args[2]);
			} else if ("herd".equals(args[1])) {
				artifact = farmRepo.herd(args[2]);
			} else if ("summon".equals(args[1])) {
				String[] args2Parts = args[2].split("@");
				if (args2Parts.length != 2) {
					System.err.println("Version should be specified");
					return VERSION_NOT_SPECIFIED_ERROR;
				}
				artifact = farmRepo.summon(args2Parts[0], args2Parts[1],
						args[3]);
			} else if ("list".equals(args[1])) {
				Collection<Artifact> animals = farmRepo.list();
				
				if (animals.size() == 0) {
					System.err.println("No results found.");
					return SUCCESS;
				}
				
				System.err.println("Available animals:");
				
				for ( Artifact animal : animals) {
					System.err.println(animal.getArtifactId() + " - " + animal.getFile().toString());
				}
				return SUCCESS;
			}
		} catch (ArtifactNotRegisteredException e) {
			System.err.println("Error: Artifact not registered.");
			return ANIMAL_NOT_FOUND_ERROR;
		}

		System.err.println("Loaded " + artifact);
		return SUCCESS;

	}

	public static int trueMain(String[] args) {
		return trueMainWithRepo(args, System.getProperty("user.home")
				+ File.separator + ".m2");
	}

	public static void main(String[] args) {
		System.exit(trueMain(args));
	}

}
