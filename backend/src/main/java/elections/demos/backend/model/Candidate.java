package elections.demos.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity representing a candidate in an election.
 */
@Entity
@Table(name = "candidate")
public class Candidate {
    @Id
    private String id; // Will store composite ID

    @ManyToOne
    @JoinColumn(name = "election_id")
    @JsonBackReference
    private Election election;

    @ManyToOne
    @JoinColumn(name = "party_id")
    @JsonBackReference
    private Party party;

    // Name fields
    private String initials;
    private String firstName;
    private String lastName;
    private String fullName;

    private String gender;
    private String localityName;

    public Candidate() {
    }

    public Candidate(String originalId, Election election, String initials, String firstName, String lastName, String gender, String localityName, Party party) {
        this.election = election;
        this.id = election.getElectionId() + "_" + party.getPartyCode() + "_" + originalId;
        this.initials = initials;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.fullName = createFullName();
        this.localityName = localityName;
        this.party = party;
    }


    private String createFullName() {
        StringBuilder sb = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) {
            sb.append(firstName).append(" ");
        }
        if (initials != null && !initials.isEmpty()) {
            sb.append(initials).append(" ");
        }
        if (lastName != null && !lastName.isEmpty()) {
            sb.append(lastName);
        }
        return sb.toString().trim();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
        this.fullName = createFullName();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        this.fullName = createFullName();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        this.fullName = createFullName();
    }

    public String getFullName() {
        return fullName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLocalityName() {
        return localityName;
    }

    public void setLocalityName(String localityName) {
        this.localityName = localityName;
    }

    public Election getElection() {
        return election;
    }

    public void setElection(Election election) {
        this.election = election;
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Candidate candidate = (Candidate) o;
        return Objects.equals(id, candidate.id) && Objects.equals(election, candidate.election) && Objects.equals(party, candidate.party) && Objects.equals(initials, candidate.initials) && Objects.equals(firstName, candidate.firstName) && Objects.equals(lastName, candidate.lastName) && Objects.equals(fullName, candidate.fullName) && Objects.equals(gender, candidate.gender) && Objects.equals(localityName, candidate.localityName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, election, party, initials, firstName, lastName, fullName, gender, localityName);
    }
}