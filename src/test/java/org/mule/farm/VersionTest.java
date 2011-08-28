package org.mule.farm;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;

import org.junit.Test;
import org.mule.farm.main.Version;


public class VersionTest {
	
	@Test(expected=IllegalArgumentException.class) 
	public void testCreateWithInvalidStrings() {
		Version.fromString("");
	}
	
	@Test(expected=NullPointerException.class)
	public void testCreateFromNullString() {
		Version.fromString(null);
	}
	
	@Test
	public void testCreateFromString() {
		List<Integer> version = new ArrayList<Integer>();
		version.add(3);
		version.add(2);
		version.add(1);
		assertEquals(version,Version.fromString("3.2.1").getVersionNumbers());
		
		version = new ArrayList<Integer>();
		version.add(3);
		version.add(2);
		assertEquals("3.2. case", version, Version.fromString("3.2.").getVersionNumbers());
	}
	
	@Test
	public void testEquals() {
		assertEquals("Test 0 padding", Version.fromString("3.2.0"), Version.fromString("3.2"));
	}
	
	@Test
	public void testComparison() {
		assertEquals(1,Version.fromString("3.2.1").compareTo(Version.fromString("3.1")));
		assertEquals(-1,Version.fromString("3.2.1").compareTo(Version.fromString("3.38")));
		assertEquals(-1,Version.fromString("3.2.1").compareTo(Version.fromString("4")));
		assertEquals("Test 0 padding", 0, Version.fromString("3.2.0").compareTo( Version.fromString("3.2")));
	}
	
	@Test
	public void inferVersionFromFileName() {
		assertEquals(Version.fromString("6.0.32"), Version.inferVersionFromFileName("apache-tomcat-6.0.32.zip"));
		assertEquals(Version.fromString("7.0.20"), Version.inferVersionFromFileName("apache-tomcat-7.0.20.zip"));
		assertEquals(Version.fromString("4.2.3"), Version.inferVersionFromFileName("jboss-4.2.3.zip"));
		assertEquals(Version.fromString("5.1.0"), Version.inferVersionFromFileName("jboss-5.1.0.zip"));
		assertEquals(Version.fromString("6.1.0"), Version.inferVersionFromFileName("jboss-6.1.0.GA.zip"));
		//TODO Support SNAPSHOT versions
//		Version.inferVersionFromFileName("mule-server-3.2.0-SNAPSHOT.war");
	}
}
