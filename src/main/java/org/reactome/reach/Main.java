package org.reactome.reach;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

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
        Files.createDirectory(friesDir.resolve("referenced"));

        // "current" directory (symbolic link)
        Path linkedDir = path.resolve("current");
        if (Files.isSymbolicLink(linkedDir))
            Files.delete(linkedDir);
        Files.createSymbolicLink(linkedDir, rootDir);
    }

	public static void main(String[] args) throws Exception {
	    Main main = new Main();
	    
	    // Root directory.
	    main.makeDirectories(FriesUtils.getRootDir());

        // Try to follow some standard Maven project practice.
        // Provide application.properties.
	    
	    // Semantic Scholar directories.
        Path semScholarDir = FriesUtils.getCurrentDir().resolve("semantic-scholar");
        Properties properties = FriesUtils.getProperties();
        URI semScholarURL = URI.create(properties.getProperty("semScholarURL"));
        Path semScholarReferencesDir = semScholarDir.resolve("noncomm_use_subset").resolve("references");

        // REACH directories.
		Path reachPapersDir = FriesUtils.getCurrentDir().resolve("reach/papers");
        String home = System.getProperty("user.home");
        Path reachCodeDir = Paths.get(home).resolve("dev/reach");
        
        // FRIES directories.
        Path reachOutputDir = FriesUtils.getCurrentDir().resolve("reach/output");
        Path friesMergedDir = FriesUtils.getCurrentDir().resolve("fries/merged");
        Path friesFilteredDir = FriesUtils.getCurrentDir().resolve("fries/filtered");
        Path friesReferencedDir = FriesUtils.getCurrentDir().resolve("fries/referenced");
        Path friesCompletedDir = FriesUtils.getRootDir().resolve("fries-completed");
        Path friesCompletedArchive = FriesUtils.getRootDir().resolve("fries-completed.tar.gz");
        
        // https://semanticscholar.org -> semantic-scholar
		logger.info("Fetching dataset from Semantic Scholar ...");
		ScholarFetcher fetcher = new ScholarFetcher();
		fetcher.fetch(semScholarURL, semScholarDir);
		
		// semantic-scholar -> reach/papers
		logger.info("Converting JSON files to text files ...");
		ScholarConverter converter = new ScholarConverter();
		converter.convert(semScholarDir, reachPapersDir, friesCompletedDir);
		
		// reach/papers -> reach/output
		logger.info("Runing REACH on text files ...");
		ReachRunner runner = new ReachRunner();
		runner.runReach(reachCodeDir);
		
		// reach/output -> fries/merged
		logger.info("Merging FRIES files ...");
		FriesMerger merger = new FriesMerger();
		merger.merge(reachOutputDir, friesMergedDir);
		
		// fries/merged -> fries/filtered
		logger.info("Filtering FRIES files ...");
		FriesFilter filter = new FriesFilter();
		filter.filter(friesMergedDir, friesFilteredDir);

		// fries/filtered -> fries/referenced
		logger.info("Adding references to FRIES files ...");
		FriesReferenceAdder adder = new FriesReferenceAdder();
		adder.addReferences(friesFilteredDir, friesReferencedDir, semScholarReferencesDir);

        // fries/referenced -> fries-completed
		logger.info("Exporting FRIES files ...");
		FriesExporter exporter = new FriesExporter();
		exporter.export(friesReferencedDir, friesCompletedDir);
		exporter.archiveFiles(friesCompletedDir, friesCompletedArchive);
	}
}
