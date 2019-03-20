package cz.mzk.archiveprocessor;

import cz.mzk.archiveprocessor.archiver.Archiver;
import cz.mzk.archiveprocessor.processor.MZKProcessor;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author kremlacek
 */
public class App {

    public static void main(String[] args) {
        run(args);
    }

    private static void run(String[] args) {

        if (args.length == 0) {
            System.out.println("Operation must be specified. Use \"archive\" of \"process\"");
            System.exit(1);
        }

        var type = args[0];

        switch (type) {
            case "archive":
                process(AppConfiguration.ConfigType.ARCHIVE, Arrays.copyOfRange(args, 1, args.length));
                break;
            case "process":
                process(AppConfiguration.ConfigType.PROCESS, Arrays.copyOfRange(args, 1, args.length));
                break;
            default:
                System.out.println("Unknown operation: " + type + " . Use archive or process");
                System.exit(1);
        }
    }

    public static void process(AppConfiguration.ConfigType type, String[] args) {
        var cfg = AppConfiguration.getConfiguration(type, args);

        if (cfg == null) {
            System.exit(1);
        }

        switch (type) {
            case PROCESS:
                var processor = new MZKProcessor(cfg);

                try {
                    processor.processDirectory(cfg.getInputDirectory());
                } catch (IOException e) {
                    throw new IllegalStateException("Processing input directory failed. Reason: " + e.getMessage());
                }

                break;
            case ARCHIVE:
                //TODO: implement archivation
                var archiver = new Archiver(cfg);

                archiver.run(cfg.getInputDirectory());

                break;
        }
    }
}
