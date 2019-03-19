package cz.mzk.archiveprocessor;

import cz.mzk.archiveprocessor.models.Sysno;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

/**
 * @author kremlacek
 */
public class Processor {

    private static final Logger LOG = Logger.getLogger(Processor.class.getName());

    private static final String ERROR_NOT_DIRECTORY = "Supplied file is not a directory.";
    private static final String ERROR_COULD_NOT_RETREIVE_SYSNO = "Could not find sysno for supplied identifier.";
    private static final String ERROR_JAVA_EXCEPTION = "Java error occured during moving images to archive.";
    private static final String ERROR_NOT_VALID_IMAGE_DIRECTORY = "Supplied directory must contain only images.";

    private static final List<String> SUPPORTED_IMAGE_TYPES = Arrays.asList(".pdf", ".tiff", ".jpg", ".jp2");

    private final File errorDirectory;
    private final File archiveDirectory;
    private final AlephConnector connector;

    public Processor(AppConfiguration cfg) {
        this(cfg, new AlephConnector());
    }

    public Processor(AppConfiguration cfg, AlephConnector connector) {
        this(cfg.getErrorDirectory(), cfg.getOutputDirectory(), connector);
    }

    public Processor(File errorDirectory, File archiveDirectory) {
        this(errorDirectory, archiveDirectory, new AlephConnector());
    }

    public Processor(File errorDirectory, File archiveDirectory, AlephConnector connector) {
        this.errorDirectory = errorDirectory;
        this.archiveDirectory = archiveDirectory;
        this.connector = connector;
    }

    /**
     * Attempt to determine sysno from a given identifier. Returns null if could not be loaded.
     *
     * @param identifier valid identifier (sysno, barcode, signature)
     * @return
     */
    public Sysno loadSysno(String identifier) {
        Sysno sys;

        if (Sysno.isValidSysno(identifier)) {
            return Sysno.createSysnoFromString(identifier);
        }

        //identifier not sysno, try to load via barcode
        sys = connector.getSysnoFromBarcode(identifier);

        //barcode failed, try signature
        if (sys == null) {
            sys = connector.getSysnoFromSignature(identifier);
        }

        return sys;
    }

    /**
     * Processes input directory, which must contain directories identified by allowed identifier type
     *
     * @param directory
     */
    public void processDirectory(File directory) throws IOException {
        if (directory == null) {
            throw new IllegalArgumentException("directory cannot be null");
        }

        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(directory + " is not a directory");
        }

        for (File file : directory.listFiles()) {
            if (!file.isDirectory()) {
                moveToError(file, ERROR_NOT_DIRECTORY);
            }

            if (!isValidImageDirectory(file)) {
                moveToError(file, ERROR_NOT_VALID_IMAGE_DIRECTORY);
            }

            Sysno s = loadSysno(file.getName());

            if (s == null) {
                moveToError(file, ERROR_COULD_NOT_RETREIVE_SYSNO);
            }

            try {
                moveToArchive(file, s);
            } catch (Exception e) {
                moveToError(file, ERROR_JAVA_EXCEPTION + " " + e.getMessage());
            }
        }
    }

    /**
     * Checks whether target directory contains only supported image types
     *
     * @param directory directory to be checked
     */
    private boolean isValidImageDirectory(File directory) {

        for(File f : directory.listFiles()) {
            //contents must be files
            if (!f.isFile()) {
                return false;
            }

            //contents must be supported image types
            if (!SUPPORTED_IMAGE_TYPES.contains(f.getName().substring(f.getName().lastIndexOf(".")))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Moves supplied file into archive directory under the supplied sysno
     *
     * @param file file to be moved
     * @param sysno sysno of the file
     */
    private void moveToArchive(File file, Sysno sysno) throws IOException, TransformerException {

        //get base directory
        Path archivePath = resolveArchivePath(sysno);

        //versioning
        for (int i = 0;true;i++) {
            Path versionedPath = archivePath.resolve("v" + i);

            if (!versionedPath.toFile().exists()) {
                archivePath = versionedPath;
                break;
            }
        }

        //copy data into archive
        FileUtils.copyDirectory(file, archivePath.resolve("data").toFile());

        //store mets into the package
        TransformerFactory
                .newInstance()
                .newTransformer()
                .transform(
                        new DOMSource(connector.getMemoizedDoc(sysno)),
                        new StreamResult(archivePath.resolve("data").resolve("mets.xml").toFile())
                );

        //create MD5 hashes file
        processAndHashFileContents(archivePath.resolve("manifest-md5.txt").toFile(), archivePath.resolve("data").toFile());
        //create bagit file
        createBagItFile(archivePath.resolve("bagit.txt").toFile());

        //delete original, do not use file moving - in case exception occurs during moving, data become inconsistent
        FileUtils.deleteDirectory(file);
    }

    /**
     * Creates bagit textfile according to the specification
     *
     * @param toFile
     */
    private void createBagItFile(File toFile) throws IOException {
        FileWriter fw = new FileWriter(toFile, true);
        fw.write("BagIt-Version: 1.0\n");
        fw.write("Tag-File-Character-Encoding: UTF-8");
        fw.close();
    }

    /**
     * Creates file containing md5 hashes of files within supplied directory
     *
     * @param hashFile target file to contain hashes of data files
     * @param dataDir directory containing files to be hashed
     */
    private void processAndHashFileContents(File hashFile, File dataDir) throws IOException {
        if (hashFile.exists()) {
            throw new IllegalArgumentException("HashFile can not exist. See: " + hashFile.getPath());
        }

        FileWriter fw = new FileWriter(hashFile, true);

        processAndHashFileContents(fw, dataDir, "");

        fw.close();
    }

    private void processAndHashFileContents(FileWriter hashFileWriter, File file, String path) throws IOException {
        if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                processAndHashFileContents(
                        hashFileWriter,
                        subFile,
                        path + (path.isEmpty() ? "" : "/") + subFile.getName()); //path string starts without slash
            }
        } else {
            String hash = DigestUtils.md5Hex(Files.newInputStream(file.toPath()));
            hashFileWriter.write(hash + " " + path + "\n");
        }
    }

    /**
     * Moves supplied file into error directory with specified error message
     *
     * @param file file to be moved
     * @param errorMsg explanation of the error
     */
    private void moveToError(File file, String errorMsg) throws IOException {
        LOG.warning("Images not archived. Reason: " + errorMsg);
        //move data into error
        Files.move(file.toPath(), errorDirectory.toPath().resolve(file.getName()));

        //write error message
        File errorFile = new File(errorDirectory, file.getName() + ".error.log");
        try (FileWriter fw = new FileWriter(errorFile)) {
            fw.write(errorMsg);
        }
    }

    /**
     * Resolves path in archive for the particular sysno. This resolution can be institution-specific.
     * Default implementation is according to MZK, but can be easily overriden for other institution needs.
     *
     * @param sysno sysno to be resolved into path
     * @return resolved path within archiveDirectory
     */
    protected Path resolveArchivePath(Sysno sysno) {
        Path archivePath = archiveDirectory.toPath()
                .resolve(Integer.toString(Calendar.getInstance().get(Calendar.YEAR)))
                .resolve(sysno.getBase().toString());

        //get numerical directory names
        String[] numericalPart = sysno.getNumericalPart().split("(?<=\\G.{3})");

        //resolve numerical part
        for (String part : numericalPart) {
            archivePath = archivePath.resolve(part);
        }

        return archivePath;
    }
}
