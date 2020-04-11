package org.reactome.reach;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gk.model.Reference;
import org.json.*;

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
	        if (metadata.getShas().contains(sha)) {
	            return metadata;
	        }
	    }

	    return null;
	}

	private Path findFile(Path dir, PaperMetadata metadata) throws Exception {
	    // PMC
	    Path path = findFile(dir, metadata.getPmcid());
	    
	    // PMID
	    if (path == null)
	        path = findFile(dir, metadata.getPmid());
	    
	    // DOI
	    if (path == null)
	        path = findFile(dir, metadata.getDoi());
	    
	    return path;
	}

	private Path findFile(Path dir, String id) throws Exception {
	    for (Path file : FriesUtils.getFilesInDir(dir)) {
	        if (id.equals(FriesUtils.getIdFromPath(file)))
	            return file;
	    }
	    
	    return null;
	}
	
	private Reference createReference(PaperMetadata metadata) {
        // Create and return new Reference.
        Reference reference = new Reference();
        reference.setAuthors(metadata.getAuthors());
        reference.setJournal(metadata.getJournal());

        if (metadata.getPmid() != null && metadata.getPmid().length() > 0)
            reference.setPmid(Long.parseLong(metadata.getPmid()));
        
        reference.setPmcid(metadata.getPmcid());
        reference.setTitle(metadata.getTitle());
        reference.setYear(metadata.getYear());
	    
	    return reference;
	}

	public void convert() throws Exception {
	    // semantic-scholar/
		Path semScholarDir = FriesUtils.getSemanticScholarDir();
		
		// noncomm_use_subset/
		Path nonCommDir = semScholarDir.resolve("noncomm_use_subset");
		
		// reach/papers/
		Path outputDir = FriesUtils.getReachDir().resolve("papers");

		// pdf_json_identified/
		Path pdfJsonDir = nonCommDir.resolve("pdf_json");
	
		// pdf_json_identified/
		Path pdfJsonIdentifiedDir = nonCommDir.resolve("pdf_json_identified");
        Files.createDirectory(pdfJsonIdentifiedDir);

		// pmc_json/
		Path pmcJsonDir = nonCommDir.resolve("pmc_json");
		
		// Metadata CSV file.
		Path metadataFile = semScholarDir.resolve("metadata.csv");
		
		// completed-fries/
		Path completedFriesDir = FriesUtils.getCompletedDir();
		
		// refrences/
		Path referencesPath = nonCommDir.resolve("references");
        Files.createDirectory(referencesPath);

		SemScholarFileChecker checker = new SemScholarFileChecker();
        List<PaperMetadata> paperMetadata = checker.getFilteredMetadata(metadataFile, completedFriesDir);

        Path jsonFile = null;
        Path path = null;
        String contents = null;
        String filename = null;
   
        // For all the "paper" in the filtered metadata.
        for (PaperMetadata metadata : paperMetadata) {
            if (metadata.getHas_pmc_xml_parse()) {
                // Find the JSON file corresponding to the "paper".
                jsonFile = findFile(pmcJsonDir, metadata);

                // path
                filename = jsonFile.getFileName().toString();
                filename = filename.replaceAll(".xml", "");
                filename = filename.replaceAll(".json", "");
                path = outputDir.resolve(filename);

                // contents
                contents = getTextFromPaper(jsonFile);

                // Output the plain text file to the output directory.
                Path jsonPath = Paths.get(path.toString().concat(".txt"));
                FriesUtils.writeFile(jsonPath, contents);
                
                // Create Reference from metadata.
                Reference reference = createReference(metadata);
                Path referencePath = referencesPath.resolve(filename.concat(".reference.json"));
                FriesUtils.writeJSONFile(referencePath, reference);
            }
            
            else if (metadata.getHas_pdf_parse()) {
                // path
                StringBuilder pathStr = new StringBuilder();

                //PMC
                if (metadata.getPmcid() != null && metadata.getPmcid().length() > 0) 
                    pathStr.append(metadata.getPmcid());

                // PMID
                else if (metadata.getPmid() != null && metadata.getPmid().length() > 0)
                    pathStr.append("PMID" + metadata.getPmid());

                // DOI
                else if (metadata.getDoi() != null && metadata.getDoi().length() > 0)
                    pathStr.append("DOI" + metadata.getDoi().replaceAll("/", "-"));

                path = Paths.get(pathStr.toString().concat(".json"));

                // content
                StringBuilder contentStr = new StringBuilder();

                // For each SHA file corresponding to the "paper".
                int i = 0;
                for (String sha : metadata.getShas()){
                    // Find the file.
                    jsonFile = findFile(pdfJsonDir, sha);

                    // Copy the SHA file to the identified directory and rename with PMC/PMID/DOI id.
                    Path target = pdfJsonIdentifiedDir.resolve(pathStr.toString().concat(".json"));
                    if (Files.exists(target)) {
                        String appendeum = pathStr.toString().concat("-" + (i++) + ".json");
                        target = pdfJsonIdentifiedDir.resolve(appendeum);
                    }

                    Files.copy(jsonFile, target);

                    // Add text contents to the output string.
                    contentStr.append(getTextFromPaper(jsonFile));
                }

                // Output the plain text file to the output directory.
                Path txtPath = outputDir.resolve(pathStr.toString().concat(".txt"));
                FriesUtils.writeFile(txtPath, contentStr.toString());

                // Create Reference from metadata.
                Reference reference = createReference(metadata);
                Path referencePath = referencesPath.resolve(pathStr.toString().concat(".reference.json"));
                FriesUtils.writeJSONFile(referencePath, reference);
            }
            
            else {
                // Log failed file. 
            }
        }
	}
}
