package org.mule.farm.api;

import org.sonatype.aether.artifact.Artifact;

public interface Animal {
	/** 
	 * Retrieves the artifact associated with the Animal.
	 * 
	 * @return an artifact , never Null.
	 */
	public Artifact getArtifact();
}
