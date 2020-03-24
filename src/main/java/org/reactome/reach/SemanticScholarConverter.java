package org.reactome.reach;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactome.reach.model.semanticscholar.Paper;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SemanticScholarConverter {
	// See src/main/resources/log4j2.xml for log4j specific configuration
	private static final Logger logger = LogManager.getLogger("mainLog");
	
	private static Paper getPaper(Path path) throws JsonParseException, JsonMappingException, IOException {
	    ObjectMapper mapper = new ObjectMapper();
	    Paper paper = mapper.readValue(path.toFile(), Paper.class);
	    return paper;
	}

	public static void main(String[] args) {
		logger.info("testing");
		
		Path directory = Paths.get("/Users/beckmanl/dev/covid-19/noncomm_use_subset/conversion");
		try(DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
		    for (Path path : stream) {
		        if (Files.isRegularFile(path)) {
                    Paper paper = getPaper(path);
		        }
		        return;
		    }
		} catch (IOException | DirectoryIteratorException e) {
		    e.printStackTrace();
		}
	}
}
