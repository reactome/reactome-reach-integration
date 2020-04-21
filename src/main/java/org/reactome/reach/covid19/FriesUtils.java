package org.reactome.reach.covid19;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FriesUtils {

    public FriesUtils() {
    }

    /**
     * Read the contents of a file into a String.
     *
     * @param file
     * @return String
     * @throws IOException
     */
    static String readFile(Path file) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }

        return stringBuilder.toString();
    }

    /**
     * Write a JSON object to a given file.
     *
     * @param file
     * @param json
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    static void writeJSONFile(Path file, Object json) throws JsonGenerationException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), json);
    }

    /**
     * Return all files in a given directory.
     *
     * @param dir
     * @return List
     * @throws IOException
     */
	static List<Path> getFilesInDir(Path dir) throws IOException {
	    return getFilesInDir(dir, null);
	}

	/**
	 * Return all files that have a given extension in a directory.
	 *
	 * @param dir
	 * @param filter
	 * @return List
	 * @throws IOException
	 */
	static List<Path> getFilesInDir(Path dir, String filter) throws IOException {
	    List<Path> files = new ArrayList<Path>();
	    try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
	        for (Path path : stream) {
	            if (!Files.isRegularFile(path))
	                continue;

	            if (filter == null || path.toString().endsWith(filter))
                    files.add(path);
	        }
        }
        return files;
	}

    /**
     * Remove "PMC", "PMID", "DOI", and file extensions from the paperId.
     *
     * @param paperId
     * @return String
     */
    static String cleanPaperId(String paperId) {
        List<String> remove = Arrays.asList("[\\D.]",
                                            ".json",
                                            ".fries");
        for (String str : remove)
            paperId = paperId.replaceAll(str, "");

        return paperId;
    }

    /**
     * Extract the PMC/PMID/DOI identifier from a given paper as represented by its Path.
     *
     * e.g. 'DOI10.1136-bmj.m606.events.json' -> 'DOI10.1136-bmj.m606'
     *
     * @param file
     * @return String
     */
    static String getIdFromPath(Path file) {
        String identifier = null;
        String filename = file.getFileName().toString();

        // PMC
        if (filename.startsWith(FriesConstants.PMC))
            identifier = filename;

        // PMID
        else if (filename.startsWith(FriesConstants.PMID))
            identifier = filename.replaceFirst(FriesConstants.PMID, "");

        // DOI
        else if (filename.startsWith(FriesConstants.DOI)) {
            identifier = filename.replaceFirst(FriesConstants.DOI, "");
            identifier = filename.replaceAll("-", "/");
        }

        // SHA
        else {
            identifier = filename;
        }

        List<String> removeStrings = Arrays.asList(".fries",
                                                   ".json",
                                                   ".reference",
                                                   ".xml",
                                                   ".events",
                                                   ".entities",
                                                   ".sentences",
                                                   ".uaz");
        for (String remove : removeStrings)
            identifier = identifier.replaceAll(remove, "");

        return identifier;
    }

    /**
     * Return application's properties.
     *
     * @return Properties
     * @throws IOException
     */
	static Properties getProperties() throws IOException {
	    Properties properties = new Properties();
	    try (InputStream inputStream = FriesUtils.class.getResourceAsStream(FriesConstants.PROPERTY_FILE)) {
	        properties.load(inputStream);
        }

	    return properties;
	}

	/**
	 * Return the root directory that all pipelines and completed files reside.
	 *
	 * @return Path
	 * @throws IOException
	 */
	static Path getRootDir() throws IOException {
	    Path rootDir = null;
	    Properties properties = getProperties();

	    boolean prependHome = Boolean.parseBoolean(properties.getProperty("prependHome"));
	    if (prependHome) {
	        rootDir = Paths.get(System.getProperty("user.home"));
	        rootDir = rootDir.resolve(properties.getProperty("rootDir"));
	    }
	    else
	        rootDir = Paths.get(properties.getProperty("rootDir"));

	    return rootDir;
	}

}
