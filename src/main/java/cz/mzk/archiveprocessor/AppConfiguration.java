package cz.mzk.archiveprocessor;

import java.io.File;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @author kremlacek
 */
public class AppConfiguration {
    enum ConfigType {
        PROCESS, ARCHIVE
    }

    private static final Options argumentOptions = new Options();;
    private static final CommandLineParser parser = new DefaultParser();
    private static final HelpFormatter helpFormatter = new HelpFormatter();

    private File inputDirectory;
    private File outputDirectory;
    private File errorDirectory;

    private AppConfiguration(ConfigType type) {

        Option inputOpt;
        Option outputOpt;

        switch (type) {
            case PROCESS:
                inputOpt = new Option("i", "input", true, "input (ingest) directory path");
                inputOpt.setRequired(true);

                outputOpt = new Option("o", "output", true, "output (temporary archive) directory path");
                outputOpt.setRequired(true);

                Option errorOpt = new Option("e", "error", true, "error directory path");
                errorOpt.setRequired(true);

                argumentOptions.addOption(inputOpt);
                argumentOptions.addOption(outputOpt);
                argumentOptions.addOption(errorOpt);
                break;
            case ARCHIVE:
                inputOpt = new Option("i", "input", true, "input (temporary archive) directory path");
                inputOpt.setRequired(true);

                outputOpt = new Option("o", "output", true, "output (permanent) directory path");
                outputOpt.setRequired(true);

                argumentOptions.addOption(inputOpt);
                argumentOptions.addOption(outputOpt);
                break;
        }
    }

    public static AppConfiguration getConfiguration(ConfigType type, String[] args) {
        var cfg = new AppConfiguration(type);

        CommandLine cmd;

        try {
            cmd = parser.parse(argumentOptions, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            helpFormatter.printHelp("utility-name", argumentOptions);
            return null;
        }

        switch (type) {
            case ARCHIVE:
                cfg.inputDirectory = new File(cmd.getOptionValue("input"));
                cfg.outputDirectory = new File(cmd.getOptionValue("output"));

                checkDirectory(cfg.inputDirectory, false);
                checkDirectory(cfg.outputDirectory, true);

                break;
            case PROCESS:
                cfg.inputDirectory = new File(cmd.getOptionValue("input"));
                cfg.outputDirectory = new File(cmd.getOptionValue("output"));
                cfg.errorDirectory = new File(cmd.getOptionValue("error"));

                checkDirectory(cfg.inputDirectory, false);
                checkDirectory(cfg.outputDirectory, true);
                checkDirectory(cfg.errorDirectory, true);
                break;
        }

        return cfg;
    }

    private static void checkDirectory(File directory, boolean writeRequired) {
        if (!directory.exists()) {
            throw new IllegalArgumentException("Directory does not exist. " + directory.getPath());
        }

        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Supplied path does not lead to directory" + directory.getPath());
        }

        if (writeRequired && !directory.canWrite()) {
            throw new IllegalArgumentException("Cannot write into supplied directory" + directory.getPath());
        }
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public File getErrorDirectory() {
        return errorDirectory;
    }

    public File getInputDirectory() {
        return inputDirectory;
    }
}
