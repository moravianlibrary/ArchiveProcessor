package cz.mzk.archiveprocessor.processor;

import cz.mzk.archiveprocessor.AlephConnector;
import cz.mzk.archiveprocessor.AppConfiguration;
import cz.mzk.archiveprocessor.model.Sysno;
import java.io.File;
import java.nio.file.Path;
import java.util.Calendar;

/**
 * @author kremlacek
 */
public class MZKProcessor extends Processor {

    public MZKProcessor(AppConfiguration cfg) {
        super(cfg);
    }

    public MZKProcessor(AppConfiguration cfg, AlephConnector connector) {
        super(cfg, connector);
    }

    public MZKProcessor(File errorDirectory, File archiveDirectory) {
        super(errorDirectory, archiveDirectory);
    }

    public MZKProcessor(File errorDirectory, File archiveDirectory, AlephConnector connector) {
        super(errorDirectory, archiveDirectory, connector);
    }

    @Override
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
