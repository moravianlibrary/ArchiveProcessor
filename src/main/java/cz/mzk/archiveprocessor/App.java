package cz.mzk.archiveprocessor;

import cz.mzk.archiveprocessor.processor.MZKProcessor;
import java.io.IOException;

/**
 * @author kremlacek
 */
public class App {

    public static void main(String[] args) {
        run(args);
    }

    private static void run(String[] args) {
        var cfg = AppConfiguration.getConfiguration(args);

        if (cfg == null) {
            System.exit(1);
        }

        var processor = new MZKProcessor(cfg);

        try {
            processor.processDirectory(cfg.getInputDirectory());
        } catch (IOException e) {
            throw new IllegalStateException("Processing input directory failed. Reason: " + e.getMessage());
        }
    }
}
