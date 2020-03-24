package org.reactome.reach.model.semanticscholar;

import java.util.List;

public class Metadata {
    private String title;
    private List<Author> authors;
    
    public Metadata() {
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }
    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }
    public List<Author> getAuthors() {
        return authors;
    }
}
