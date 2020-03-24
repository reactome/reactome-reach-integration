package org.reactome.reach.model.semanticscholar;

public class Affiliation {
    private String laboratory;
    private String institution;
    private Location location;
    
    public Affiliation() {
    }

    public void setLaboratory(String laboratory) {
        this.laboratory = laboratory;
    }
    public String getLaboratory() {
        return laboratory;
    }
    public void setInstitution(String institution) {
        this.institution = institution;
    }
    public String getInstitution() {
        return institution;
    }
    public void setLocation(Location location) {
        this.location = location;
    }
    public Location getLocation() {
        return location;
    }
}
