package org.reactome.reach.covid19;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScholarFetcher {
	private final Logger logger = Main.getLogger();

    public ScholarFetcher() {
    }

    /**
     * Extract a tar.gz file to a given output directory.
     *
     * @param tarGz
     * @param outputDir
     * @throws IOException
     */
    private void extractFiles(Path tarGz, Path outputDir) throws IOException {
        try (InputStream inputStream = Files.newInputStream(tarGz);
             InputStream buffInputStream = new BufferedInputStream(inputStream);
             InputStream gzipInputStream = new GzipCompressorInputStream(buffInputStream);
             ArchiveInputStream tarInputStream = new TarArchiveInputStream(gzipInputStream)) {

            ArchiveEntry entry = null;
            while ((entry = tarInputStream.getNextEntry()) != null) {
                if (!tarInputStream.canReadEntryData(entry)) {
                    continue;
                }

                File file = new File(outputDir.toFile(), entry.getName());
                if (entry.isDirectory()) {
                   if (!file.isDirectory() && !file.mkdirs()) {
                       throw new IOException("Failed to create directory: " + file);
                   }
                }

                else {
                    File parent = file.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                       throw new IOException("Failed to create directory: " + parent);
                    }
                    try (OutputStream outputStream = Files.newOutputStream(file.toPath())) {
                        IOUtils.copy(tarInputStream, outputStream);
                    }
                }
            }
        }
    }

    /**
     * Download a file from a given URL to the provided output directory.
     *
     * @param url
     * @param outputDir
     * @return Path
     * @throws ClientProtocolException
     * @throws IOException
     */
    private Path fetchFile(URI url, Path outputDir) throws ClientProtocolException, IOException {
        Content content = Request.Get(url).execute().returnContent();
        Path filename = Paths.get(url.getPath()).getFileName();
        Path path = Files.write(outputDir.resolve(filename), content.asBytes());
        return path;
    }

    /**
     * Download the dataset and metadata file from Semantic Scholar.
     *
     * @param inputURL
     * @param outputDir
     * @throws IOException
     */
    public void fetch(URI inputURL, Path outputDir) throws IOException {
        Properties properties = FriesUtils.getProperties();

        // Download the metadata file.
        URI metadataURL = inputURL.resolve(properties.getProperty("semScholarMetadata"));
        fetchFile(metadataURL, outputDir);

        // Download the dataset.
        URI datasetURL = inputURL.resolve(properties.getProperty("semScholarDataset"));
        Path datasetArchive = fetchFile(datasetURL, outputDir);

        // Extract the archived dataset.
        logger.info("Extracting dataset.");
        extractFiles(datasetArchive, outputDir);
    }

}
