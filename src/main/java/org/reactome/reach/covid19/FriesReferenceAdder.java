package org.reactome.reach.covid19;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.gk.model.Person;
import org.gk.model.Reference;
import org.gk.reach.ReachUtils;
import org.reactome.reach.model.ncbiMetadata.NcbiMetadata;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Create and append Reference data to FRIES files.
 */
public class FriesReferenceAdder {
    public FriesReferenceAdder() {
    }

    /**
     * Fetch the metadata from NCBI for a given paper.
     *
     * @param paperId
     * @return Reference
     * @throws IOException
     */
    public Reference fetchReference(Path paper) throws IOException {
        String filename = paper.getFileName().toString();
        StringBuilder stringBuilder = new StringBuilder(FriesConstants.REFERENCE_URL);

        if (filename.toUpperCase().startsWith(FriesConstants.PMC))
            stringBuilder.append(FriesConstants.PMC_QUERY);

        else if (filename.toUpperCase().startsWith(FriesConstants.PMID))
            stringBuilder.append(FriesConstants.PUBMED_QUERY);

        // TODO Add support for DOI prefix.
        else return null;

        stringBuilder.append(FriesConstants.ID_QUERY);
        // Remove "PMC" or "PMID" from paperId.
        stringBuilder.append(FriesUtils.cleanPaperId(filename));
        stringBuilder.append(FriesConstants.RETMODE);
        stringBuilder.append(FriesConstants.TOOL_EMAIL);

        HttpCaller httpCaller = new HttpCaller();
        String metadata = httpCaller.callHttpGet(URI.create(stringBuilder.toString())).asString();

        NcbiMetadata paperMetadataObj = ReachUtils.readJsonText(metadata, NcbiMetadata.class);
        // Generic Key, Value map from JSON returned by API.
        Map<String, Object> paperData = paperMetadataObj.getResult().getPaperData().get(filename);
        return createReference(paperData);
    }

    /**
     * Given a map of generic JSON Keys and Values, create and return a new Reference.
     *
     * @param paperData
     * @return Reference
     */
    private Reference createReference(Map<String, Object> paperData) {
        // authors
        List<Map<String, String>> authorMaps = (List<Map<String, String>>) paperData.get("authors");
        List<Person> authorList = new ArrayList<Person>();
        List<String> authorName = null;
        for (Map<String, String> authorMap : authorMaps) {
            authorName = Arrays.asList(authorMap.get("name").split(" "));
            Person author = new Person();
            author.setLastName(authorName.get(0));
            if (authorName.size() > 1)
                author.setFirstName(authorName.get(1));
            authorList.add(author);
        }

        // pages
        String pages = (String) paperData.get("pages");

        // pmid, pmcid
        List<Map<String, String>> articleids = (List<Map<String, String>>) paperData.get("articleids");
        long pmid = 0;
        String pmcid = null;
        String idtype = null;
        for (Map<String, String> map : articleids) {
            idtype = map.get("idtype");
            if (idtype.equals("pmid") || idtype.equals("pubmed"))
                pmid = Long.parseLong(map.get("value"));
            else if (idtype.equals("pmcid"))
                pmcid = map.get("value");
        }

        // source (i.e. journal)
        String source = (String) paperData.get("source");

        // title
        String title = (String) paperData.get("title");

        // volume
        String volume = (String) paperData.get("volume");

        // year
        String pubdate = (String) paperData.get("pubdate");
        int year = Integer.parseInt(pubdate.split(" ")[0]);


        // Create and return new Reference.
        Reference reference = new Reference();
        reference.setAuthors(authorList);
        reference.setJournal(source);
        reference.setPage(pages);
        reference.setPmid(pmid);
        reference.setPmcid(pmcid);
        reference.setTitle(title);
        reference.setVolume(volume);
        reference.setYear(year);
        return reference;
    }

    /**
     * Create a new Reference from a given row of the metadata file.
     *
     * @param metadata
     * @return Reference
     */
	public Reference createReference(ScholarMetadata metadata) {
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

	/**
	 * Find and return the contents of the given reference file (e.g. PMC16946.reference.json).
	 *
	 * @param refereneFile
	 * @param referenceDir
	 * @return String
	 * @throws IOException
	 */
	private String getReference(Path refereneFile, Path referenceDir) throws IOException {
	    List<Path> referenceFiles = FriesUtils.getFilesInDir(referenceDir);
	    String referenceId = null;
	    String fileId = FriesUtils.getIdFromPath(refereneFile);

	    for (Path referenceFile : referenceFiles) {
	        referenceId = FriesUtils.getIdFromPath(referenceFile);

	        if (referenceId.equals(fileId))
	            return FriesUtils.readFile(referenceFile);
	    }

        return null;
	}


	/**
	 * Append the references to all papers in the input directory.
	 *
	 * @param inputDir
	 * @param outputDir
	 * @param referenceDir
	 * @throws IOException
	 */
	public void addReferences(Path inputDir, Path outputDir, Path referenceDir) throws IOException {
     String reference = null;
	    String contents = null;
	    String referenceStr = null;
	    Object json = null;
	    ObjectMapper mapper = new ObjectMapper();

	    // Get total number of files to display progress.
        List<Path> friesFiles = FriesUtils.getFilesInDir(inputDir, FriesConstants.JSON_EXT);

        // For all JSON files in the directory.
        for (Path file : friesFiles) {

            reference = getReference(file, referenceDir);
            if (reference == null)
                continue;

            // Add reference to file contents.
            contents = FriesUtils.readFile(file);
            contents = contents.substring(0, contents.lastIndexOf("}"));
            referenceStr = ", \"reference\": " + reference + "}";
            contents = contents.concat(referenceStr);

            // Write file to output directory.
            json = mapper.readValue(contents, Object.class);
            FriesUtils.writeJSONFile(outputDir.resolve(file.getFileName()), json);
        }
	}
}
