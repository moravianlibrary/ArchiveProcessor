package cz.mzk.archiveprocessor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author kremlacek
 */
public class AppTest {

    Path testDir;

    Path testIn;
    Path testOut;
    Path testErr;

    //Placeholder test
    @Test
    public void test() {
        assertTrue(true);
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        testDir = new File("./tmp/archiveProcessor-test").toPath();

        if (testDir.toFile().exists()) {
            FileUtils.deleteDirectory(testDir.toFile());
        }

        Files.createDirectory(testDir) ;

        testIn = Files.createDirectory(testDir.resolve("input"));
        testOut = Files.createDirectory(testDir.resolve("output"));
        testErr = Files.createDirectory(testDir.resolve("error"));

        Path sigDir = Files.createDirectory(testIn.resolve("2619158951"));
        Files.createTempFile(sigDir, "img1", ".tiff");
        Files.createTempFile(sigDir, "img2", ".tiff");
    }

    @AfterEach
    public void afterEach() throws IOException {
        FileUtils.deleteDirectory(testDir.toFile());
    }

    @Test
    public void runSimpleArchivation() throws IOException {
        new Processor(testErr.toFile(), testOut.toFile()).processDirectory(testIn.toFile());

        assertTrue(testIn.toFile().listFiles().length == 0, "All images must be processed.");
        assertTrue(testOut.toFile().listFiles().length > 0, "Images must be archived.");
        assertTrue(testErr.toFile().listFiles().length == 0, "No errors can occur.");
    }
}
