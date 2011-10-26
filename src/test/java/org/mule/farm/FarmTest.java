package org.mule.farm;

import static org.junit.Assert.*;
import static org.mule.farm.util.TestUtil.*;
import static org.mule.farm.util.Util.*;

import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.cli.CLI;
import org.mule.farm.main.FarmApp;
import org.mule.farm.util.MockOutputStream;

public class FarmTest {

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

	@Test
	public void testContainers() {
		containerHelper("jboss4x", "jboss4x@4.2.3", "jboss-4.2.3.GA.zip");
		containerHelper("jboss5x", "jboss5x", "jboss-5.1.0.GA.zip");
		containerHelper("jboss6x", "jboss6x@6.1.0", "jboss6x-6.1.0.zip");
		containerHelper("tomcat6x", "tomcat6x@6.0.32",
				"apache-tomcat-6.0.32.zip");
		containerHelper("tomcat7x", "tomcat7x@6.0.20",
				"apache-tomcat-7.0.20.zip");
	}

	@Test
	public void testLocalPut() {
		assertEquals(
				"Fetch the package from the Internet",
				CLI.SUCCESS,
				callFarmAppMainWithRepo("repo_eraseme", "put", "tomcat6x",
						"apache-tomcat-6.0.33.zip"));
	}

	@Test
	public void testRemotePut() {
		assertEquals(
				"Fetch the package from the Internet",
				CLI.SUCCESS,
				callFarmAppMainWithRepo(
						"repo_eraseme",
						"put",
						"tomcat6x",
						"http://mirrors.axint.net/apache/tomcat/tomcat-6/v6.0.33/bin/apache-tomcat-6.0.33.zip"));
		assertEquals(
				"Fetch the package from the Internet",
				CLI.SUCCESS,
				callFarmAppMainWithRepo(
						"repo_eraseme",
						"install",
						"tomcat6x"));
	}

	private void containerHelper(String name, String nameVersion,
			String zipFileName) {
		assertEquals("As the repo is new, nothing must be found here",
				FarmApp.ANIMAL_NOT_FOUND_ERROR,
				callFarmAppMainWithRepo("repo_eraseme", "install", name));

		assertEquals(
				String.format("Adding [%s] to the repo", name),
				CLI.SUCCESS,
				callFarmAppMainWithRepo("repo_eraseme", "put", nameVersion,
						zipFileName));

		assertEquals(
				String.format("Now I should retrieve [%s] successfully", name),
				CLI.SUCCESS,
				callFarmAppMainWithRepo("repo_eraseme", "install", name));

		assertFolderExistence(name);
	}

//	@Test
//	public void testMule() {
//
//		callFarmAppMain("alias", "mule-ee-distribution-standalone-mmc",
//				"mule-ee-distribution-standalone-mmc:::");
//
//		callFarmAppMain("breed", "mule-ee-distribution-standalone-mmc");
//		assertFolderExistence("mule-ee-distribution-standalone-mmc");
//
//		callFarmAppMain("herd", "mmc-server");
//		assertFileExistenceWithRegex("mmc-server.*\\.war");
//
//		callFarmAppMain("herd", "mmc-mule-app");
//		assertFileExistenceWithRegex("mmc-mule-app.*\\.zip");
//	}

	@Test
	public void testList() {

		MockOutputStream stdout = MockOutputStream.createWireTap(System.out);
		MockOutputStream stderr =  MockOutputStream.createWireTap(System.err);
		System.setErr(new PrintStream(stderr));
		System.setOut(new PrintStream(stdout));
		callFarmAppMainWithRepo("repo_eraseme", "list");
		assertEquals("No results found.\n", stderr.getContent());

		assertEquals(
				CLI.SUCCESS,
				callFarmAppMainWithRepo("repo_eraseme", "put",
						"jboss6x@6.1.0", "jboss6x-6.1.0.zip"));
		assertEquals(
				CLI.SUCCESS,
				callFarmAppMainWithRepo("repo_eraseme", "put",
						"tomcat7x@7.0.20", "apache-tomcat-7.0.20.zip"));

		stdout = MockOutputStream.createWireTap(System.out);
		System.setOut(new PrintStream(stdout));
		callFarmAppMainWithRepo("repo_eraseme", "list");
		assertEquals("Available animals:\ntomcat7x\njboss6x\n",
				stdout.getContent());
	}

}
