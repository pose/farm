package org.mule.farm.main;

import java.io.File;
import java.util.Collection;

import org.mule.barn.Barn;
import org.mule.barn.annotations.Command;
import org.mule.barn.annotations.OnException;
import org.mule.farm.api.Animal;
import org.mule.farm.api.ArtifactNotRegisteredException;
import org.mule.farm.api.FarmRepository;
import org.mule.farm.impl.FarmRepositoryImpl;


public class FarmApp {

	public static final int VERSION_NOT_SPECIFIED_ERROR = -2;
	public static final int ANIMAL_NOT_FOUND_ERROR = -3;
	
	private FarmRepository farmRepo;

	private FarmApp(String repoPath) {
		this.farmRepo = FarmRepositoryImpl.createFarmRepository(repoPath);
	}

	@Command
	@OnException(value = ArtifactNotRegisteredException.class, message = "Error: Animal not found", returnCode = FarmApp.ANIMAL_NOT_FOUND_ERROR)
	public Animal install(String identifier)
			throws ArtifactNotRegisteredException {
		System.err.println("Installing...");
		return farmRepo.install(identifier);
	}

	@Command
	@OnException(value = ArtifactNotRegisteredException.class, message = "Error: Animal not found", returnCode = FarmApp.ANIMAL_NOT_FOUND_ERROR)
	public Animal get(String identifier) throws ArtifactNotRegisteredException {
		System.err.println("Getting...");
		return farmRepo.get(identifier);
	}

	@Command
	public void put(String artifactId, String version, String filePathOrUrl) {
		System.err.println("Putting...");
		farmRepo.put(artifactId, version, filePathOrUrl);
	}

	@Command
	public void put(String artifactMaybeVersion, String filePathOrUrl) {
		String[] args2Parts = artifactMaybeVersion.split("@");
		Version version = null;
		if (args2Parts.length != 2) {

			// If the version was not specified, try to infer it
			// from the file name.
			version = Version.inferVersionFromFileName(filePathOrUrl);
		} else {
			version = Version.fromString(args2Parts[1]);
		}

		put(args2Parts[0], version.toString(), filePathOrUrl);
	}

	@Command
	public void list() {
		Collection<Animal> animals = farmRepo.list();

		if (animals.size() == 0) {
			System.err.println("No results found.");
			return;
		}

		System.out.println("Available animals:");

		for (Animal animalToIterate : animals) {
			System.out.println(animalToIterate.getArtifact().getArtifactId());
		}
	}

	public static int trueMainWithRepo(String[] args, String repoPath) {
		FarmApp farmApp = new FarmApp(repoPath);
		Barn<FarmApp> cli = new Barn<FarmApp>(farmApp, "farm");
		
		return cli.runCommandLine(args);
	}

	public static int trueMain(String[] args) {
		// TODO: Get the real .m2 folder
		return trueMainWithRepo(args, System.getProperty("user.home")
				+ File.separator + ".m2" + File.separator + "repository");
	}

	public static void main(String[] args) {
		System.exit(trueMain(args));
	}

}
