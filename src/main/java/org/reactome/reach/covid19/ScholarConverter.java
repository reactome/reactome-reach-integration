package org.reactome.reach.covid19;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gk.model.Reference;
import org.json.*;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Converts JSON files in Semantic Scholar datasets to a plain text format
 * digestible by REACH.
 */
public class ScholarConverter {
	private final Logger logger = Main.getLogger();

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
	 * Return the value of a given JSON arary's "field".
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

	/**
	 *
	 *
	 * @param dir
	 * @param metadata
	 * @return Path
	 * @throws Exception
	 */
	private Path findFile(Path dir, ScholarMetadata metadata, String extension) throws Exception {
	    // PMC
	    Path path = dir.resolve(metadata.getPmcid() + extension);

	    // PMID
	    if (!path.toFile().exists()) {
	        path = dir.resolve("PMID" + metadata.getPmid() + extension);

            // DOI
            if (!path.toFile().exists())
                path = dir.resolve("DOI" + metadata.getPmid().replaceAll("/", "-") + extension);
	    }

	    return path;
	}

	/**
	 * Create a filename from a paper's metadata with a given file extension.
	 *
	 * @param metadata
	 * @return String
	 */
	private String createFilename(ScholarMetadata metadata, String extension) {
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

	    return filename.concat(extension);
	}

	/**
	 * Write a reference file for a give paper/metadata.
	 *
	 * e.g. PMC1616946.json -> PMC1616946.reference.json
	 *
	 * @param outputFile
	 * @param metadata
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	private void createReferenceFile(Path outputFile, ScholarMetadata metadata)
	        throws JsonGenerationException, JsonMappingException, IOException {
	    FriesReferenceAdder referenceAdder = new FriesReferenceAdder();

	    // Create Reference from metadata.
	    Reference reference = referenceAdder.createReference(metadata);
	    FriesUtils.writeJSONFile(outputFile, reference);
	}

	/**
	 * Convert a parsed JSON file (e.g. 'PMC1616946.json') to plain text.
	 *
	 * @param inputDir
	 * @param outputDir
	 * @param metadata
	 * @return Path
	 * @throws Exception
	 */
	private Path convertParsedFile(Path inputFile, Path outputDir) throws Exception {
	    // path
	    String filename = inputFile.getFileName().toString();
	    filename = filename.replaceAll(".xml", "");
	    filename = filename.replaceAll(".json", "");
	    Path path = outputDir.resolve(filename);

	    // contents
	    String contents = getTextFromPaper(inputFile);

	    // Output the plain text file to the output directory.
	    Path outputFile = Paths.get(path.toString().concat(".txt"));
	    writeFile(outputFile, contents);

	    return outputFile;
	}

	/**
	 * Match an unparsed file (e.g. '<SHA>.json') to it's equivalent file (i.e. "<PMC/PMID/DOI>.txt").
	 *
	 * @param inputDir
	 * @param outputDir
	 * @param metadata
	 * @return List
	 * @throws Exception
	 */
	private List<Path> identifyUnparsedFile(Path inputDir, Path outputDir, ScholarMetadata metadata) throws Exception {
	    List<Path> identifiedFiles = new ArrayList<Path>();

	    // For each SHA file corresponding to the "paper".
	    int i = 0;
	    Path jsonFile = null;
	    String filename = null;
	    for (String sha : metadata.getShas()){
	        // Find the file.
	        jsonFile = inputDir.resolve(sha + ".json");
	        filename = createFilename(metadata, ".json");

	        // Copy the SHA file to the identified directory and rename with PMC/PMID/DOI id.
	        Path target = outputDir.resolve(filename);
	        if (Files.exists(target)) {
	            String addendum = filename.replaceAll("(.json)", "-" + (i++) + "$1");
	            target = outputDir.resolve(addendum);
	        }

	        Files.copy(jsonFile, target);

	        // Add identified file to the list.
	        identifiedFiles.add(target);
	    }

	    return identifiedFiles;
	}

	/**
	 * Convert an unparsed file (e.g. '<SHA>.json') to plain text.
	 *
	 * @param inputDir
	 * @param outputDir
	 * @param metadata
	 * @return Path
	 * @throws Exception
	 */
	private Path convertUnparsedFile(List<Path> inputFiles, Path outputDir) throws Exception {
	    StringBuilder content = new StringBuilder();

	    for (Path inputFile : inputFiles)
	        content.append(getTextFromPaper(inputFile));

	    // Output the plain text file to the output directory.
	    String inputFilename = inputFiles.get(0).getFileName().toString();
	    String filename = inputFilename.replaceAll(".json", ".txt");
	    Path outputFile = outputDir.resolve(filename);
	    writeFile(outputFile, content.toString());

	    return outputFile;
	}

    /**
     * Write a given String to a given file.
     *
     * @param file
     * @param contents
     * @throws IOException
     */
	private void writeFile(Path file, String contents) throws IOException {
	    try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
	        writer.write(contents.toString(), 0, contents.length());
	    }
	}

	/**
	 * Convert papers from JSON to plain text.
	 *
	 * @param inputDir
	 * @param outputDir
	 * @param friesCompletedDir
	 * @throws Exception
	 */
	public void convert(Path inputDir, Path outputDir, Path friesCompletedDir) throws Exception {
		// Metadata CSV file.
		Path metadataFile = inputDir.resolve("metadata.csv");

		// Extracted noncommercial dataset.
		Path nonCommDir = inputDir.resolve("noncomm_use_subset");

		// Papers parsed into 'PMC<ID>.xml.json'.
		Path pmcJsonDir = nonCommDir.resolve("pmc_json");

		// Papers not been parsed (still '<SHA>.json').
		Path pdfJsonDir = nonCommDir.resolve("pdf_json");

		// Papers matched to their PMC/PMID/DOI id's by comparing checksums in the metadata file.
		Path identifiedDir = nonCommDir.resolve("pdf_json_identified");
		if (!Files.exists(identifiedDir))
            Files.createDirectory(identifiedDir);

		// Output directory for references created from the metadata file.
		Path referencesDir = nonCommDir.resolve("references");
		if (!Files.exists(referencesDir))
            Files.createDirectory(referencesDir);

		// Filter out FRIES files have already been processed by the pipeline in order to
        // determine which files in the dataset may be safely skipped.
		ScholarFileChecker checker = new ScholarFileChecker();
        List<ScholarMetadata> paperMetadata = checker.getFilteredMetadata(metadataFile, friesCompletedDir);

        Path parsedFile = null;
        List<Path> unparsedFiles = null;
        Path convertedFile = null;
        String refFilename = null;

        // For all the CSV rows in the filtered metadata.
        for (ScholarMetadata metadata : paperMetadata) {
            if (!metadata.getHas_pmc_xml_parse() && !metadata.getHas_pdf_parse()) {
                // Log failed file.
                StringBuilder noPaperFoundErr = new StringBuilder();
                noPaperFoundErr.append("No file found for paper ->");
                noPaperFoundErr.append(" title: '" + metadata.getTitle() + "',");
                noPaperFoundErr.append(" doi: " + metadata.getDoi());
                logger.warn(noPaperFoundErr.toString());
                continue;
            }

            // If the paper has already been parsed into "PMC<ID>.xml.json".
            if (metadata.getHas_pmc_xml_parse()) {
                parsedFile = findFile(pmcJsonDir, metadata, ".xml.json");
                convertedFile = convertParsedFile(parsedFile, outputDir);
            }

            // If the paper has not been parsed (still "<SHA>.json").
            else {
                unparsedFiles = identifyUnparsedFile(pdfJsonDir, identifiedDir, metadata);
                convertedFile = convertUnparsedFile(unparsedFiles, outputDir);
            }

            refFilename = convertedFile.getFileName().toString().replaceAll(".txt", ".reference.json");
            createReferenceFile(referencesDir.resolve(refFilename), metadata);
        }

        int numIdentified = identifiedDir.toFile().list().length;
        int numConverted = outputDir.toFile().list().length;
        logger.info("Identfied " + numIdentified + " files.");
        logger.info("Converted " + numConverted + " files.");
	}
}
