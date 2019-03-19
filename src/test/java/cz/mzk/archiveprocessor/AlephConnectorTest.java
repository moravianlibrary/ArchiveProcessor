package cz.mzk.archiveprocessor;

import cz.mzk.archiveprocessor.model.Sysno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author kremlacek
 */
public class AlephConnectorTest {

    private AlephConnector connector;

    @BeforeEach
    public void prepare() {
        connector = getMockAlephConnector();
    }

    @Test
    public void getSysnoFromSignatureTest() {
        Sysno sys = connector.getSysnoFromSignature("TK-A-0039.785");

        assertNotNull(sys);
        assertEquals("mzk01", sys.getBase().toString(), "base must match");
        assertEquals("000960951", sys.getNumericalPart(), "num part must match");
    }

    @Test
    public void getSysnoFromBarcodeTest() {
        //TODO
    }

    public static AlephConnector getMockAlephConnector() {
        return new AlephConnector(new URLWrapperMock(AlephConnectorTest.class.getResource("/marcTestFile.xml")));
    }
}
