/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.filesystem;

public class FileSystemChangesAlwaysTrue implements FileSystemChanges {

	@Override
	public boolean hasChanges() {
		return true;
	}

	@Override
	public void reload() {
	}

}
