package org.reactome.reach.model.semanticscholar;

public class Span {
    private int start;
    private int end;
    private String text;
    private String ref_id;

    public Span() {
    }
    
    public void setStart(int start) {
        this.start = start;
    }
    public int getStart() {
        return start;
    }
    public void setEnd(int end) {
        this.end = end;
    }
    public int getEnd() {
        return end;
    }
    public void setText(String text) {
        this.text = text;
    }
    public String getText() {
        return text;
    }
    public void setRef_id(String ref_id) {
        this.ref_id = ref_id;
    }
    public String getRef_id() {
        return ref_id;
    }
}
