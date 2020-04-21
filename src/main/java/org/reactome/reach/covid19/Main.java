package org.reactome.reach.covid19;

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
	// See src/main/resources/log4j2.xml for log4j specific configuration.
	private final static Logger logger = LogManager.getLogger("mainLog");

	public Main() {
	}

	public static Logger getLogger() {
	    return logger;
	}

    private void makeDirectories(Path path) throws IOException {
        // Pipelines directory.
        Path pipelinesDir = path.resolve("pipelines");
        if (!Files.exists(pipelinesDir))
            Files.createDirectory(pipelinesDir);

        // Create time stamp.
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String timestamp = date.format(formatter);

        // root directory (time stamped)
        Path pipelineDir = pipelinesDir.resolve(timestamp);
        Files.createDirectory(pipelineDir);

        // Semantic Scholar directory
        Path semScholarDir = pipelineDir.resolve("semantic-scholar");
        Files.createDirectory(semScholarDir);

        // REACH directory
        Path reachDir = pipelineDir.resolve("reach");
        Files.createDirectory(reachDir);
        Files.createDirectory(reachDir.resolve("papers"));
        Files.createDirectory(reachDir.resolve("output"));

        // FRIES directory
        Path friesDir = pipelineDir.resolve("fries");
        Files.createDirectory(friesDir);
        Files.createDirectory(friesDir.resolve("merged"));
        Files.createDirectory(friesDir.resolve("filtered"));
        Files.createDirectory(friesDir.resolve("referenced"));

        // "current" directory (symbolic link)
        Path linkedDir = path.resolve("current");
        if (Files.isSymbolicLink(linkedDir))
            Files.delete(linkedDir);
        Files.createSymbolicLink(linkedDir, pipelineDir);

        // Completed FRIES files.
        Path friesCompletedDir = path.resolve("fries-completed");
        if (!Files.exists(friesCompletedDir))
            Files.createDirectory(friesCompletedDir);
    }

	public static void main(String[] args) throws Exception {
	    // Root directory.
	    Main main = new Main();

	    main.makeDirectories(FriesUtils.getRootDir());
	    Path currentDir = FriesUtils.getRootDir().resolve("current");

	    // Semantic Scholar directories.
        Path semScholarDir = currentDir.resolve("semantic-scholar");
        Properties properties = FriesUtils.getProperties();
        URI semScholarURL = URI.create(properties.getProperty("semScholarURL"));
        Path semScholarReferencesDir = semScholarDir.resolve("noncomm_use_subset").resolve("references");

        // REACH directories.
		Path reachPapersDir = currentDir.resolve("reach/papers");
        String home = System.getProperty("user.home");
        Path reachCodeDir = Paths.get(home).resolve("dev/reach");

        // FRIES directories.
        Path reachOutputDir = currentDir.resolve("reach/output");
        Path friesMergedDir = currentDir.resolve("fries/merged");
        Path friesFilteredDir = currentDir.resolve("fries/filtered");
        Path friesReferencedDir = currentDir.resolve("fries/referenced");
        Path friesCompletedDir = FriesUtils.getRootDir().resolve("fries-completed");
        Path friesCompletedArchive = FriesUtils.getRootDir().resolve("fries-completed.tar.gz");

        // https://semanticscholar.org -> semantic-scholar
		logger.info("Fetching dataset and metadata file from Semantic Scholar.");
		ScholarFetcher fetcher = new ScholarFetcher();
		fetcher.fetch(semScholarURL, semScholarDir);

		// semantic-scholar -> reach/papers
		logger.info("Converting JSON files to text files.");
		ScholarConverter converter = new ScholarConverter();
		converter.convert(semScholarDir, reachPapersDir, friesCompletedDir);

		// reach/papers -> reach/output
		logger.info("Running REACH on text files.");
		ReachRunner runner = new ReachRunner();
		runner.runReach(reachCodeDir);

		// reach/output -> fries/merged
		logger.info("Merging FRIES files.");
		FriesMerger merger = new FriesMerger();
		merger.merge(reachOutputDir, friesMergedDir);

		// fries/merged -> fries/filtered
		logger.info("Filtering FRIES files.");
		FriesFilter filter = new FriesFilter();
		filter.filter(friesMergedDir, friesFilteredDir);

		// fries/filtered -> fries/referenced
		logger.info("Adding references to FRIES files.");
		FriesReferenceAdder adder = new FriesReferenceAdder();
		adder.addReferences(friesFilteredDir, friesReferencedDir, semScholarReferencesDir);

        // fries/referenced -> fries-completed
		logger.info("Exporting FRIES files.");
		FriesExporter exporter = new FriesExporter();
		exporter.export(friesReferencedDir, friesCompletedDir);
		exporter.archiveFiles(friesCompletedDir, friesCompletedArchive);
	}
}
