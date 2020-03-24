package org.reactome.reach.model.semanticscholar;

public class Location {
    private String settlement;
    private String country;

    public Location() {
    }

    public void setSettlement(String settlement) {
        this.settlement = settlement;
    }
    public String getSettlement() {
        return settlement;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public String getCountry() {
        return country;
    }

}
