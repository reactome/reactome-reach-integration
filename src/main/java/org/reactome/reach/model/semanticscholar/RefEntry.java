package org.reactome.reach.model.semanticscholar;

public class RefEntry {
    private String text;
    private String type;

    public RefEntry() {
    }

    public void setText(String text) {
        this.text = text;
    }
    public String getText() {
        return text;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }
}

