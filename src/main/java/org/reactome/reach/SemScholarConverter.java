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

	/**
	 * Return a map of 'SHA' checksums to their respective paper's metadata.
	 *
	 * metadata file must be a CSV file. For values separated by other characters, see
     * <a href="http://opencsv.sourceforge.net/#configuration">Opencsv Configuration</a>.
	 *
	 * @param metadataFile
	 * @return List
	 * @throws IOException
	 * @throws CsvException
	 */
	private List<PaperMetadata> createPaperMetadata(Path metadataFile) throws IOException, CsvException {
	    @SuppressWarnings("unchecked")
	    // Get list of "noncomm_use_subset" papers from rows in the metadata file.
        List<PaperMetadata> papers = new CsvToBeanBuilder(new FileReader(metadataFile.toString()))
                                             .withType(PaperMetadata.class)
                                             .withOrderedResults(false)
                                             .withVerifier(new PaperMetadataFilter())
                                             .build()
                                             .parse();

        return papers;
	}

	private Map<Path, String> jsonToTextFile(Path jsonFile, PaperMetadata metadata, Path outputDir) throws IOException {
	    StringBuilder filename = new StringBuilder();

	    // PMC
	    if (metadata.getPmcid() != null && metadata.getPmcid().length() > 0)
	        filename.append(metadata.getPmcid());

	    // PMID
	    else if (metadata.getPmid() != null && metadata.getPmid().length() > 0)
	        filename.append("PMID").append(metadata.getPmid());

	    // DOI
	    else if (metadata.getDoi() != null && metadata.getDoi().length() > 0)
	        filename.append("DOI").append(metadata.getDoi().replace("/", "-"));

	    // Checksum
	    else
	        filename.append(metadata.getSha());

	    filename.append(".txt");
	    Path path = outputDir.resolve(Paths.get(filename.toString()));

	    String contents = getTextFromPaper(jsonFile);

	    Map<Path, String> file = new HashMap<Path, String>();
	    file.put(path, contents);

	    return file;
	}

	private Map<Path, String> createFilesFromChecksums(Path metadataFile, Path semanticScholarDir, Path outputDir)
	        throws IOException, CsvException {
		String filename = null;
		String sha = null;
		SemScholarConverter converter = new SemScholarConverter();

	    List<PaperMetadata> papers = converter.createPaperMetadata(metadataFile);
	    List<Path> jsonFiles = FriesUtils.getFilesInDir(semanticScholarDir, FriesConstants.JSON_EXT);

	    Map<Path, String> fileMap = new HashMap<Path, String>();

	    for (Path jsonFile : jsonFiles) {
	        // Extract checksum from filename.
	        filename = jsonFile.getFileName().toString();
	        sha = filename.substring(0, filename.lastIndexOf("."));

	        for (PaperMetadata metadata : papers) {
	            if (metadata.getSha().equals(sha)) {
	                fileMap.putAll(converter.jsonToTextFile(jsonFile, metadata, outputDir));
	                break;
	            }
	        }
	    }

	    return fileMap;
	}

	private Map<Path,String> createFilesFromJson(Path inputDir, Path outputDir) {
	    Map<Path, String> fileMap = new HashMap<Path, String>();

	    return fileMap;
	}

	public static void main(String[] args) throws IOException, CsvException {
	    // Directories
		Path semanticScholarDir = FriesUtils.getSemanticScholarDir();
		Path nonCommDir = semanticScholarDir.resolve("noncomm_use_subset");
		Path metadataFile = nonCommDir.resolve(Paths.get("metadata.csv"));
		Path outputDir = FriesUtils.getReachDir().resolve(Paths.get("papers"));
		SemScholarConverter converter = new SemScholarConverter();
		Map<Path, String> fileMap = new HashMap<Path, String>();

		Path checksumDir = nonCommDir.resolve(Paths.get("pdf_json"));
		fileMap.putAll(converter.createFilesFromChecksums(metadataFile, checksumDir, outputDir));

		Path jsonDir = nonCommDir.resolve(Paths.get("pmc_json"));
		fileMap.putAll(converter.createFilesFromJson(jsonDir, outputDir));

		Path path = null;
		String contents = null;
		for (Map.Entry<Path, String> file : fileMap.entrySet()) {
		    path = file.getKey();
		    contents = file.getValue();
		    try (BufferedWriter writer = Files.newBufferedWriter(path)) {
		        writer.write(contents, 0, contents.length());
		    }
		}
	}
}
