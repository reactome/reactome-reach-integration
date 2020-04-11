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

public class SemScholarFileChecker {

    public SemScholarFileChecker() {
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
	    // Get list of "noncomm_use_subset" papers from rows in the metadata file.
        List<PaperMetadata> papers = new CsvToBeanBuilder<PaperMetadata>(new FileReader(metadataFile.toString()))
                                             .withType(PaperMetadata.class)
                                             .withOrderedResults(false)
                                             .withVerifier(new PaperMetadataFilter())
                                             .build()
                                             .parse();
	    return papers;
	}


	private List<PaperMetadata> filterPaperMetadata(List<PaperMetadata> paperMetadata, Set<String> completedIds) throws Exception {
	    List<PaperMetadata> filteredMetadata = new ArrayList<PaperMetadata>();

	    for (PaperMetadata metadata : paperMetadata) {
	        if (completedIds.contains(metadata.getPmcid()) ||
	            completedIds.contains(metadata.getPmid()) ||
	            completedIds.contains(metadata.getDoi()))
	            continue;

	        filteredMetadata.add(metadata);
	    }

	    return filteredMetadata;
	}
	
	public List<PaperMetadata> getFilteredMetadata(Path metadataFile, Path completedFriesDir) throws IOException, Exception {
		List<PaperMetadata> paperMetadata = createPaperMetadata(metadataFile);

		Set<String> completedIds = new HashSet<String>();

		for (Path completedFries : FriesUtils.getFilesInDir(completedFriesDir))
		    completedIds.add(FriesUtils.getIdFromPath(completedFries));

		List<PaperMetadata> filteredMetadata = filterPaperMetadata(paperMetadata, completedIds);

		return filteredMetadata;
	}

}
