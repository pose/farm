package org.mule.farm.api;

import java.util.Collection;

import org.mule.farm.main.ArtifactNotRegisteredException;

public interface FarmRepository {

	public Animal install(String identifier)
			throws ArtifactNotRegisteredException;

	public Animal get(String identifier)
			throws ArtifactNotRegisteredException;

	public Animal put(String identifier, String version, String url);

	public Collection<Animal> list();

}