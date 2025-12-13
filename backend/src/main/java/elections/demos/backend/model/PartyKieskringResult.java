package elections.demos.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "party_kieskring_result")
public class PartyKieskringResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer kieskring;
    private String party;
    private Long votes;
    private String electionId;

    public PartyKieskringResult() {}

    public PartyKieskringResult(Integer kieskring, String party, Long votes, String electionId) {
        this.kieskring = kieskring;
        this.party = party;
        this.votes = votes;
        this.electionId = electionId;
    }


    //check of nog nodig is
    public Long getId() { return id; }
    public Integer getKieskring() { return kieskring; }
    public String getParty() { return party; }
    public Long getVotes() { return votes; }
    public String getElectionId() { return electionId; }

    public void setId(Long id) { this.id = id; }
    public void setKieskring(Integer kieskring) { this.kieskring = kieskring; }
    public void setParty(String party) { this.party = party; }
    public void setVotes(Long votes) { this.votes = votes; }
    public void setElectionId(String electionId) { this.electionId = electionId; }
}
