package elections.demos.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "region")
public class Region {
    @Id
    private String id; // Composite ID for uniqueness

    private Integer regionNumber;

    private String regionCategory; // "STAAT", "KIESKRING", "GEMEENTE"
    private String regionName;

    @ManyToOne
    @JoinColumn(name = "election_id")
    @JsonBackReference
    private Election election;

    private String superiorRegionCategory;
    private String committeeCategory;

    public Region() {
    }

    public Region(String electionId, Integer regionNumber, String regionCategory, String regionName) {
        this.id = electionId + "_" + regionNumber + "_" + regionCategory;
        this.regionNumber = regionNumber;
        this.regionCategory = regionCategory;
        this.regionName = regionName;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getRegionNumber() {
        return regionNumber;
    }

    public void setRegionNumber(Integer regionNumber) {
        this.regionNumber = regionNumber;
    }

    public String getRegionCategory() {
        return regionCategory;
    }

    public void setRegionCategory(String regionCategory) {
        this.regionCategory = regionCategory;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public Election getElection() {
        return election;
    }

    public void setElection(Election election) {
        this.election = election;
    }

    public String getSuperiorRegionCategory() {
        return superiorRegionCategory;
    }

    public void setSuperiorRegionCategory(String superiorRegionCategory) {
        this.superiorRegionCategory = superiorRegionCategory;
    }

    public String getCommitteeCategory() {
        return committeeCategory;
    }

    public void setCommitteeCategory(String committeeCategory) {
        this.committeeCategory = committeeCategory;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Region region = (Region) o;
        return Objects.equals(id, region.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}