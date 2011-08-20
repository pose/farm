package org.mule.farm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.farm.main.FarmApp;

import com.sun.xml.internal.ws.developer.MemberSubmissionAddressing;

public class FarmTest {

	private void createFolder(String name) {
		File folder = new File("." + File.separator + name);
		folder.mkdir();
	}

	public static void removeFolder(String folderName) {
		deleteDirectory(new File("." + File.separator + folderName));
	}

	public static boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	@Before
	public void setUp() {
		removeFolder("repo_eraseme");
		createFolder("repo_eraseme");
	}

	@After
	public void tearDown() {
		removeFolder("repo_eraseme");
	}

	@Test
	public void testSanityCheck() {
		createFolder("foobar");
		assertFolderExistenceWithRegex("foo.*");
		assertFolderExistence("foobar");
		removeFolder("foobar");
	}

	public static int callFarmAppMainWithRepo(String repo, String... s) {
		return FarmApp.trueMainWithRepo(s, repo);
	}

	public static int callFarmAppMain(String... s) {
		return FarmApp.trueMain(s);
	}

	@Test
	public void testJBoss4x() {
		assertEquals("As the repo is new, nothing must be found here",
				FarmApp.ANIMAL_NOT_FOUND_ERROR,
				callFarmAppMainWithRepo("repo_eraseme", "breed", "jboss4x"));

		assertEquals(
				"Adding JBoss4x to the repo",
				FarmApp.SUCCESS,
				callFarmAppMainWithRepo("repo_eraseme", "summon",
						"jboss4x@4.2.3", "jboss-4.2.3.GA.zip"));

		assertEquals("Now I should retrieve JBoss4x successfully", FarmApp.SUCCESS,
				callFarmAppMainWithRepo("repo_eraseme", "breed", "jboss4x"));

		assertFolderExistence("jboss4x");
	}

	@Test
	public void testJBoss5x() {
		callFarmAppMainWithRepo("repo_eraseme", "summon",
				"jboss6x@6.1.0", "jboss-6.1.0.GA.zip");
		callFarmAppMain("", "breed", "jboss5x");
		assertFolderExistence("jboss5x");
	}

	@Test
	public void testJBoss6x() {
		callFarmAppMainWithRepo("repo_eraseme", "summon",
				"jboss6x@6.1.0", "jboss-6.1.0.GA.zip");
		callFarmAppMain("", "breed", "jboss6x");
		assertFolderExistence("jboss6x");
	}

	@Test
	public void testJTomcat6x() {
		callFarmAppMainWithRepo("repo_eraseme", "summon",
				"tomcat6x@6.0.32", "apache-tomcat-6.0.32.zip");
		callFarmAppMain("", "breed", "tomcat6x");
		assertFolderExistence("tomcat6x");
	}

	@Test
	public void testJTomcat7x() {
		callFarmAppMainWithRepo("repo_eraseme", "summon",
				"tomcat7x@6.0.20", "apache-tomcat-7.0.20.zip");
		callFarmAppMain("", "breed", "tomcat7x");
		assertFolderExistence("tomcat7x");
	}

	@Test
	public void testMule() {
		callFarmAppMain("", "breed", "mule-ee-distribution-standalone-mmc");
		assertFolderExistence("mule-ee-distribution-standalone-mmc");

		callFarmAppMain("", "herd", "mmc-server");
		assertFileExistenceWithRegex("mmc-server.*\\.war");

		callFarmAppMain("", "herd", "mmc-mule-app");
		assertFileExistenceWithRegex("mmc-mule-app.*\\.zip");
	}
	
	@Test
	public void testList() {
		callFarmAppMainWithRepo("repo_eraseme", "list");
		
		callFarmAppMainWithRepo("repo_eraseme", "summon",
				"jboss6x@6.1.0", "jboss-6.1.0.GA.zip");
		callFarmAppMainWithRepo("repo_eraseme", "summon",
				"tomcat7x@7.0.20", "apache-tomcat-7.0.20.zip");
		
		callFarmAppMainWithRepo("repo_eraseme", "list");
	}

	private void assertFileExistenceWithRegex(String regex) {

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

	@SuppressWarnings("unused")
	private void assertFileExistence(String fileName) {
		File file = new File("." + File.separator + fileName);
		assertTrue(String.format("Path [%s] does not exist", fileName),
				file.exists());
		assertTrue(String.format("[%s] is not a file", fileName), file.isFile());
	}

	private void assertFolderExistence(String folderName) {
		File file = new File("." + File.separator + folderName);
		assertTrue(String.format("Path [%s] does not exist", folderName),
				file.exists());
		assertTrue(String.format("[%s] is not a folder", folderName),
				file.isDirectory());
	}

	private void assertFolderExistenceWithRegex(String regex) {

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
