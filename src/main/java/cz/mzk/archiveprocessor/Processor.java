package cz.mzk.archiveprocessor;

import cz.mzk.archiveprocessor.models.Sysno;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * @author kremlacek
 */
public class Processor {

    private static final String ERROR_NOT_DIRECTORY = "Supplied file is not a directory.";
    private static final String ERROR_COULD_NOT_RETREIVE_SYSNO = "Could not find sysno for supplied identifier";
    private static final String ERROR_JAVA_EXCEPTION = "Java error occured.\n";

    private final File errorDirectory;
    private final File archiveDirectory;

    public Processor(File errorDirectory, File archiveDirectory) {
        this.errorDirectory = errorDirectory;
        this.archiveDirectory = archiveDirectory;
    }

    AlephConnector connector = new AlephConnector();

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
     * Moves supplied file into archive directory under the supplied sysno
     *
     * @param file file to be moved
     * @param sysno sysno of the file
     */
    private void moveToArchive(File file, Sysno sysno) throws IOException, TransformerException {

        Path archivePath = archiveDirectory.toPath().resolve(sysno.getBase() + sysno.getNumericalPart());

        //store mets into the package
        TransformerFactory
                .newInstance()
                .newTransformer()
                .transform(
                        new DOMSource(connector.getMemoizedDoc(sysno)),
                        new StreamResult(archivePath.resolve("mets.xml").toFile())
                );

        //copy data into archive
        Files.copy(file.toPath(), archivePath.resolve("data"));

        //delete original, do not use file moving - in case exception occurs during moving, data become inconsistent
        Files.delete(file.toPath());
    }

    /**
     * Moves supplied file into error directory with specified error message
     *
     * @param file file to be moved
     * @param errorMsg explanation of the error
     */
    private void moveToError(File file, String errorMsg) throws IOException {
        //move data into error
        Files.move(file.toPath(), errorDirectory.toPath().resolve(file.getName()));

        //write error message
        File errorFile = new File(errorDirectory, file.getName() + ".error.log");
        try (FileWriter fw = new FileWriter(errorFile)) {
            fw.write(errorMsg);
        }
    }
}
