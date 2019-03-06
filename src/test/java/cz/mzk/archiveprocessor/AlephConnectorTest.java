package cz.mzk.archiveprocessor;

import cz.mzk.archiveprocessor.models.Sysno;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author kremlacek
 */
public class AlephConnectorTest {

    private AlephConnector connector;

    public class URLWrapperMock extends URLWrapper {

        private List<String> requestedURLList = new LinkedList<>();
        private URL returnURL;
        private URL responseIdURL = AlephConnectorTest.class.getResource("/recordTestFile.xml");

        public URLWrapperMock(URL returnURL) {
            if (returnURL == null) {
                throw new IllegalArgumentException("returnURL cannot be null");
            }

            this.returnURL = returnURL;
        }

        @Override
        public InputStream getStream(String url) throws IOException {
            requestedURLList.add(url);

            if (url.startsWith("http://aleph.mzk.cz/X?base=")) {
                return responseIdURL.openStream();
            }

            return returnURL.openStream();
        }

        public List<String> getRequestedURL() {
            return requestedURLList;
        }
    }

    @BeforeEach
    public void prepare() {
        connector = new AlephConnector(new URLWrapperMock(AlephConnectorTest.class.getResource("/marcTestFile.xml")));
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
}
