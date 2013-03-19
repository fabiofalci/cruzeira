/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.filesystem;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of FileSystemChanges that uses NIO
 * 
 */
public class FileSystemChangesNIO implements FileSystemChanges {

	final Logger logger = LoggerFactory.getLogger(FileSystemChangesNIO.class);

	private WatchService watcher;

	public FileSystemChangesNIO(String dir) {
		try {
			init(dir);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void init(String dir) throws Exception {
		watcher = FileSystems.getDefault().newWatchService();
		Path path = Paths.get(dir);

		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
				return FileVisitResult.CONTINUE;
			}
		});

		logger.debug("Watching filesystem for changes: {}", dir);
	}

	public boolean hasChanges() {
		try {
			if (watcher != null) {
				WatchKey otherKey = watcher.poll();
				if (otherKey != null && otherKey.pollEvents().size() > 0) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public void shutdown() {
		try {
			watcher.close();
			watcher = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
