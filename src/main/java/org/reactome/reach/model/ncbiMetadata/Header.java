package org.reactome.reach.model.ncbiMetadata;

public class Header {
    private String type;
    private String version;

    public Header() {
    }

    public void setType(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getVersion() {
        return version;
    }
}
