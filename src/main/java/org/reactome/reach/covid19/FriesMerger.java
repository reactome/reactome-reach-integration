package org.reactome.reach.covid19;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FriesMerger {
	private static final Logger logger = LogManager.getLogger("mainLog");

    public FriesMerger() {
    }

    /**
     * Return a list of Identifiers mapped to the respective Event, Entity, and Sentence triplet.
     *
     * @param inputDir
     * @return
     * @throws IOException
     */
    private Map<String, List<Path>> getTripletMap(Path inputDir) throws IOException {
        String identifier = null;

        // Identifiers mapped to file triplet).
        Map<String, List<Path>> fileMap = new HashMap<String, List<Path>>();

        // For all JSON files in the directory.
        for (Path path : FriesUtils.getFilesInDir(inputDir, FriesConstants.JSON)) {
            if (!path.toString().endsWith(FriesConstants.JSON))
                continue;

            identifier = FriesUtils.getIdFromPath(path);

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

    /**
     * Merge an Event, Entity, and Sentence triplet into one file.
     *
     * @param identifier
     * @param files
     * @return
     * @throws IOException
     */
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

    /**
     * Merge each Event, Entity, and Sentence triplet returned by REACH into single files.
     *
     * @param inputDir
     * @param outputDir
     * @throws IOException
     */
    public void merge(Path inputDir, Path outputDir) throws IOException {
        // Identifiers mapped to file triplet).
        Map<String, List<Path>> fileMap = getTripletMap(inputDir);

        Path path = null;
        String filename = null;
		Object json = null;
        // For all files in the map.
		for (Map.Entry<String, List<Path>> map : fileMap.entrySet()) {
		    json = mergeJson(map.getKey(), map.getValue());
		    // Write the file to the output directory.
		    filename = map.getKey().concat(FriesConstants.FRIES).concat(FriesConstants.JSON);
		    path = Paths.get(outputDir.toString(), filename);
		    FriesUtils.writeJSONFile(path, json);
		}

        int numMerged = inputDir.toFile().list().length;
		logger.info("Merged " + numMerged + "files.");
    }
}
