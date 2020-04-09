package org.reactome.reach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
	// See src/main/resources/log4j2.xml for log4j specific configuration
	private static final Logger logger = LogManager.getLogger("mainLog");

    private void makeDirectories(Path path) throws IOException {
        // Create time stamp.
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String timestamp = date.format(formatter);

        // root directory (time stamped)
        Path rootDir = path.resolve(Paths.get(timestamp));
        Files.createDirectory(rootDir);
        
        // Semantic Scholar directory
        Path semScholarDir = rootDir.resolve(Paths.get("semantic-scholar"));
        Files.createDirectory(semScholarDir);
        
        // REACH directory
        Path reachDir = rootDir.resolve(Paths.get("reach"));
        Files.createDirectory(reachDir);
        Files.createDirectory(reachDir.resolve(Paths.get("papers")));
        Files.createDirectory(reachDir.resolve(Paths.get("output")));
        
        // FRIES directory
        Path friesDir = rootDir.resolve(Paths.get("fries"));
        Files.createDirectory(friesDir);
        Files.createDirectory(friesDir.resolve(Paths.get("merged")));
        Files.createDirectory(friesDir.resolve(Paths.get("filtered")));
        Files.createDirectory(friesDir.resolve(Paths.get("references")));

        // "current" directory (symbolic link)
        Path linkedDir = path.resolve(Paths.get("current"));
        if (Files.isSymbolicLink(linkedDir))
            Files.delete(linkedDir);
        Files.createSymbolicLink(linkedDir, rootDir);
    }

	public static void main(String[] args) throws Exception {
	    Main main = new Main();
	    main.makeDirectories(FriesUtils.getRootDir());

		logger.info("SemScholarFetcher ...");
		SemScholarFetcher fetcher = new SemScholarFetcher();
//		fetcher.fetch();
        
		logger.info("SemScholarConverter ...");
		SemScholarConverter converter = new SemScholarConverter();
//		converter.convert();
		
		logger.info("REACH ...");
		// TODO From REACH code directory, run:
		//   sbt 'run-main org.clulab.processors.server.ProcessorServer'
		//   sbt 'run-main org.clulab.reach.RunReachCLI' 
		
		logger.info("FriesMerger ...");
		FriesMerger merger = new FriesMerger();
//		merger.merge();
		
		logger.info("FriesFilter ...");
		FriesFilter filter = new FriesFilter();
//		filter.filter();

		logger.info("ReferenceFetch ...");
		FriesReferenceAdder referenceAdder = new FriesReferenceAdder();
//		referenceAdder.addReferences();
	}
}
