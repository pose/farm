package org.mule.farm;

import java.io.File;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.mule.farm.main.FarmApp;

public class FarmTest {

	private void createFolder(String name) {
		File folder = new File("." + File.separator + name);
		folder.mkdir();
	}
	
	private void removeFolder(String name) {
		new File("." + File.separator + name).delete();
	}

	@Test
	public void testSanityCheck() {
		createFolder("foobar");
		assertFolderExistenceWithRegex("foo.*");
		assertFolderExistence("foobar");
		removeFolder("foobar");
		
	}

	@Test
	public void testMainApp() {
		 FarmApp.main(new String[] { "fetch", "jboss5", "to", "jboss5" });
		 assertFolderExistence("jboss5");
		
		 FarmApp.main(new String[] { "fetch", "tomcat6", "to", "tomcat6" });
		 assertFolderExistence("tomcat6");
		
		 FarmApp.main(new String[] { "fetch", "tomcat7", "to", "tomcat7" });
		 assertFolderExistence("tomcat7");
		
		 FarmApp.main(new String[] { "fetch", "mule3.1.2ee", "to", "muley3"
		 });
		 assertFolderExistence("mule3.2ee");

		FarmApp.main(new String[] { "fetch", "mule2.6ce" });
		assertFolderExistenceWithRegex("mule.*");

		FarmApp.main(new String[] { "fetch", "mule3.1.2ce" });
		assertFolderExistenceWithRegex("mule.*");

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
		fail(String.format("Path regex [%s] not found", regex));
	}
}
