package org.reactome.reach.model.semanticscholar;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Paper {
    private String paper_id;
    private Metadata metadata;
    @JsonProperty(value = "abstract")
    private List<Abstract> abstracts;
    private List<BodyText> body_text;
    private Map<String, BibEntry> bib_entries;
    private Map<String, RefEntry> ref_entries;
    private BackMatter back_matter; 
    
    public Paper() {
    }
    
    public void setPaper_id(String paper_id) { 
        this.paper_id = paper_id;
    }
    public String getPaper_id() {
        return paper_id;
    }
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
    public Metadata getMetadata() {
        return metadata;
    }
    public void setAbstract(List<Abstract> abstracts) {
        this.abstracts = abstracts;
    }
    public List<Abstract> getAbstract() {
        return abstracts;
    }
    public void setBody_text(List<BodyText> body_text) {
        this.body_text = body_text;
    }
    public List<BodyText> getBody_text() {
        return body_text;
    }
    public void setBib_entries(Map<String, BibEntry> bib_entries) {
        this.bib_entries = bib_entries;
    }
    public Map<String, BibEntry> getBib_entries() {
        return bib_entries;
    }
    public void setRef_entries(Map<String, RefEntry> ref_entries) {
        this.ref_entries = ref_entries;
    }
    public Map<String, RefEntry> getRef_entries() {
        return ref_entries;
    }
    public void setBack_matter(BackMatter back_matter) {
        this.back_matter = back_matter;
    }
}
