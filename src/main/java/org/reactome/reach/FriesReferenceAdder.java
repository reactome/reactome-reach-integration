package org.reactome.reach;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.gk.model.Person;
import org.gk.model.Reference;
import org.gk.reach.ReachUtils;
import org.gk.reach.model.paperMetadata.PaperMetadata;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FriesReferenceAdder {
    public FriesReferenceAdder() {
    }

	public Reference createReference(scholarMetadata metadata) {
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
	
	private String getReference(Path file, Path referenceDir) throws IOException {
	    List<Path> referenceFiles = FriesUtils.getFilesInDir(referenceDir);
	    String referenceId = null;
	    String fileId = FriesUtils.getIdFromPath(file);

	    for (Path referenceFile : referenceFiles) {
	        referenceId = FriesUtils.getIdFromPath(referenceFile);

	        if (referenceId.equals(fileId))
	            return FriesUtils.readFile(referenceFile);
	    }
	    
        return null;
	}

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
