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
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of FileSystemChanges that uses NIO
 * 
 */
public class FileSystemChangesNIO implements FileSystemChanges {

	final Logger logger = LoggerFactory.getLogger(FileSystemChangesNIO.class);

	private WatchService watcher;
	private final String dir;
	
	/**
	 * Types that trigger changes
	 */
	private List<String> types;
	
	/**
	 * Watch the directory and trigger changes only when a file of one of passed
	 * type change. If typesToWatch is null then all files should be watched.
	 */
	public FileSystemChangesNIO(String dir, String...typesToWatch) {
		this.dir = dir;
		if (typesToWatch.length > 0) {
			types = new ArrayList<>(typesToWatch.length);
			for (String type : typesToWatch) {
				types.add(type);
			}
		}
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void init() throws Exception {
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
				if (otherKey != null) {
					List<WatchEvent<?>> events = otherKey.pollEvents();
					if (events.size() > 0) {
						if (checkForTypes(events)) {
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	private boolean checkForTypes(List<WatchEvent<?>> events) {
		if (types == null) {
			return true;
		}
		for (WatchEvent<?> event : events) {
			Path path = (Path) event.context();
			String name = path.getFileName().toString();
			int index = name.indexOf(".");
			if (index != -1 && types.contains(name.substring(index + 1))) {
				return true;
			}
		}
		return false;
	}

	public void reload() {
		try {
			watcher.close();
			watcher = null;
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
