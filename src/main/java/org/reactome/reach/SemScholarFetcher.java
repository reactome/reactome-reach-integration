package org.reactome.reach;

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

import org.apache.http.client.fluent.Content;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

public class SemScholarFetcher {

    public SemScholarFetcher() {
    }

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

    private Path fetchFile(URI url, Path outputDir) throws IOException {
        HttpCaller caller = new HttpCaller();
        Content content = caller.callHttpGet(url);
        Path filename = Paths.get(url.getPath()).getFileName();
        Path path = Files.write(outputDir.resolve(filename), content.asBytes());
        return path;
    }

    public static void main(String[] args) throws IOException {
        SemScholarFetcher fetcher = new SemScholarFetcher();

        // Get the Semantic Scholar URL from properties file.
        Properties properties = FriesUtils.getProperties();
        URI semScholarURL = URI.create(properties.getProperty("semScholarURL"));

        // Download the metadata file.
        Path outputDir = FriesUtils.getSemanticScholarDir();
        String metadataURL = properties.getProperty("semScholarMetadata");
        Path metadata = fetcher.fetchFile(semScholarURL.resolve(metadataURL), outputDir);

        // Download the dataset.
        String datasetURL = properties.getProperty("semScholarDataset");
        Path datasetArchive = fetcher.fetchFile(semScholarURL.resolve(datasetURL), outputDir);

        // Extract the archived file.
        fetcher.extractFiles(datasetArchive, outputDir);
    }
}
