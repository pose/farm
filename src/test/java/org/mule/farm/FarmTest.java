package org.mule.farm;

import static org.junit.Assert.*;
import static org.mule.farm.util.TestUtil.*;
import static org.mule.farm.util.Util.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.farm.main.FarmApp;

public class FarmTest {

	private final class MockOutputStream extends OutputStream {
		private StringBuffer stringBuffer = new StringBuffer();

		@Override
		public void write(int b) throws IOException {
			stringBuffer.append((char) b);
		}

		public String getContent() {
			return stringBuffer.toString();
		}
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

	@Test
	public void testContainers() {
		containerHelper("jboss4x", "jboss4x@4.2.3", "jboss-4.2.3.GA.zip");
		containerHelper("jboss6x", "jboss6x@6.1.0", "jboss-6.1.0.GA.zip");
		containerHelper("tomcat6x", "tomcat6x@6.0.32",
				"apache-tomcat-6.0.32.zip");
		containerHelper("tomcat7x", "tomcat7x@6.0.20",
				"apache-tomcat-7.0.20.zip");
	}

	@Test
	public void testLocalSummon() {
		fail();
	}

	@Test
	public void testRemoteSummon() {
		assertEquals(
				"Fetch the package from the Internet",
				FarmApp.SUCCESS,
				callFarmAppMainWithRepo("repo_eraseme", "summon", "tomcat6x@",
						"http://www.google.com"));
		fail();
	}

	private void containerHelper(String name, String nameVersion,
			String zipFileName) {
		assertEquals("As the repo is new, nothing must be found here",
				FarmApp.ANIMAL_NOT_FOUND_ERROR,
				callFarmAppMainWithRepo("repo_eraseme", "breed", name));

		assertEquals(
				String.format("Adding [%s] to the repo", name),
				FarmApp.SUCCESS,
				callFarmAppMainWithRepo("repo_eraseme", "summon", nameVersion,
						zipFileName));

		assertEquals(
				String.format("Now I should retrieve [%s] successfully", name),
				FarmApp.SUCCESS,
				callFarmAppMainWithRepo("repo_eraseme", "breed", name));

		assertFolderExistence(name);
	}

	@Test
	public void testMule() {
		
		callFarmAppMain("alias", "mule-ee-distribution-standalone-mmc", "mule-ee-distribution-standalone-mmc:::");
		
		callFarmAppMain("breed", "mule-ee-distribution-standalone-mmc");
		assertFolderExistence("mule-ee-distribution-standalone-mmc");

		callFarmAppMain("herd", "mmc-server");
		assertFileExistenceWithRegex("mmc-server.*\\.war");

		callFarmAppMain("herd", "mmc-mule-app");
		assertFileExistenceWithRegex("mmc-mule-app.*\\.zip");
	}

	@Test
	public void testList() {

		MockOutputStream mockOutputStream = new MockOutputStream();
		System.setErr(new PrintStream(mockOutputStream));
		callFarmAppMainWithRepo("repo_eraseme", "list");
		assertEquals("No results found.\n", mockOutputStream.getContent());

		assertEquals(
				FarmApp.SUCCESS,
				callFarmAppMainWithRepo("repo_eraseme", "summon",
						"jboss6x@6.1.0", "jboss-6.1.0.GA.zip"));
		assertEquals(
				FarmApp.SUCCESS,
				callFarmAppMainWithRepo("repo_eraseme", "summon",
						"tomcat7x@7.0.20", "apache-tomcat-7.0.20.zip"));

		mockOutputStream = new MockOutputStream();
		callFarmAppMainWithRepo("repo_eraseme", "list");
		assertEquals("Available animals:\njboss6x\njboss7x\n",
				mockOutputStream.getContent());
	}

}
