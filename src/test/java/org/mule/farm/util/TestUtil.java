package org.mule.farm.util;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.regex.Pattern;

import org.mule.farm.main.FarmApp;

public class TestUtil {

	public static int callFarmAppMainWithRepo(String repo, String... s) {
		return FarmApp.trueMainWithRepo(s, repo);
	}

	public static int callFarmAppMain(String... s) {
		return FarmApp.trueMain(s);
	}

	public static void assertFileExistenceWithRegex(String regex) {

		File root = new File(".");

		for (File file : root.listFiles()) {
			String path = file.getPath();
			String[] splittedPath = path.split(File.separator);
			if (splittedPath.length != 2) {
				continue;
			}

			if (Pattern.matches(regex, splittedPath[1])) {
				if (file.isFile()) {
					return;
				}
			}
		}
		fail(String.format("Files with the regex [%s] were not found", regex));
	}

	public static void assertFileExistence(String fileName) {
		File file = new File("." + File.separator + fileName);
		assertTrue(String.format("Path [%s] does not exist", fileName),
				file.exists());
		assertTrue(String.format("[%s] is not a file", fileName), file.isFile());
	}

	public static void assertFolderExistence(String folderName) {
		File file = new File("." + File.separator + folderName);
		assertTrue(String.format("Path [%s] does not exist", folderName),
				file.exists());
		assertTrue(String.format("[%s] is not a folder", folderName),
				file.isDirectory());
	}

	public static void assertFolderExistenceWithRegex(String regex) {

		File root = new File(".");

		for (File file : root.listFiles()) {
			String path = file.getPath();
			String[] splittedPath = path.split(File.separator);
			if (splittedPath.length != 2) {
				continue;
			}

			if (Pattern.matches(regex, splittedPath[1])) {
				if (file.isDirectory()) {
					return;
				}
			}
		}
		fail(String.format("Folders with regex [%s] were not found", regex));
	}
}

