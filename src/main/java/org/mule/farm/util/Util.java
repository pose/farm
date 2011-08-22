package org.mule.farm.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Util {
	public static void createFolder(String name) {
		File folder = new File("." + File.separator + name);
		folder.mkdir();
	}

	public static void removeFolder(String folderName) {
		deleteDirectory(new File("." + File.separator + folderName));
	}

	public static boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	public static void copy(String origin, String destiny) {
		File inputFile = new File(origin);
		File outputFile = new File(destiny);

		FileReader in = null;
		FileWriter out = null;

		try {
			in = new FileReader(inputFile);
			out = new FileWriter(outputFile);

			int c;

			while ((c = in.read()) != -1)
				out.write(c);

			in.close();
			out.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}

	}

	public static void move(String origin, String destiny) {
		new File(origin).renameTo(new File(destiny));
	}

}
