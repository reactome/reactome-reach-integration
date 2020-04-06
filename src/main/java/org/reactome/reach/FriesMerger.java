package org.reactome.reach;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriesMerger {

    public FriesMerger() {
    }

    public static void main(String[] args) throws IOException {
        String inputDir = "/Users/beckmanl/Documents/reach/output-diff/";
        String outputDir = "/Users/beckmanl/Documents/reach/output-diff/merged/";
        // Organization
        String org = "uaz";
        String identifier = null;
        String pathStr = null;

        // Identifiers mapped to file triplet).
        Map<String, List<Path>> fileMap = new HashMap<String, List<Path>>();
        
        // For all JSON files in the directory.
        for (Path path : FriesUtils.getFilesInDir(Paths.get(inputDir))) {
            if (!path.toString().endsWith(".json"))
                continue;

            // Get the identifier (PMC, PMID, DOI).
            pathStr = path.getFileName().toString();
            // PMC3908835.uaz.fries.json -> PMC3908835
            identifier = pathStr.substring(0, pathStr.indexOf(org) - 1);

            // Add the identifier and path to file map.
            if (fileMap.containsKey(identifier))
                fileMap.get(identifier).add(path);
            else {
                List<Path> triplet = new ArrayList<Path>();
                triplet.add(path);
                fileMap.put(identifier, triplet);
            }
        }

        Path path = null;
		String filename = null;
        // For all files in the map.
		for (Map.Entry<String, List<Path>> map : fileMap.entrySet()) {
		    Path eventFile = null;
		    Path entityFile = null;
		    Path sentenceFile = null;
		    StringBuilder contents = new StringBuilder();

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
		    contents.append("{ \"events\": " + FriesUtils.readFile(eventFile));
		    contents.append(", \"entities\": " + FriesUtils.readFile(entityFile));
		    contents.append(", \"sentences\": " + FriesUtils.readFile(sentenceFile) + "}");

		    // Write the file to the output directory.
		    path = Paths.get(outputDir + map.getKey() + ".fries.json");
		    FriesUtils.writeFile(contents.toString(), path);
		}
    }
}
