package cz.mzk.archiveprocessor.models;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author kremlacek
 */
public class SysnoTest {

    @Test
    public void ValidBaseTest() {
        String[] validSysnoStrings = {"mzk01000123875", "mzk03002858729"};

        for (String validSysnoString : validSysnoStrings) {
            Sysno s = Sysno.createSysnoFromString(validSysnoString);

            Assert.assertNotNull(s);
            Assert.assertEquals("base must match", validSysnoString.substring(0, Sysno.SYSNO_BASE_LENGTH), s.getBase().toString());
            Assert.assertEquals("numerical part must match", validSysnoString.substring(Sysno.SYSNO_BASE_LENGTH), s.getNumericalPart());
        }

        //Assert.assertTrue();
    }
}
