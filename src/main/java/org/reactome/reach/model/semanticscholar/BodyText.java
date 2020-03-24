package org.reactome.reach.model.semanticscholar;

import java.util.List;

public class BodyText {
    private String text;
    private List<Span> cite_spans;
    private List<Span> ref_spans;
    private List<Span> eq_spans;
    private String section;
    
    public BodyText() {
    }
    
    public void setText(String text) {
        this.text = text;
    }
    public String getText() {
        return text;
    }
    public void setCite_spans(List<Span> cite_spans) {
        this.cite_spans = cite_spans;
    }
    public List<Span> getCite_spans() {
        return cite_spans;
    }
    public void setRef_spans(List<Span> ref_spans) {
        this.ref_spans = ref_spans;
    }
    public List<Span> getRef_spans() {
        return ref_spans;
    }
    public void setEq_spans(List<Span> eq_spans) {
        this.eq_spans = eq_spans;
    }
    public List<Span> getEq_spans() {
        return eq_spans;
    }
    public void setSection(String section) {
        this.section = section;
    }
    public String getSection() {
        return section;
    }
}
