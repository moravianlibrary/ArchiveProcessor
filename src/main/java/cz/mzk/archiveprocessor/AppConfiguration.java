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

    private AppConfiguration() {}

    private static final Options argumentOptions;
    private static final CommandLineParser parser = new DefaultParser();
    private static final HelpFormatter helpFormatter = new HelpFormatter();

    private File inputDirectory;
    private File outputDirectory;
    private File errorDirectory;

    static{
        argumentOptions = new Options();

        Option inputOpt = new Option("i", "input", true, "input directory path");
        inputOpt.setRequired(true);

        Option outputOpt = new Option("o", "output", true, "output directory path");
        outputOpt.setRequired(true);

        Option errorOpt = new Option("e", "error", true, "error directory path");
        errorOpt.setRequired(true);

        argumentOptions.addOption(inputOpt);
        argumentOptions.addOption(outputOpt);
        argumentOptions.addOption(errorOpt);
    }

    public static AppConfiguration getConfiguration(String[] args) {
        var cfg = new AppConfiguration();

        CommandLine cmd;

        try {
            cmd = parser.parse(argumentOptions, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            helpFormatter.printHelp("utility-name", argumentOptions);
            return null;
        }

        cfg.inputDirectory = new File(cmd.getOptionValue("input"));
        cfg.outputDirectory = new File(cmd.getOptionValue("output"));
        cfg.errorDirectory = new File(cmd.getOptionValue("error"));

        checkDirectory(cfg.inputDirectory, false);
        checkDirectory(cfg.outputDirectory, true);
        checkDirectory(cfg.errorDirectory, true);

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
