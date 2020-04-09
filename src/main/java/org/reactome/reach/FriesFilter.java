package org.reactome.reach;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.gk.reach.ReachUtils;
import org.gk.reach.model.fries.Event;
import org.gk.reach.model.fries.FriesObject;

public class FriesFilter {

    public FriesFilter() {
    }

    public static void main(String[] args) throws Exception {
        Path friesDir = FriesUtils.getFriesDir();
        // Try to follow some standard Maven project practice.
        // Provide application.properties.
        Path inputDir = friesDir.resolve(Paths.get(FriesConstants.MERGED));
        Path outputDir = friesDir.resolve(Paths.get(FriesConstants.FILTERED));
        
        List<Path> friesFiles = FriesUtils.getFilesInDir(inputDir, FriesConstants.JSON_EXT);

        // For all JSON FRIES files (in the input directory).
        for (Path friesFile : friesFiles) {
            // Check if FriesFile has required information (at least one event).
            String inputJson = FriesUtils.readFile(friesFile);
            FriesObject friesObject = ReachUtils.readJsonText(inputJson);
            List<Event> events = friesObject.getEvents().getFrameObjects();
            if (events.size() == 0)
                continue;
            
            boolean needIt = false;
            for (Event event : events) {
                if (event.getArguments().size() > 1) {
                    needIt = true;
                    break;
                }
            }
            
            if (needIt) {
                Files.copy(friesFile, outputDir.resolve(friesFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
