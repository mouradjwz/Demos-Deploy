package elections.demos.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "party_results",
        uniqueConstraints = @UniqueConstraint(columnNames = {"election_id", "party_id"}))
public class PartyResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "election_id", nullable = false)
    private Election election;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    private int seats;

    @Column(nullable = false)
    private double percentage;

    private long totalVotes;

    private int areas;

    // Utility methods for managing relationships
    public void setElection(Election election) {
        if (this.election != null) {
            this.election.getPartyResults().remove(this);
        }
        this.election = election;
        if (election != null && !election.getPartyResults().contains(this)) {
            election.getPartyResults().add(this);
        }
    }

    public void setParty(Party party) {
        if (this.party != null) {
            this.party.getPartyResults().remove(this);
        }
        this.party = party;
        if (party != null && !party.getPartyResults().contains(this)) {
            party.getPartyResults().add(this);
        }
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Election getElection() {
        return election;
    }

    public Party getParty() {
        return party;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public long getTotalVotes() {
        return totalVotes;
    }

    public void setTotalVotes(long totalVotes) {
        this.totalVotes = totalVotes;
    }

    public int getAreas() {
        return areas;
    }

    public void setAreas(int areas) {
        this.areas = areas;
    }
}