package org.reactome.reach;

import java.util.ArrayList;
import java.util.List;

import org.gk.model.Person;

import com.opencsv.bean.CsvBindAndSplitByName;
import com.opencsv.bean.CsvBindByName;

public class PaperMetadata {

    public PaperMetadata() {
    }

    @CsvBindAndSplitByName(column = "sha", 
                           elementType = String.class,
                           collectionType = ArrayList.class,
                           splitOn = "; ")
    public List<String> shas;

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

    @CsvBindAndSplitByName(elementType = Person.class,
                           collectionType = ArrayList.class,
                           splitOn = "; ",
                           converter = TextToPerson.class)
    public List<Person> authors;

    @CsvBindByName
    public String journal;

    @CsvBindByName
    public boolean has_pdf_parse;

    @CsvBindByName
    public boolean has_pmc_xml_parse;

    @CsvBindByName
    public String full_text_file;

    @CsvBindByName
    public String url;
    
    @CsvBindByName
    public String publish_time;

    public List<String> getShas() {
        return shas;
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

    public boolean getHas_pmc_xml_parse() {
        return has_pmc_xml_parse;
    }

    public boolean getHas_pdf_parse() {
        return has_pdf_parse;
    }
    
    public List<Person> getAuthors() {
        return authors;
    }

    public String getTitle() {
        return title;
    }
    
    public String getJournal() {
        return journal;
    }
    
    public String getPublish_time() {
        return publish_time;
    }
    
    public int getYear() {
        String year = publish_time;

        // e.g. 2020-02-02
        if (publish_time.contains("-"))
            year = publish_time.substring(0, publish_time.indexOf("-"));

        return Integer.parseInt(year);
    }
    
}
