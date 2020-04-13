package org.reactome.reach;

public class FriesConstants {
    public static final String PMC = "PMC";
    public static final String PMID = "PMID";
    public static final String DOI = "DOI";
    public static final String MERGED = "merged";
    public static final String FILTERED = "filtered";
    public static final String COMPLETED = "fries-completed";
    public static final String REFERENCES = "references";
    public static final String RESTART_LOG = "restart.log";
    public static final String PROPERTY_FILE = "converter.prop";
    public static final String OUTPUT = "output";
    public static final String JSON_EXT = ".json";
    public static final String FRIES_EXT = ".fries";
    public static final String EVENTS = "events";
    public static final String ENTITIES = "entities";
    public static final String SENTENCES = "sentences";
    public static final String PMC_ID = "PMC ID";
    public static final String NCBI_URL = "https://www.ncbi.nlm.nih.gov";
    public static final String PMC_URL = NCBI_URL.concat("/pmc/articles/");
    public static final String PMID_URL = NCBI_URL.concat("/pubmed/");
    public static final String QUERY_PREFIX = "?text=";
    public static final String REACH_API_URL = ""; // TODO determine local running REACH url.
    public static final String REACH_SEND_TYPE = ""; // TODO determine send type for file upload.
    public static final String GRAPHQL_API_URL = "https://reach-api.nrnb-docker.ucsd.edu/";
    public static final String GRAPHQL_SEND_TYPE = "application/GraphQL";
    public static final String GRAPHQL_SEARCH_TEMPLATE = "resources/reachSearchTemplate.graphql";
    public static final String REACH_REQUEST_TYPE = "application/Json";
    public static final String PAPER_URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?";
    public static final String CONVERT_URL = "https://www.ncbi.nlm.nih.gov/pmc/utils/idconv/v1.0/";
    public static final String TOOL_EMAIL = "&tool=Reactome-REACH&email=beckmanl@ohsu.edu";
    public static final String PMC_QUERY = "&db=pmc";
    public static final String PUBMED_QUERY = "&db=pubmed";
    public static final String ID_QUERY = "&id=";
    public static final String IDS_QUERY = "&ids=";
    public static final String NXML_EXT = ".nxml";
    public static final String RECORD = "record";
    public static final String REFERENCE_URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?";
    public static final String RETMODE = "&retmode=json";

}
