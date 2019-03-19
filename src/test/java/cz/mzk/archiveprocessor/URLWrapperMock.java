package cz.mzk.archiveprocessor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * @author kremlacek
 */
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
