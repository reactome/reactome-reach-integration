package org.reactome.reach.covid19;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.gk.reach.ReachUtils;
import org.gk.reach.model.fries.Event;
import org.gk.reach.model.fries.FriesObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FriesFilter {
	private final Logger logger = Main.getLogger();

    public FriesFilter() {
    }

    /**
     * Filter out all FRIES files without at least one Event with two partipants.
     *
     * @param inputDir
     * @param outputDir
     * @throws Exception
     */
    public void filter(Path inputDir, Path outputDir) throws Exception {
        List<Path> friesFiles = FriesUtils.getFilesInDir(inputDir, FriesConstants.JSON);

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

            if (needIt)
                Files.copy(friesFile, outputDir.resolve(friesFile.getFileName()));
        }

        int numFiltered = inputDir.toFile().list().length - outputDir.toFile().list().length;
		logger.info("Filtered " + numFiltered + " file(s).");
    }
}
