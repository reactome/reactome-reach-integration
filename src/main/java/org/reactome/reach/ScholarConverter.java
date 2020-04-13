package org.reactome.reach;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gk.model.Reference;
import org.json.*;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Converts JSON files in Semantic Scholar data sets to a plain text format
 * digestible by REACH.
 */
public class ScholarConverter {
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
	    if (paper.has("abstract"))
            text.append(getTextFromJSON(paper.getJSONArray("abstract"), "text"));
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

	private Path findFile(Path dir, ScholarMetadata metadata) throws Exception {
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
	
	private String createFilename(ScholarMetadata metadata) {
	    String filename = new String();
	    //PMC
	    if (metadata.getPmcid() != null && metadata.getPmcid().length() > 0) 
	        filename = metadata.getPmcid();

	    // PMID
	    else if (metadata.getPmid() != null && metadata.getPmid().length() > 0)
	        filename = "PMID" + metadata.getPmid();

	    // DOI
	    else if (metadata.getDoi() != null && metadata.getDoi().length() > 0)
	        filename = "DOI" + metadata.getDoi().replaceAll("/", "-"); 
	    
	    return filename;
	}

	private Path findFile(Path dir, String id) throws Exception {
	    for (Path file : FriesUtils.getFilesInDir(dir)) {
	        if (id.equals(FriesUtils.getIdFromPath(file)))
	            return file;
	    }
	    
	    return null;
	}
	
	private void createReferenceFile(Path outputFile, ScholarMetadata metadata)
	        throws JsonGenerationException, JsonMappingException, IOException {
	    FriesReferenceAdder referenceAdder = new FriesReferenceAdder();
	    
	    // Create Reference from metadata.
	    Reference reference = referenceAdder.createReference(metadata);
	    FriesUtils.writeJSONFile(outputFile, reference);
	}
	
	private Path convertParsedFile(Path inputDir, Path outputDir, ScholarMetadata metadata) throws Exception {
	    // Find the JSON file corresponding to the "paper".
	    Path jsonFile = findFile(inputDir, metadata);

	    // path
	    String filename = jsonFile.getFileName().toString();
	    filename = filename.replaceAll(".xml", "");
	    filename = filename.replaceAll(".json", "");
	    Path path = outputDir.resolve(filename);

	    // contents
	    String contents = getTextFromPaper(jsonFile);

	    // Output the plain text file to the output directory.
	    Path jsonPath = Paths.get(path.toString().concat(".txt"));
	    FriesUtils.writeFile(jsonPath, contents);
	    
	    return Paths.get(filename);
	}
	
	private String identifyUnparsedFile(Path inputDir, Path outputDir, ScholarMetadata metadata) throws Exception {
	    // content
	    StringBuilder contentStr = new StringBuilder();

	    // For each SHA file corresponding to the "paper".
	    int i = 0;
	    Path jsonFile = null;
	    String filename = null;
	    for (String sha : metadata.getShas()){
	        // Find the file.
	        jsonFile = findFile(inputDir, sha);
	        filename = createFilename(metadata);

	        // Copy the SHA file to the identified directory and rename with PMC/PMID/DOI id.
	        Path target = outputDir.resolve(filename.concat(".txt"));
	        if (Files.exists(target)) {
	            String appendeum = filename.concat("-" + (i++) + ".txt");
	            target = outputDir.resolve(appendeum);
	        }

	        Files.copy(jsonFile, target);

	        // Add text contents to the output string.
	        contentStr.append(getTextFromPaper(jsonFile));
	    }
	    
	    return contentStr.toString();
	}
	
	private Path convertUnparsedFile(Path inputDir, Path outputDir, ScholarMetadata metadata) throws Exception {
	    String content = identifyUnparsedFile(inputDir, outputDir, metadata);

	    // Output the plain text file to the output directory.
	    String filename = createFilename(metadata);
	    Path txtPath = outputDir.resolve(filename.concat(".txt"));
	    FriesUtils.writeFile(txtPath, content);
	    
	    return Paths.get(filename);
	}

	public void convert(Path inputDir, Path outputDir, Path friesCompletedDir) throws Exception {
		// Metadata CSV file.
		Path metadataFile = inputDir.resolve("metadata.csv");

		// Extracted noncommercial data set.
		Path nonCommDir = inputDir.resolve("noncomm_use_subset");
		
		// Papers parsed into 'PMC<ID>.xml.json'.
		Path pmcJsonDir = nonCommDir.resolve("pmc_json");

		// Papers not been parsed (still '<SHA>.json').
		Path pdfJsonDir = nonCommDir.resolve("pdf_json");

		// Papers matched to their PMC/PMID/DOI id's by comparing checksums in the metadata file.
		Path pdfJsonIdentifiedDir = nonCommDir.resolve("pdf_json_identified");
        Files.createDirectory(pdfJsonIdentifiedDir);

		// Output directory for references created from the metadata file.
		Path referencesDir = nonCommDir.resolve("references");
        Files.createDirectory(referencesDir);
		
		// FRIES files already processed by the pipeline.
        // Used to check which files in the new dataset may be safely skipped.
		ScholarFileChecker checker = new ScholarFileChecker();
        List<ScholarMetadata> paperMetadata = checker.getFilteredMetadata(metadataFile, friesCompletedDir);

        Path outputFile = null;
        
        // For all the CSV rows in the filtered metadata.
        for (ScholarMetadata metadata : paperMetadata) {
            
            // If the paper has already been parsed into "PMC<ID>.xml.json".
            if (metadata.getHas_pmc_xml_parse()) {
                outputFile = referencesDir.resolve(convertParsedFile(pmcJsonDir, outputDir, metadata) + ".reference.json");
                createReferenceFile(outputFile, metadata);
            }
            
            // If the paper has not been parsed (still "<SHA>.json").
            else if (metadata.getHas_pdf_parse()) {
                outputFile = referencesDir.resolve(convertUnparsedFile(pdfJsonDir, outputDir, metadata) + ".reference.json");
                createReferenceFile(outputFile, metadata);
            }
            
            else {
                // Log failed file. 
            }
        }
	}
}
