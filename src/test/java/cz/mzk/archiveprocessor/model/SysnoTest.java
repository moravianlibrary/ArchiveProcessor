package cz.mzk.archiveprocessor.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author kremlacek
 */
public class SysnoTest {

    @Test
    public void ValidBaseTest() {
        String[] validSysnoStrings = {"mzk01000123875", "MZK01000123875", "mzk03002858729"};

        for (String validSysnoString : validSysnoStrings) {
            Sysno s = Sysno.createSysnoFromString(validSysnoString);

            assertNotNull(s);
            //even upper case should pass
            assertEquals(validSysnoString.substring(0, Sysno.SYSNO_BASE_LENGTH).toLowerCase(), s.getBase().toString(), "base must match");
            assertEquals(validSysnoString.substring(Sysno.SYSNO_BASE_LENGTH), s.getNumericalPart(), "numerical part must match");
        }
    }

    @Test
    public void InvalidBaseTest() {
        String[] validSysnoStrings = {"mzk00000123875", "mzk04002858729"};

        for (String validSysnoString : validSysnoStrings) {
            assertThrows(IllegalArgumentException.class, () -> Sysno.createSysnoFromString(validSysnoString), "invalid base cannot pass");
        }
    }

    @Test
    public void InvalidNumTest() {
        String[] validSysnoStrings = {"mzk0100012a875", "mzk03002.58729", "mzk030021158729"};

        for (String validSysnoString : validSysnoStrings) {
            assertThrows(IllegalArgumentException.class, () -> Sysno.createSysnoFromString(validSysnoString), "invalid number cannot pass");
        }
    }
}
