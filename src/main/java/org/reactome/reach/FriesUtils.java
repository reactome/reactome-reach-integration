package org.reactome.reach;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FriesUtils {
//	private static final Logger logger = LogManager.getLogger("mainLog");

    public FriesUtils() {
    }

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

    static List<String> readFileToList(Path file) throws IOException {
        List<String> list = new ArrayList<String>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
        }

        return list;
    }

	static void writeFile(Path file, String contents) throws IOException {
	    try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
	        writer.write(contents.toString(), 0, contents.length());
	    }
	}

    static void appendFile(Path file, String str) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file,
                                                             StandardCharsets.UTF_8,
                                                             StandardOpenOption.APPEND)) {
            writer.write(str, 0, str.length());
        }
    }
    
    static void writeJSONFile(Path file, Object json) throws JsonGenerationException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper(); 
        mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), json);
    }

	static List<Path> getFilesInDir(Path dir) throws IOException {
	    return getFilesInDir(dir, null);
	}

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
            
        identifier = identifier.replaceAll(".json", "");
        identifier = identifier.replaceAll(".xml", "");

        return identifier;
    }


    static void showCurentFile(Path file) {
        String str = file.toString();
//	    logger.info(str);
	}

	static void showSuccesfulFile(Path file, int current, int total) {
	    String str = file.toString() + " -> OK ( " + current + " / " + total + " ) ";
//	    logger.info(str);
	}

	static Path getSemanticScholarDir() throws IOException {
	    return getCurrentDir().resolve("semantic-scholar");
	}

	static Path getReachDir() throws IOException {
	    return getCurrentDir().resolve("reach");
	}

	static Path getFriesDir() throws IOException {
	    return getCurrentDir().resolve("fries");
	}

	static Path getCurrentDir() throws IOException {
	    return getRootDir().resolve("current");
	}

	static Path getCompletedDir() throws IOException {
	    return getRootDir().resolve("fries-completed");
	}

	static Path getCacheDir() throws IOException {
	    return getRootDir().resolve("cache");
	}

	static Properties getProperties() throws IOException {
	    Properties properties = new Properties();
	    Path propertiesFile = Paths.get(FriesConstants.PROPERTY_FILE);

	    try (BufferedReader reader = Files.newBufferedReader(propertiesFile)) {
	        properties.load(reader);
        }

	    return properties;
	}

	static Path getRootDir() throws IOException {
	    Properties properties = getProperties();

	    StringBuilder path = new StringBuilder();
        path.append(System.getProperty("user.home"));
        path.append(System.getProperty("file.separator"));
	    path.append(properties.get("rootDir"));
        path.append(System.getProperty("file.separator"));
	    Path rootDir = Paths.get(path.toString());
	    return rootDir;
	}
}
