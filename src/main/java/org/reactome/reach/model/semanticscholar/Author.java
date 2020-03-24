package org.reactome.reach.model.semanticscholar;

import java.util.List;

public class Author {
    private String first;
    private List<String> middle;
    private String last;
    private String suffix;
    private Affiliation affiliation;
    private String email;
    
    public Author() {
    }

    public void setFirst(String first) {
        this.first = first;
    }
    public String getFirst() {
        return first;
    }
    public void setMiddle(List<String> middle) {
        this.middle = middle;
    }
    public List<String> getMiddle() {
        return middle;
    }
    public void setLast(String last) {
        this.last = last;
    }
    public String getLast() {
        return last;
    }
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
    public String getSuffix() {
        return suffix;
    }
    public void setAffiliation(Affiliation affiliation) {
        this.affiliation = affiliation;
    }
    public Affiliation getAffiliation() {
        return affiliation;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getEmail() {
        return email;
    }
}
