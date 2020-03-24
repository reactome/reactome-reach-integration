package org.reactome.reach.model.semanticscholar;

import java.util.List;

public class BibEntry {
    private String ref_id;
    private String title;
    private List<Author> authors;
    private int year;
    private String venue;
    private String volume;
    private String issn;
    private String pages;
    private OtherIds other_ids;

    public BibEntry() {
    }

    public void setRef_id(String ref_id) {
        this.ref_id = ref_id;
    }
    public String getRef_id() {
        return ref_id;
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
    public void setYear(int year) {
        this.year = year;
    }
    public int getYear() {
        return year;
    }
    public void setVenue(String venue) {
        this.venue = venue;
    }
    public String getVenue() {
        return venue;
    }
    public void setVolume(String volume) {
        this.volume = volume;
    }
    public String getVolume() {
        return volume;
    }
    public void setIssn(String issn) {
        this.issn = issn;
    }
    public String getIssn() {
        return issn;
    }
    public void setPages(String pages) {
        this.pages = pages;
    }
    public String getPages() {
        return pages;
    }
    public void setOther_ids(OtherIds other_ids) {
        this.other_ids = other_ids;
    }
    public OtherIds getOther_ids() {
        return other_ids;
    }
}
