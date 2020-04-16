package org.reactome.reach.covid19;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
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

        identifier = identifier.replaceAll(".fries", "");
        identifier = identifier.replaceAll(".json", "");
        identifier = identifier.replaceAll(".reference", "");
        identifier = identifier.replaceAll(".xml", "");

        return identifier;
    }

	static Properties getProperties() throws IOException {
	    Properties properties = new Properties();
	    try (InputStream inputStream = FriesUtils.class.getResourceAsStream(FriesConstants.PROPERTY_FILE)) {
	        properties.load(inputStream);
        }

	    return properties;
	}

	static Path getTestDir() throws IOException {
	    Path testDir = Paths.get(System.getProperty("user.home"));
	    return testDir.resolve(Paths.get("Documents/reach-to-fries-testing"));
	}

	static Path getCurrentDir() throws IOException {
	    return getRootDir().resolve("current");
	}

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
