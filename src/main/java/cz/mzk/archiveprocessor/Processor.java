package cz.mzk.archiveprocessor;

import cz.mzk.archiveprocessor.models.Sysno;

/**
 * @author kremlacek
 */
public class Processor {

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
}
