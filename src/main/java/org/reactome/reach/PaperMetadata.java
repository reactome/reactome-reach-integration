package org.reactome.reach;

import com.opencsv.bean.CsvBindByName;

public class PaperMetadata {

    public PaperMetadata() {
    }

    @CsvBindByName
    public String sha;

    @CsvBindByName
    public String title;

    @CsvBindByName
    public String doi;

    @CsvBindByName
    public String pmcid;

    @CsvBindByName
    public String pubmed_id;

    @CsvBindByName
    public String license;

    @CsvBindByName
    public String authors;

    @CsvBindByName
    public String journal;

    @CsvBindByName
    public String has_pdf_parse;

    @CsvBindByName
    public String has_pmc_xml_parse;

    @CsvBindByName
    public String full_text_file;

    @CsvBindByName
    public String url;

    public String getSha() {
        return sha;
    }

    public String getPmcid() {
        return pmcid;
    }

    public String getPmid() {
        return pubmed_id;
    }

    public String getDoi() {
        return doi;
    }

    public String getFull_text_file() {
        return full_text_file;
    }

    public String getHasPmc_xml_parse() {
        return has_pmc_xml_parse;
    }

}
