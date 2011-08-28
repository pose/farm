package org.mule.farm.main;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import org.mule.farm.api.Animal;
import org.mule.farm.api.FarmRepository;

public class FarmApp {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Command {
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface OnException {
		Class<? extends Exception> value();

		String message();

		int returnCode();
	}

	public static final int INVALID_PARAMETERS = -3;
	public static final int VERSION_NOT_SPECIFIED_ERROR = -2;
	public static final int ANIMAL_NOT_FOUND_ERROR = -1;
	public static final int SUCCESS = 0;
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

		if (args.length < 1) {
			System.err.println("Invalid parameters");
			return INVALID_PARAMETERS;
		}

		FarmApp farmApp = new FarmApp(repoPath);
		for (Method method : FarmApp.class.getMethods()) {
			if (method.isAnnotationPresent(Command.class)
					&& method.getName().equals(args[0])
					&& method.getParameterTypes().length == args.length - 1) {
				try {
					method.invoke(farmApp,
							(Object[]) Arrays.copyOfRange(args, 1, args.length));
					return SUCCESS;
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					if (method.isAnnotationPresent(OnException.class)) {
						OnException annotation = method
								.getAnnotation(OnException.class);
						if (annotation != null
								&& annotation.value().equals(
										e.getCause().getClass())) {
							System.err.println(annotation.message());
							return annotation.returnCode();
						}
					}

					e.printStackTrace();
				}
			}
		}

		// TODO Add how should valid parameters be
		System.err.println("Error: Invalid parameters.");
		return FarmApp.INVALID_PARAMETERS;

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
