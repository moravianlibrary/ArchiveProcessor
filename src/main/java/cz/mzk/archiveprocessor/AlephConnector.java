package cz.mzk.archiveprocessor;

import cz.mzk.archiveprocessor.models.IdentifierType;
import cz.mzk.archiveprocessor.models.Sysno;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author kremlacek
 */
public class AlephConnector {

    private static Logger LOG = Logger.getLogger(AlephConnector.class.getName());

    private Map<Sysno, Document> memoizedDocMap = new HashMap<>();

    public static final int RETRY_COUNT = 5;

    private URLWrapper urlWrapper;

    public AlephConnector() {
        this(new URLWrapper());
    }

    public AlephConnector(URLWrapper urlWrapper) {
        this.urlWrapper = urlWrapper;
    }

    public Sysno getSysnoFromSignature(String signature) {
        return getSysnoWithBaseFromAleph(signature, IdentifierType.SIGNATURE);
    }

    public Sysno getSysnoFromBarcode(String barcode) {
        return getSysnoWithBaseFromAleph(barcode, IdentifierType.BARCODE);
    }

    /**
     * request for MARC record from Aleph XServer with retrieval of Sysno and Base
     *
     * @param identifierValue
     * @return
     */
    private Sysno getSysnoWithBaseFromAleph(String identifierValue, IdentifierType identifierType) {
        String sysno;
        String base = null;

        if (identifierValue == null) return null;

        if (identifierValue.contains(" ")) {
            identifierValue  = identifierValue.replaceAll(" ", "%20");
        }

        Document doc = getResponseFromAleph(identifierValue, RETRY_COUNT, identifierType);

        if (doc == null) {
            return null;
        }

        String set_number = doc.getElementsByTagName("set_number").item(0).getTextContent();
        String no_entries = doc.getElementsByTagName("no_entries").item(0).getTextContent();

        int counter = 0;

        do {
            try {
                doc = getDocumentFromURL("http://aleph.mzk.cz/X?op=present&set_no=" + set_number + "&set_entry=" + no_entries + "&format=marc");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            counter++;
        } while (
                counter < RETRY_COUNT &&
                        doc.getElementsByTagName("doc_number").getLength() < 0
        );

        if (counter == RETRY_COUNT) {
            LOG.info("Could not get sysno from Aleph. Identifier: " + identifierValue + ", Type: " + identifierType.getValue());
        }

        sysno = doc.getElementsByTagName("doc_number").item(0).getTextContent();

        NodeList field = doc.getElementsByTagName("subfield");

        for (int i = 0; i < field.getLength(); i++) {
            String label = ((Element) field.item(i)).getAttribute("label");

            if (label.equals("l")) {
                base = field.item(i).getTextContent();
            }
        }

        if (sysno == null || !base.startsWith("MZK0")) return null;

        Sysno resultSysno = new Sysno(base, sysno);

        memoizeDoc(resultSysno, doc);

        return resultSysno;
    }

    private void memoizeDoc(Sysno sysno, Document doc) {
        memoizedDocMap.put(sysno, doc);
    }

    public Document getMemoizedDoc(Sysno sysno) {
        return memoizedDocMap.get(sysno);
    }

    private Document getResponseFromAleph(String identifierValue, int retryCount, IdentifierType type) {
        Document doc;

        for (Sysno.Base alephBase : Sysno.Base.values()) {
            doc = getResponseFromAleph(alephBase.toString(), identifierValue, retryCount, type);

            if (doc != null) return doc;
        }

        return null;
    }

    private Document getResponseFromAleph(String base, String identifierValue, int retryCount, IdentifierType type) {
        int counter = 0;
        Document doc;

        do {
            try {
                doc = getDocumentFromURL("http://aleph.mzk.cz/X?base=" + base + "&op=find&request="+type.getValue()+"=" + identifierValue);
            } catch (IOException | SAXException | ParserConfigurationException e) {
                LOG.warning("Loading document for identifier " + identifierValue);
                e.printStackTrace();
                return null;
            }

            counter++;
        } while (
            counter < retryCount &&
                    doc.getElementsByTagName("set_number").getLength() < 1 &&
                    doc.getElementsByTagName("no_entries").getLength() < 1
        );

        if (counter == retryCount) return null;

        return doc;
    }

    private Document getDocumentFromURL(String url) throws IOException, ParserConfigurationException, SAXException {
        if (url == null) return null;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        return db.parse(urlWrapper.getStream(url));
    }
}
