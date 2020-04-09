package org.reactome.reach;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.json.*;
import com.opencsv.*;
import com.opencsv.exceptions.CsvException;

/**
 * Converts JSON files in Semantic Scholar data sets to a plain text format
 * digestible by REACH.
 */
public class SemScholarConverter {
	// See src/main/resources/log4j2.xml for log4j specific configuration
	private static final Logger logger = LogManager.getLogger("mainLog");
	
	private String getText(Path path) throws FileNotFoundException, IOException {
	    StringBuilder text = new StringBuilder();
	    JSONObject paper = new JSONObject(FriesUtils.readFile(path));
	    text.append(extractText(paper.getJSONArray("abstract")));
	    text.append(extractText(paper.getJSONArray("body_text")));
        return text.toString();
	}
	
	private String extractText(JSONArray jsonArray) {
	    StringBuilder text = new StringBuilder();
	    for (Object obj : jsonArray) {
	         JSONObject bodyText = (JSONObject) obj;
	         text.append(bodyText.get("text"));
	    }
	    return text.toString();
	}
	
	private Map<String, Metadata> createMetadataObjects(Path metadataFile) throws IOException, CsvException {
        String shas = null;
        String doi = null;
        String pmcid = null;
        String pmid = null;
	    String[] line;
        Metadata metadataObj = null;
	    Map<String, Metadata> metadataMap = new HashMap<String, Metadata>();

	    try (CSVReader reader = new CSVReader(new FileReader(metadataFile.toString()))) {
	        while ((line = reader.readNext()) != null) {
	            shas = line[0];
	            doi = line[3];
	            pmcid = line[4];
	            pmid = line[5];

	            for (String sha : shas.split("; ")) {
	                metadataObj = new Metadata(sha, doi, pmcid, pmid);
	                metadataMap.put(sha, metadataObj);
	            }
	        }
	    }
        return metadataMap;
	}
	
	private void createTextFile(String contents, Path outputDir, Metadata metadataObj) throws IOException {
	    StringBuilder filename = new StringBuilder();

	    // PMC
	    if (metadataObj.getPmcid() != null && metadataObj.getPmcid().length() > 0)
	        filename.append(metadataObj.getPmcid());

	    // PMID
	    else if (metadataObj.getPmid() != null && metadataObj.getPmid().length() > 0)
	        filename.append("PMID").append(metadataObj.getPmid());
	    
	    // DOI
	    else if (metadataObj.getDoi() != null && metadataObj.getDoi().length() > 0)
	        filename.append("DOI").append(metadataObj.getDoi().replace("/", "-"));

	    // Checksum
	    else
	        filename.append(metadataObj.getSha());

	    filename.append(".txt");

	    Path path = outputDir.resolve(Paths.get(filename.toString()));

	    try (BufferedWriter writer = Files.newBufferedWriter(path)) {
	        writer.write(contents, 0, contents.length());
	    }
	}

	public static void main(String[] args) throws IOException, CsvException {
	    // Directories
		Path semanticScholarDir = FriesUtils.getSemanticScholarDir();
		Path metadataFile = semanticScholarDir.resolve(Paths.get("metadata.csv"));
		Path outputDir = FriesUtils.getReachDir().resolve(Paths.get("papers"));

		// Variables
		String text = null;
		String filename = null;
		String sha = null;
		Metadata metadataObj = null;
		SemScholarConverter converter = new SemScholarConverter();
		Map<String, Metadata> metadataMap = converter.createMetadataObjects(metadataFile);

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(semanticScholarDir)) {
		    // For all files in the input directory.
		    for (Path path : stream) {
		        if (Files.isRegularFile(path) && path.toString().endsWith(".json")) {
		            // Extract checksum from filename.
		            filename = path.getFileName().toString();
		            sha = filename.substring(0, filename.lastIndexOf("."));
		            metadataObj = metadataMap.get(sha);

		            if (metadataObj == null) continue;

		            text = converter.getText(path);

		            converter.createTextFile(text, outputDir, metadataObj);
		        }
		    }
		} 
	}
}
