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
        Path rootDir = path.resolve(timestamp);
        Files.createDirectory(rootDir);
        
        // Semantic Scholar directory
        Path semScholarDir = rootDir.resolve("semantic-scholar");
        Files.createDirectory(semScholarDir);
        
        // REACH directory
        Path reachDir = rootDir.resolve("reach");
        Files.createDirectory(reachDir);
        Files.createDirectory(reachDir.resolve("papers"));
        Files.createDirectory(reachDir.resolve("output"));
        
        // FRIES directory
        Path friesDir = rootDir.resolve("fries");
        Files.createDirectory(friesDir);
        Files.createDirectory(friesDir.resolve("merged"));
        Files.createDirectory(friesDir.resolve("filtered"));
        Files.createDirectory(friesDir.resolve("references"));

        // "current" directory (symbolic link)
        Path linkedDir = path.resolve("current");
        if (Files.isSymbolicLink(linkedDir))
            Files.delete(linkedDir);
        Files.createSymbolicLink(linkedDir, rootDir);
    }

	public static void main(String[] args) throws Exception {
	    Main main = new Main();
	    main.makeDirectories(FriesUtils.getRootDir());

		logger.info("Fetching dataset from Semantic Scholar ...");
		SemScholarFetcher fetcher = new SemScholarFetcher();
//		fetcher.fetch();
        
		logger.info("Converting JSON files to text files ...");
		SemScholarConverter converter = new SemScholarConverter();
//		converter.convert();
		
		logger.info("Runing REACH on text files ...");
		// TODO From REACH code directory, run:
		//   sbt 'run-main org.clulab.processors.server.ProcessorServer'
		//   sbt 'run-main org.clulab.reach.RunReachCLI' 
		
		logger.info("Merging FRIES files ...");
		FriesMerger merger = new FriesMerger();
		merger.merge();
		
		logger.info("Filtering FRIES files ...");
		FriesFilter filter = new FriesFilter();
		filter.filter();
	}
}
