package org.reactome.reach;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

public class FriesExporter {
    public FriesExporter() {
    }
    
    /**
     * Compress 'completed' directory to be ready for uploading/sharing.
     * 
     * @param inputDir
     * @param outputFile
     * @throws IOException
     */
    public void archiveFiles(Path inputDir, Path outputFile) throws IOException {
        List<Path> inputFiles = FriesUtils.getFilesInDir(inputDir);

        try (OutputStream outputStream = Files.newOutputStream(outputFile);
             OutputStream gzipOutputStream = new GzipCompressorOutputStream(outputStream);
             ArchiveOutputStream tarOutputStream = new TarArchiveOutputStream(gzipOutputStream)) {
            for (Path file : inputFiles) {
                ArchiveEntry entry = tarOutputStream.createArchiveEntry(file.toFile(), file.getFileName().toString());
                tarOutputStream.putArchiveEntry(entry);
                if (file.toFile().isFile()) {
                    try (InputStream inputStream = Files.newInputStream(file)) {
                        IOUtils.copy(inputStream, tarOutputStream);
                    }
                }
                tarOutputStream.closeArchiveEntry();
            }
            tarOutputStream.finish();
        }
    }
    
    public void export(Path inputDir, Path outputDir) throws IOException {
        // Copy new FRIES files to 'completed' directory. 
        List<Path> friesFiles = FriesUtils.getFilesInDir(inputDir);
        Path target = null;
        for (Path friesFile : friesFiles) {
            target = outputDir.resolve(friesFile.getFileName());
            if (!Files.exists(target))
                Files.copy(friesFile, target);
        }
    }

}
