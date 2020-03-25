package org.reactome.reach;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.json.*;
import com.opencsv.*;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;

public class SemanticScholarConverter {
	// See src/main/resources/log4j2.xml for log4j specific configuration
	private static final Logger logger = LogManager.getLogger("mainLog");
	
	private static String getText(Path path) throws FileNotFoundException, IOException {
	    StringBuilder text = new StringBuilder();
	    JSONObject paper = new JSONObject(readFile(path));
	    text.append(extractText(paper.getJSONArray("abstract")));
	    text.append(extractText(paper.getJSONArray("body_text")));
        return text.toString();
	}
	
	private static String extractText(JSONArray jsonArray) {
	    StringBuilder text = new StringBuilder();
	    for (Object obj : jsonArray) {
	         JSONObject bodyText = (JSONObject) obj;
	         text.append(bodyText.get("text"));
	    }
	    return text.toString();
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
	
	private static List<Metadata> createMetadataObjects(Path path) throws IOException, CsvException {
	    CSVReader reader = new CSVReader(new FileReader(path.toString()));
	    List<String[]> myEntries = reader.readAll();
	    List<Metadata> metadata = new ArrayList<Metadata>();
        return metadata;
	}

	public static void main(String[] args) throws IOException, CsvException {
		logger.info("testing");
		
		Path directory = Paths.get("/Users/beckmanl/dev/covid-19/noncomm_use_subset/conversion");
		Path metadataPath = Paths.get("/Users/beckmanl/dev/covid-19/noncomm_use_subset/conversion/metadata.csv");
		List<Metadata> metadata = createMetadataObjects(metadataPath);
		/*
		try(DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
		    String text = null;
		    for (Path path : stream) {
		        if (Files.isRegularFile(path) && path.endsWith(".json")) {
		            text = getText(path);
		        }
		        return;
		    }
		} catch (IOException | DirectoryIteratorException e) {
		    e.printStackTrace();
		}
		*/
	}
}
