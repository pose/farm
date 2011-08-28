package org.mule.farm.main;

import org.apache.commons.lang3.Validate;
import org.mule.farm.api.Animal;
import org.sonatype.aether.artifact.Artifact;

public class AnimalImpl implements Animal {
	public static AnimalImpl createAnimalFromArtifact(Artifact artifact) {
		return new AnimalImpl(artifact);
	}

	private Artifact artifact;

	private AnimalImpl(Artifact artifact) {
		Validate.notNull(artifact);
		this.artifact = artifact;
	}

	@Override
	public Artifact getArtifact() {
		return this.artifact;
	}

}
