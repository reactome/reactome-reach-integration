package org.reactome.reach;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FriesMerger {

    public FriesMerger() {
    }

    private String getIdentifier(Path path) throws IOException {
        String identifier = null;

        // Get the identifier (PMC, PMID, DOI).
        String pathStr = path.getFileName().toString();
        if (pathStr.startsWith(FriesConstants.PMC) || pathStr.startsWith(FriesConstants.PMID)) {
            // PMC3908835.events.json -> PMC3908835
            identifier = pathStr.split("\\.")[0];
        }
        else if (pathStr.startsWith(FriesConstants.DOI)) {
            // DOI10.1136-bmj.m606.events.json -> DOI10.1136-bmj.m606.
            List<String> list = Arrays.asList(pathStr.split("\\."));

            // Remove "events" and "json".
            list = list.subList(0, list.size() - 2);
            identifier = String.join(".", list);
        }
        else
            throw new IOException();
        
        return identifier;
    }
    
    private Map<String, List<Path>> getTripletMap(Path dir) throws IOException {
        String identifier = null;

        // Identifiers mapped to file triplet).
        Map<String, List<Path>> fileMap = new HashMap<String, List<Path>>();

        // For all JSON files in the directory.
//        for (Path path : FriesUtils.getFilesInDir(dir, FriesConstants.JSON_EXT)) {
        for (Path path : FriesUtils.getFilesInDir(FriesUtils.getCacheDir().resolve("output"), FriesConstants.JSON_EXT)) {
            if (!path.toString().endsWith(FriesConstants.JSON_EXT))
                continue;

            identifier = getIdentifier(path);

            // Add the identifier and path to file map.
            if (fileMap.containsKey(identifier))
                fileMap.get(identifier).add(path);
            else {
                List<Path> triplet = new ArrayList<Path>();
                triplet.add(path);
                fileMap.put(identifier, triplet);
            }
        }
        
        return fileMap;
    }

    private Object mergeJson(String identifier, List<Path> files) throws IOException {
        Path eventFile = null;
        Path entityFile = null;
        Path sentenceFile = null;
        String filename = null;

        // Identify the files.
        for (Path file : files) {
            filename = file.getFileName().toString();
            if (filename.contains(FriesConstants.EVENTS))
                eventFile = file;
            else if (filename.contains(FriesConstants.ENTITIES))
                entityFile = file;
            else if (filename.contains(FriesConstants.SENTENCES))
                sentenceFile = file;
        }
        
        if (eventFile == null || entityFile == null || sentenceFile == null)
            throw new IOException();

        // Merge the triplet by building a new file.
        StringBuilder contents = new StringBuilder();
        contents.append("{ \"events\": " + FriesUtils.readFile(eventFile));
        contents.append(", \"entities\": " + FriesUtils.readFile(entityFile));
        contents.append(", \"sentences\": " + FriesUtils.readFile(sentenceFile) + "}");

		ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(contents.toString(), Object.class);
    }

    public void merge() throws IOException {
        Path reachDir = FriesUtils.getReachDir();
        Path inputDir = reachDir.resolve(FriesConstants.OUTPUT);

        Path friesDir = FriesUtils.getFriesDir();
        Path outputDir = friesDir.resolve(FriesConstants.MERGED);

        // Identifiers mapped to file triplet).
        Map<String, List<Path>> fileMap = getTripletMap(inputDir);
        
        Path path = null;
        String filename = null;
		Object json = null;
        // For all files in the map.
		for (Map.Entry<String, List<Path>> map : fileMap.entrySet()) {
		    json = mergeJson(map.getKey(), map.getValue());
		    // Write the file to the output directory.
		    filename = map.getKey().concat(FriesConstants.FRIES_EXT).concat(FriesConstants.JSON_EXT);
		    path = Paths.get(outputDir.toString(), filename);
		    FriesUtils.writeJSONFile(path, json);
		}
    }
}
