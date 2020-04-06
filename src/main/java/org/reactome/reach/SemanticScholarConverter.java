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

public class SemanticScholarConverter {
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
	
	private Map<String, Metadata> createMetadataObjects(Path path) throws IOException, CsvException {
        Metadata metadataObj = null;
	    Map<String, Metadata> metadataMap = new HashMap<String, Metadata>();
        String shas = null;
        String doi = null;
        String pmcid = null;
        String pmid = null;
	    String[] line;
	    CSVReader reader = new CSVReader(new FileReader(path.toString()));
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
	    reader.close();
        return metadataMap;
	}
	
	private void writeFile(String text, Metadata metadataObj, String dir) throws IOException {
	    StringBuilder filename = new StringBuilder(dir);
	    if (metadataObj.getPmcid() != null && metadataObj.getPmcid().length() > 0)
	        filename.append(metadataObj.getPmcid());
	    else if (metadataObj.getPmid() != null && metadataObj.getPmid().length() > 0)
	        filename.append("PMID").append(metadataObj.getPmid());
	    else if (metadataObj.getDoi() != null && metadataObj.getDoi().length() > 0)
	        filename.append("DOI").append(metadataObj.getDoi().replace("/", "-"));
	    else
	        filename.append(metadataObj.getSha());
	    filename.append(".txt");
	    Path path = Paths.get(filename.toString());

	    try (BufferedWriter writer = Files.newBufferedWriter(path)) {
	        writer.write(text, 0, text.length());
	    }
	}

	public static void main(String[] args) throws IOException, CsvException {
		logger.info("testing");
		String dir = "/Users/beckmanl/dev/covid-19/noncomm_use_subset/conversion/";
		String outputDir = dir + "converted/";
		String metadataFile = "metadata.csv";

		String text = null;
		String filename = null;
		String sha = null;
		Metadata metadataObj = null;
		SemanticScholarConverter converter = new SemanticScholarConverter();
		Map<String, Metadata> metadataMap = converter.createMetadataObjects(Paths.get(dir + metadataFile));
		try(DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir))) {
		    for (Path path : stream) {
		        if (Files.isRegularFile(path) && path.toString().endsWith(".json")) {
		            filename = path.getFileName().toString();
		            sha = filename.substring(0, filename.lastIndexOf("."));
		            metadataObj = metadataMap.get(sha);

		            if (metadataObj == null) continue;
		            text = converter.getText(path);

		            converter.writeFile(text, metadataObj, outputDir);
		        }
		    }
		} 
	}
}
