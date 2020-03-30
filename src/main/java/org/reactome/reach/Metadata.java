package org.reactome.reach;

public class Metadata {
    private String sha;
    private String doi; 
    private String pmcid;   
    private String pmid;   
    
    public Metadata() {
    }

    public Metadata(String sha, String doi, String pmcid, String pmid) {
        this.sha = sha;
        this.doi = doi;
        this.pmcid = pmcid;
        this.pmid = pmid;
    }
    
    public String getSha() {
        return sha;
    }
    public String getDoi() {
        return doi;
    }
    public String getPmcid() {
        return pmcid;
    }
    public String getPmid() {
        return pmid;
    }
}
