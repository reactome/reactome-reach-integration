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
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.json.*;
import com.opencsv.*;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvException;

/**
 * Converts JSON files in Semantic Scholar data sets to a plain text format
 * digestible by REACH.
 */
public class SemScholarConverter {
	// See src/main/resources/log4j2.xml for log4j specific configuration
	private static final Logger logger = LogManager.getLogger("mainLog");

	/**
	 * Return the abstract and body text from a given paper.
	 *
	 * @param paper
	 * @return String
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private String getTextFromPaper(Path file) throws FileNotFoundException, IOException {
	    StringBuilder text = new StringBuilder();
	    JSONObject paper = new JSONObject(FriesUtils.readFile(file));
//	    text.append(getTextFromJSON(paper.getJSONArray("abstract"), "text"));
	    text.append(getTextFromJSON(paper.getJSONArray("body_text"), "text"));
        return text.toString();
	}

	/**
	 * Return the value of a given JSON arary's "field" .
	 *
	 * @param jsonArray
	 * @return String
	 */
	private String getTextFromJSON(JSONArray jsonArray, String field) {
	    StringBuilder text = new StringBuilder();
	    for (Object obj : jsonArray) {
	         JSONObject jsonObject = (JSONObject) obj;
	         text.append(jsonObject.get(field));
	    }
	    return text.toString();
	}


	private void jsonToTextFile(Path jsonFile, Path outputDir, PaperMetadata metadata) throws IOException {
	    String filename = jsonFile.getFileName().toString();
	    filename = filename.replaceAll(".xml", "");
	    filename = filename.replaceAll(".json", ".txt");

	    Path path = outputDir.resolve(Paths.get(filename));

	    String contents = getTextFromPaper(jsonFile);

	    FriesUtils.writeFile(path, contents);
	}

	private Map<Path,String> createFilesFromJson(Path inputDir, Path outputDir) {
	    Map<Path, String> fileMap = new HashMap<Path, String>();

	    return fileMap;
	}

	private PaperMetadata matchChecksum(Path shaFile, List<PaperMetadata> paperMetadata)
	        throws IOException, CsvException {
	    String filename = null;
	    String sha = null;

	    Map<Path,PaperMetadata> fileMap = new HashMap<Path,PaperMetadata>();

	    // Extract checksum from filename.
	    filename = shaFile.getFileName().toString();
	    sha = filename.substring(0, filename.lastIndexOf("."));

	    // Search for the extracted checksum value in the list of paper metadata.
	    for (PaperMetadata metadata : paperMetadata) {
	        if (metadata.getSha().equals(sha)) {
	            return metadata;
	        }
	    }

	    return null;
	}

	private Path findFile(Path dir, PaperMetadata metadata) throws Exception {
	    String filename = null;

	    for (Path file : FriesUtils.getFilesInDir(dir)) {
	        filename = FriesUtils.getIdFromPath(file);
	        if (metadata.getPmcid().equals(filename) ||
	            metadata.getPmcid().equals(filename) ||
	            metadata.getPmcid().equals(filename)) {
	            return file;
	        }
	    }
	    
	    return null;
	}

	public static void main(String[] args) throws Exception {
	    // semantic-scholar/
		Path semanticScholarDir = FriesUtils.getSemanticScholarDir();
		
		// noncomm_use_subset/
		Path nonCommDir = semanticScholarDir.resolve("noncomm_use_subset");
		
		// reach/papers/
		Path outputDir = FriesUtils.getReachDir().resolve(Paths.get("papers"));

		// pdf_json_identified/
		Path pdfJsonDir = nonCommDir.resolve(Paths.get("pdf_json"));
	
		// pdf_json_identified/
		Path pdfJsonIdentifiedDir = nonCommDir.resolve(Paths.get("pdf_json_converted"));

		// pmc_json/
		Path pmcJsonDir = nonCommDir.resolve(Paths.get("pmc_json"));
		
		// Metadata CSV file.
		Path metadataFile = semanticScholarDir.resolve(Paths.get("metadata.csv"));
		
		// completed-fries/
		Path completedFriesDir = FriesUtils.getCompletedDir();

		SemScholarFileChecker checker = new SemScholarFileChecker();
        List<PaperMetadata> paperMetadata = 	checker.getFilteredMetadata(metadataFile, completedFriesDir);

		SemScholarConverter converter = new SemScholarConverter();
        Path jsonFile = null;
   
        // For all the "paper" in the filtered metadata.
        for (PaperMetadata metadata : paperMetadata) {
            if (metadata.getHas_pmc_xml_parse()) {
                // Find the JSON file corresponding to the "paper".
                jsonFile = converter.findFile(pmcJsonDir, metadata);

                // Output the plain text file to the output directory.
                converter.jsonToTextFile(jsonFile, outputDir, metadata);
            }
            
            else if (metadata.getHas_pdf_parse()) {
                // Find the SHA file corresponding to the "paper".
                jsonFile = converter.findFile(pdfJsonDir, metadata);
                
                // Copy the SHA file to the identified directory and rename with PMC/PMID/DOI id.
                StringBuilder path = new StringBuilder();

                //PMC
                if (metadata.getPmcid() != null) 
                    path.append("PMC" + metadata.getPmcid());
                
                // PMID
                if (metadata.getPmid() != null)
                    path.append("PMID" + metadata.getPmid());
                
                // DOI
                if (metadata.getDoi() != null)
                    path.append("DOI" + metadata.getDoi().replaceAll("/", "-"));
                
                path.append(".json");
                
                Path target = pdfJsonIdentifiedDir.resolve(path.toString());
                Files.copy(jsonFile, target);

                // Output the plain text file to the output directory.
                converter.jsonToTextFile(target, outputDir, metadata);
            }
            
            else {
                // Log failed file. 
            }
        }
	}
}
