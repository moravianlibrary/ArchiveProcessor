package cz.mzk.archiveprocessor;

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
    }
}
