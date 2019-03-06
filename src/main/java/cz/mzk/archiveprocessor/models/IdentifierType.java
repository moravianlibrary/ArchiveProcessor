package cz.mzk.archiveprocessor.models;

/**
 * @author Jakub Kremlacek
 */
public enum IdentifierType {
    ISSN("ssn"), SIGNATURE("sig"), BARCODE("bar");

    String value;

    IdentifierType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
