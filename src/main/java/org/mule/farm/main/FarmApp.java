package org.mule.farm.main;

import java.io.File;
import java.util.Collection;

import org.sonatype.aether.artifact.Artifact;

public class FarmApp {
	private static final int VERSION_NOT_SPECIFIED_ERROR = -2;
	public static final int ANIMAL_NOT_FOUND_ERROR = -1;
	public static final int SUCCESS = 0;

	public static int trueMainWithRepo(String[] args, String repoPath) {

		if (args.length < 1) {
			System.err.println("Invalid parameters");
			return -3;
		}
		FarmRepo farmRepo;
		farmRepo = new FarmRepo(repoPath);
		Artifact artifact = null;

		try {
			if ("breed".equals(args[0])) {
				artifact = farmRepo.breed(args[1]);
			} else if ("herd".equals(args[0])) {
				artifact = farmRepo.herd(args[1]);
			} else if ("summon".equals(args[0])) {
				String[] args2Parts = args[1].split("@");
				if (args2Parts.length != 2) {
					System.err.println("Version should be specified");
					return VERSION_NOT_SPECIFIED_ERROR;
				}
				artifact = farmRepo.summon(args2Parts[0], args2Parts[1],
						args[2]);

				System.out.println(artifact.getFile());
				return SUCCESS;
			} else if ("list".equals(args[0])) {
				Collection<Artifact> animals = farmRepo.list();

				if (animals.size() == 0) {
					System.err.println("No results found.");
					return SUCCESS;
				}

				System.err.println("Available animals:");

				for (Artifact animal : animals) {
					System.err.println(animal.getArtifactId());
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
