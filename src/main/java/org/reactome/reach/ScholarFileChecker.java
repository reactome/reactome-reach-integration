package org.reactome.reach;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvException;

public class ScholarFileChecker {

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
                                             .withVerifier(new PaperMetadataFilter())
                                             .build()
                                             .parse();
	    return papers;
	}


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
	
	public List<ScholarMetadata> getFilteredMetadata(Path metadataFile, Path completedFriesDir) throws IOException, Exception {
		List<ScholarMetadata> paperMetadata = createPaperMetadata(metadataFile);

		Set<String> completedIds = new HashSet<String>();

		for (Path completedFries : FriesUtils.getFilesInDir(completedFriesDir))
		    completedIds.add(FriesUtils.getIdFromPath(completedFries));

		List<ScholarMetadata> filteredMetadata = filterPaperMetadata(paperMetadata, completedIds);

		return filteredMetadata;
	}

}
