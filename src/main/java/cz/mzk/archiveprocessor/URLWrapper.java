package cz.mzk.archiveprocessor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author kremlacek
 */
public class URLWrapper {
    public InputStream getStream(String url) throws IOException {
        return new URL(url).openStream();
    }
}
