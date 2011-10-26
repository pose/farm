package org.mule.farm.impl;

import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.providers.http.HttpWagon;
import org.sonatype.aether.connector.wagon.WagonProvider;

public class ManualWagonProvider implements WagonProvider {

	public Wagon lookup(String roleHint) throws Exception {
		System.out.println("rolehit " + roleHint);
		if ("http".equals(roleHint)) {
			return new HttpWagon();
		}
		return null;
	}

	public void release(Wagon wagon) {
	}

}