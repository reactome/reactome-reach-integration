package org.reactome.reach.model.semanticscholar;

import java.util.List;

public class Abstract {
    private String text;
    private List<String> cite_spans;
    private List<String> ref_spans;
    private String section;
    
    public Abstract() {
    }
    
    public void setText(String text) {
        this.text = text;
    }
    public String getText() {
        return text;
    }
    public void setCite_spans(List<String> cite_spans) {
        this.cite_spans = cite_spans;
    }
    public List<String> getCite_spans() {
        return cite_spans;
    }
    public void setRef_spans(List<String> ref_spans) {
        this.ref_spans = ref_spans;
    }
    public List<String> getRef_spans() {
        return ref_spans;
    }
    public void setSection(String section) {
        this.section = section;
    }
    public String getSection() {
        return section;
    }

}
