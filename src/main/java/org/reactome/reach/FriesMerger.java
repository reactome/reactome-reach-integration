package org.reactome.reach;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriesMerger {

    public FriesMerger() {
    }

	private static String readFile(Path path) throws IOException {
	    StringBuilder stringBuilder = new StringBuilder();
	    try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line = null;
            while ((line = reader.readLine()) != null)
                stringBuilder.append(line);
        }
	    return stringBuilder.toString();
	}
	
    
    public static void main(String[] args) throws IOException {
        String inputDir = "/Users/beckmanl/Documents/reach/output/testing/";
        String outputDir = "/Users/beckmanl/Documents/reach/output/testing/output/";
        String identifier = null;
        String pathStr = null;

        // Identifiers mapped to file triplet).
        Map<String, List<Path>> fileMap = new HashMap<String, List<Path>>();
        
        // For all JSON files in the directory.
		try(DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(inputDir))) {
		    for (Path path : stream) {
		        if (!path.toString().endsWith(".json"))
		            continue;

                // Get the identifier (PMC, PMID, DOI).
		        pathStr = path.getFileName().toString();
		        identifier = pathStr.substring(0, pathStr.indexOf("."));

                // Add the identifier and path to file map.
		        if (fileMap.containsKey(identifier))
		            fileMap.get(identifier).add(path);
		        else {
		            List<Path> triplet = new ArrayList<Path>();
		            triplet.add(path);
		            fileMap.put(identifier, triplet);
		        }
		    }
		}
        
		Path path = null;
		String filename = null;
		Path eventFile = null;
		Path entityFile = null;
		Path sentenceFile = null;
		StringBuilder contents = new StringBuilder();
		OpenOption[] options = {StandardOpenOption.CREATE, StandardOpenOption.APPEND};
        // For all files in the map.
		for (Map.Entry<String, List<Path>> map : fileMap.entrySet()) {
		    // Identify the files.
		    for (Path file : map.getValue()) {
		        filename = file.getFileName().toString();
		        if (filename.contains("events"))
		            eventFile = file;
		        else if (filename.contains("entities"))
		            entityFile = file;
		        else if (filename.contains("sentences"))
		            sentenceFile = file;
		    }
		    
            // Merge the triplet by building a new file.
		    contents.append("{ \"events\": " + readFile(eventFile));
		    contents.append(", \"entities\": " + readFile(entityFile));
		    contents.append(", \"sentences\": " + readFile(sentenceFile) + "}");

		    // Write the file to the output directory.
		    path = Paths.get(outputDir + map.getKey() + ".fries.json");
		    try(BufferedWriter writer = Files.newBufferedWriter(path, options)) {
		        writer.write(contents.toString(), 0, contents.length());
		    }
		}
    }
}
