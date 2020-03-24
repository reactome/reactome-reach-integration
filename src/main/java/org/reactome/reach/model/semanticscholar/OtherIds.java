package org.reactome.reach.model.semanticscholar;

import java.util.List;

public class OtherIds {
    private List<String> DOI;
    
    public OtherIds() {
    }
    
    public void setDoi(List<String> DOI) {
        this.DOI = DOI;
    }
    public List<String> getDoi() {
        return DOI;
    }
}
