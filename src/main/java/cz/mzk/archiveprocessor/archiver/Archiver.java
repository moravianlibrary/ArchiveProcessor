package cz.mzk.archiveprocessor.archiver;

import cz.mzk.archiveprocessor.AppConfiguration;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;

/**
 * @author kremlacek
 */
public class Archiver {
    private static final Logger LOG = Logger.getLogger(Archiver.class.getName());

    private final File outputDirectory;

    public Archiver(AppConfiguration cfg) {
        outputDirectory = cfg.getOutputDirectory();
    }

    public void run(File inputDirectory) {
        if (inputDirectory == null) {
            throw new IllegalArgumentException("inputDirectory cannot be null");
        }

        if (!inputDirectory.exists()) {
            throw new IllegalArgumentException("inputDirectory must exist");
        }

        try {
            compressZipfile(inputDirectory, outputDirectory.toPath().resolve(inputDirectory.getName()).toFile());
        } catch (IOException e) {
            LOG.severe("Directory " + inputDirectory.getName() + " could not be processed. Reason: " + e.getMessage());
        }
    }

    public static void compressZipfile(File sourceDir, File outputFile) throws IOException {
        ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(outputFile));
        compressDirectoryToZipfile(sourceDir, sourceDir, zipFile);
        zipFile.close();
    }

    private static void compressDirectoryToZipfile(File rootDir, File sourceDir, ZipOutputStream out) throws IOException {
        for (File file : sourceDir.listFiles()) {
            if (file.isDirectory()) {
                compressDirectoryToZipfile(rootDir, sourceDir.toPath().resolve(file.getName()).toFile(), out);
            } else {
                ZipEntry entry = new ZipEntry(sourceDir.getPath().replace(rootDir.getPath(), "") + File.separator + file.getName());
                out.putNextEntry(entry);

                FileInputStream in = new FileInputStream(file);
                IOUtils.copy(in, out);
                in.close();
            }
        }
    }
}
