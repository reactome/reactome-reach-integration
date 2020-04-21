package org.reactome.reach.covid19;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ScholarFileChecker {
	private final Logger logger = Main.getLogger();

    public ScholarFileChecker() {
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
	private List<ScholarMetadata> createPaperMetadata(Path metadataFile) throws IOException, CsvException {
	    // Get list of "noncomm_use_subset" papers from rows in the metadata file.
        List<ScholarMetadata> papers = new CsvToBeanBuilder<ScholarMetadata>(new FileReader(metadataFile.toString()))
                                             .withType(ScholarMetadata.class)
                                             .withOrderedResults(false)
                                             .withVerifier(new ScholarMetadataFilter())
                                             .build()
                                             .parse();
	    return papers;
	}


	/**
	 * Check metadata against already processed paper ID's.
	 *
	 * @param paperMetadata
	 * @param completedIds
	 * @return List
	 * @throws Exception
	 */
	private List<ScholarMetadata> filterPaperMetadata(List<ScholarMetadata> paperMetadata, Set<String> completedIds) throws Exception {
	    List<ScholarMetadata> filteredMetadata = new ArrayList<ScholarMetadata>();

	    for (ScholarMetadata metadata : paperMetadata) {
	        if (completedIds.contains(metadata.getPmcid()) ||
	            completedIds.contains(metadata.getPmid()) ||
	            completedIds.contains(metadata.getDoi()))
	            continue;

	        filteredMetadata.add(metadata);
	    }

	    return filteredMetadata;
	}

	/**
	 * Filter out all papers (metadata rows) that have already been processed.
	 *
	 * @param metadataFile
	 * @param completedFriesDir
	 * @return List
	 * @throws IOException
	 * @throws Exception
	 */
	public List<ScholarMetadata> getFilteredMetadata(Path metadataFile, Path completedFriesDir) throws IOException, Exception {
		List<ScholarMetadata> paperMetadata = createPaperMetadata(metadataFile);

		Set<String> completedIds = new HashSet<String>();

		for (Path completedFries : FriesUtils.getFilesInDir(completedFriesDir))
		    completedIds.add(FriesUtils.getIdFromPath(completedFries));

		logger.info("Filtering metadata.");
		List<ScholarMetadata> filteredMetadata = filterPaperMetadata(paperMetadata, completedIds);

		logger.info("Number of papers already processed: " + filteredMetadata.size());
		logger.info("Number of new papers: " + filteredMetadata.size());
		return filteredMetadata;
	}

}
