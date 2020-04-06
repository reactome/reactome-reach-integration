package org.reactome.reach;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FriesUtils {
    public FriesUtils() {
    }
    
	static String readFile(Path file) throws IOException {
	    StringBuilder stringBuilder = new StringBuilder();
	    try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line = null;
            while ((line = reader.readLine()) != null)
                stringBuilder.append(line);
        }
	    return stringBuilder.toString();
	}
	
	static List<Path> getFilesInDir(Path dir) throws IOException {
	    List<Path> files = new ArrayList<Path>();
	    try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
	        for (Path path : stream)
	            files.add(path);
        }
        return files;
	}
	
	static void writeFile(String contents, Path file) throws IOException {
	    try (BufferedWriter writer = Files.newBufferedWriter(file)) {
	        writer.write(contents.toString(), 0, contents.length());
	    }
	}
	
	static String getProgress(String filename, int current, int total) {
	    return filename + " -> OK ( " + current + " / " + total + " ) ";
	}
}
