package elections.demos.backend.dto;

public class PartyVotesDTO {
    private String party;
    private long votes;

    public PartyVotesDTO() {}

    public PartyVotesDTO(String party, long votes) {
        this.party = party;
        this.votes = votes;
    }

    public String getParty() {
        return party;
    }

    public void setParty(String party) {
        this.party = party;
    }

    public long getVotes() {
        return votes;
    }

    public void setVotes(long votes) {
        this.votes = votes;
    }

    @Override
    public String toString() {
        return "PartyVotesDTO{" +
                "party='" + party + '\'' +
                ", votes=" + votes +
                '}';
    }
}
