/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.filesystem;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.cruzeira.filesystem.FileSystemChangesNIO;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class FileSystemChangesNIOTest {
	static final String basePath = "target/test-fstmp";

	@Before
	public void init() {
		clean();
		createDir(basePath);
	}

	@AfterClass
	public static void clean() {
		Path path = Paths.get(basePath);
		try {
			Files.deleteIfExists(Paths.get(basePath + "/subdir1/subdir2/subfiletmp.txt"));
			Files.deleteIfExists(Paths.get(basePath + "/subdir1/subdir2"));
			Files.deleteIfExists(Paths.get(basePath + "/subdir1"));
			Files.deleteIfExists(Paths.get(basePath + "/tmp.txt"));
			Files.deleteIfExists(Paths.get(basePath + "/tmp1.txt"));
			Files.deleteIfExists(Paths.get(basePath + "/tmp2.readme"));
			Files.deleteIfExists(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void emptyDir() {
		FileSystemChangesNIO fileSystemChanges = new FileSystemChangesNIO(basePath);
		sleep();
		Assert.assertFalse(fileSystemChanges.hasChanges());
	}

	@Test
	public void newFile() {
		FileSystemChangesNIO fileSystemChanges = new FileSystemChangesNIO(basePath);
		createFile(basePath + "/tmp.txt");
		sleep();
		Assert.assertTrue(fileSystemChanges.hasChanges());
	}
	
	@Test
	public void newFileWithIgnore() {
		FileSystemChangesNIO fileSystemChanges = new FileSystemChangesNIO(basePath, ".txt");
		createFile(basePath + "/tmp.txt");
		sleep();
		Assert.assertTrue(fileSystemChanges.hasChanges());
		
		createFile(basePath + "/tmp2.readme");
		sleep();
		Assert.assertFalse(fileSystemChanges.hasChanges());
	}
	
	@Test
	public void reload() {
		FileSystemChangesNIO fileSystemChanges = new FileSystemChangesNIO(basePath);
		createFile(basePath + "/tmp.txt");
		sleep();
		Assert.assertTrue(fileSystemChanges.hasChanges());
		
		fileSystemChanges.reload();
		createFile(basePath + "/tmp1.txt");
		sleep();
		Assert.assertTrue(fileSystemChanges.hasChanges());
	}

	@Test
	public void deleteFile() {
		Path file = createFile(basePath + "/tmp.txt");
		FileSystemChangesNIO fileSystemChanges = new FileSystemChangesNIO(basePath);
		try {
			Files.deleteIfExists(file);
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
		sleep();
		Assert.assertTrue(fileSystemChanges.hasChanges());
	}

	@Test
	public void editFile() {
		Path file = createFile(basePath + "/tmp.txt");
		FileSystemChangesNIO fileSystemChanges = new FileSystemChangesNIO(basePath);

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file.toFile()));
			writer.append("Edited");
			writer.close();
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}

		sleep();

		Assert.assertTrue(fileSystemChanges.hasChanges());
	}

	@Test
	public void emptySubdir() {
		createDir(basePath + "/subdir1/subdir2");
		FileSystemChangesNIO fileSystemChanges = new FileSystemChangesNIO(basePath);
		sleep();
		Assert.assertFalse(fileSystemChanges.hasChanges());
	}

	@Test
	public void newFileSubdir() {
		Path path = createDir(basePath + "/subdir1/subdir2");
		FileSystemChangesNIO fileSystemChanges = new FileSystemChangesNIO(basePath);
		createFile(path + "/subfiletmp.txt");
		sleep();
		Assert.assertTrue(fileSystemChanges.hasChanges());
	}

	@Test
	public void deleteFileSubdir() {
		Path path = createDir(basePath + "/subdir1/subdir2");
		Path file = createFile(path + "/subfiletmp.txt");
		FileSystemChangesNIO fileSystemChanges = new FileSystemChangesNIO(basePath);
		try {
			Files.deleteIfExists(file);
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
		sleep();
		Assert.assertTrue(fileSystemChanges.hasChanges());
	}

	@Test
	public void editFileSubdir() {
		Path path = createDir(basePath + "/subdir1/subdir2");
		Path file = createFile(path + "/subfiletmp.txt");
		FileSystemChangesNIO fileSystemChanges = new FileSystemChangesNIO(basePath);

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file.toFile()));
			writer.append("Edited");
			writer.close();
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}

		sleep();

		Assert.assertTrue(fileSystemChanges.hasChanges());
	}
	
	@Test
	public void ignoreNewFile() {
		FileSystemChangesNIO fileSystemChanges = new FileSystemChangesNIO(basePath, ".doc");
		createFile(basePath + "/tmp.txt");
		sleep();
		Assert.assertFalse(fileSystemChanges.hasChanges());
	}
	
	@Test
	public void ignoreReload() {
		FileSystemChangesNIO fileSystemChanges = new FileSystemChangesNIO(basePath, ".doc");
		createFile(basePath + "/tmp.txt");
		sleep();
		Assert.assertFalse(fileSystemChanges.hasChanges());
		
		fileSystemChanges.reload();
		createFile(basePath + "/tmp1.txt");
		sleep();
		Assert.assertFalse(fileSystemChanges.hasChanges());
	}

	@Test
	public void ignoreDeleteFile() {
		Path file = createFile(basePath + "/tmp.txt");
		FileSystemChangesNIO fileSystemChanges = new FileSystemChangesNIO(basePath, ".doc");
		try {
			Files.deleteIfExists(file);
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
		sleep();
		Assert.assertFalse(fileSystemChanges.hasChanges());
	}

	@Test
	public void ignoreEditFile() {
		Path file = createFile(basePath + "/tmp.txt");
		FileSystemChangesNIO fileSystemChanges = new FileSystemChangesNIO(basePath, ".doc");

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file.toFile()));
			writer.append("Edited");
			writer.close();
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}

		sleep();

		Assert.assertFalse(fileSystemChanges.hasChanges());
	}
	
	@Test
	public void ignoreNewFileSubdir() {
		Path path = createDir(basePath + "/subdir1/subdir2");
		FileSystemChangesNIO fileSystemChanges = new FileSystemChangesNIO(basePath, ".doc");
		createFile(path + "/subfiletmp.txt");
		sleep();
		Assert.assertFalse(fileSystemChanges.hasChanges());
	}

	@Test
	public void ignoreDeleteFileSubdir() {
		Path path = createDir(basePath + "/subdir1/subdir2");
		Path file = createFile(path + "/subfiletmp.txt");
		FileSystemChangesNIO fileSystemChanges = new FileSystemChangesNIO(basePath, ".doc");
		try {
			Files.deleteIfExists(file);
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
		sleep();
		Assert.assertFalse(fileSystemChanges.hasChanges());
	}

	@Test
	public void ignoreEditFileSubdir() {
		Path path = createDir(basePath + "/subdir1/subdir2");
		Path file = createFile(path + "/subfiletmp.txt");
		FileSystemChangesNIO fileSystemChanges = new FileSystemChangesNIO(basePath, ".doc");

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file.toFile()));
			writer.append("Edited");
			writer.close();
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}

		sleep();

		Assert.assertFalse(fileSystemChanges.hasChanges());
	}

	private void sleep() {
		// TODO SO dependent
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private Path createDir(String dir) {
		try {
			return Files.createDirectories(Paths.get(dir));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Path createFile(String file) {
		try {
			return Files.createFile(Paths.get(file));
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
		return null;
	}
}
