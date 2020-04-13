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

public class scholarFileChecker {

    public scholarFileChecker() {
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
	private List<scholarMetadata> createPaperMetadata(Path metadataFile) throws IOException, CsvException {
	    // Get list of "noncomm_use_subset" papers from rows in the metadata file.
        List<scholarMetadata> papers = new CsvToBeanBuilder<scholarMetadata>(new FileReader(metadataFile.toString()))
                                             .withType(scholarMetadata.class)
                                             .withOrderedResults(false)
                                             .withVerifier(new PaperMetadataFilter())
                                             .build()
                                             .parse();
	    return papers;
	}


	private List<scholarMetadata> filterPaperMetadata(List<scholarMetadata> paperMetadata, Set<String> completedIds) throws Exception {
	    List<scholarMetadata> filteredMetadata = new ArrayList<scholarMetadata>();

	    for (scholarMetadata metadata : paperMetadata) {
	        if (completedIds.contains(metadata.getPmcid()) ||
	            completedIds.contains(metadata.getPmid()) ||
	            completedIds.contains(metadata.getDoi()))
	            continue;

	        filteredMetadata.add(metadata);
	    }

	    return filteredMetadata;
	}
	
	public List<scholarMetadata> getFilteredMetadata(Path metadataFile, Path completedFriesDir) throws IOException, Exception {
		List<scholarMetadata> paperMetadata = createPaperMetadata(metadataFile);

		Set<String> completedIds = new HashSet<String>();

		for (Path completedFries : FriesUtils.getFilesInDir(completedFriesDir))
		    completedIds.add(FriesUtils.getIdFromPath(completedFries));

		List<scholarMetadata> filteredMetadata = filterPaperMetadata(paperMetadata, completedIds);

		return filteredMetadata;
	}

}
