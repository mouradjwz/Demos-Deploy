package elections.demos.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "party")
public class Party {
    @Id
    private String id; // Composite ID: electionId + "_" + partyCode

    private String partyCode;

    private String name;

    @ManyToOne
    @JoinColumn(name = "election_id", nullable = false)
    @JsonBackReference
    private Election election;

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Candidate> candidates = new ArrayList<>();

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PartyResult> partyResults = new ArrayList<>();

    public Party() {
    }

    public Party(String partyCode, String name, Election election) {
        if (partyCode == null || name == null || election == null) {
            throw new IllegalArgumentException("Party code, name, and election cannot be null.");
        }
        this.partyCode = partyCode;
        this.name = name;
        setElection(election); // Automatically sets the ID.
    }

    public void setElection(Election election) {
        if (election == null) {
            throw new IllegalArgumentException("Election cannot be null.");
        }
        this.election = election;
        this.id = election.getElectionId() + "_" + partyCode;
    }

    public void addCandidate(Candidate candidate) {
        if (candidate == null) {
            throw new IllegalArgumentException("Candidate cannot be null.");
        }
        if (!candidates.contains(candidate)) {
            candidates.add(candidate);
            candidate.setParty(this);
        }
    }

    public void addPartyResult(PartyResult partyResult) {
        if (partyResult == null) {
            throw new IllegalArgumentException("PartyResult cannot be null.");
        }
        if (!partyResults.contains(partyResult)) {
            partyResults.add(partyResult);
            partyResult.setParty(this);
        }
    }
    // Getters and setters

    public String getId() {
        return id;
    }

    public String getPartyCode() {
        return partyCode;
    }

    public void setPartyCode(String partyCode) {
        this.partyCode = partyCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Election getElection() {
        return election;
    }

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public List<PartyResult> getPartyResults() {
        return partyResults;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Party party = (Party) o;
        return Objects.equals(id, party.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Party{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}