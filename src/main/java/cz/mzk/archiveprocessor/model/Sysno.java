package cz.mzk.archiveprocessor.model;

/**
 * Represents system number identifier.
 *
 * @author kremlacek
 */
public class Sysno {

    /**
     * Represents identifier base
     * 01 for new publications
     * 03 for oldprint
     */
    public enum Base {
        MZK01("mzk01"), MZK03("mzk03");

        private String value;

        Base(String value) {
            this.value = value;
        }

        public static Base fromString(String text) {
            for (Base b : Base.values()) {
                if (b.toString().equalsIgnoreCase(text)) {
                    return b;
                }
            }

            return null;
        }

        public String toString() {
            return value;
        }
    }

    public static int SYSNO_NUM_LENGTH = 9;
    public static int SYSNO_BASE_LENGTH = 5;

    private Base base;
    private String num;

    public Sysno(String base, String num) {
        base = base.toLowerCase();
        this.base = Base.fromString(base);

        if (this.base == null) {
            throw new IllegalArgumentException("Invalid base " + base + ".");
        }

        if (num.length() != SYSNO_NUM_LENGTH) {
            throw new IllegalArgumentException("Invalid sysno length. Required " + SYSNO_NUM_LENGTH + ", instead got " + num.length());
        }

        if (!num.matches("[0-9]+")) {
            throw new IllegalArgumentException("Invalid sysno contents. Sysno must contain only numbers, instead got " + num);
        }

        this.num = num;
    }

    /**
     * Loads sysno from string. If format is invalid null is returned.
     *
     * @param sysnoStr sysno string
     * @return sysno object or null
     */
    public static Sysno createSysnoFromString(String sysnoStr) {
        if (sysnoStr == null || sysnoStr.length() != SYSNO_NUM_LENGTH + SYSNO_BASE_LENGTH) {
            throw new IllegalArgumentException("Invalid sysno string. Length must be " + SYSNO_NUM_LENGTH + SYSNO_BASE_LENGTH);
        }

        return new Sysno(sysnoStr.substring(0, SYSNO_BASE_LENGTH), sysnoStr.substring(SYSNO_BASE_LENGTH));
    }

    public static boolean isValidSysno(String sysnoStr) {
        if (sysnoStr == null || sysnoStr.length() != SYSNO_NUM_LENGTH + SYSNO_BASE_LENGTH) {
            return false;
        }

        return isValidSysno(sysnoStr.substring(0, SYSNO_BASE_LENGTH), sysnoStr.substring(SYSNO_BASE_LENGTH));
    }

    public static boolean isValidSysno(String base, String num) {
        //check base part
        try {
            Base.valueOf(base);
        } catch (IllegalArgumentException e) {
            return false;
        }

        //check numerical part
        if (num.length() != SYSNO_NUM_LENGTH || !num.matches("[0-9]+]")) {
            return false;
        }

        return true;
    }

    public String getNumericalPart() {
        return num;
    }

    public Base getBase() {
        return base;
    }
}
