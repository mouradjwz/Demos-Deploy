package elections.demos.backend.dto;

import elections.demos.backend.model.Election;
import elections.demos.backend.model.Party;
import elections.demos.backend.model.PartyResult;
import elections.demos.backend.utils.PartyColorUtils;

public class PartyDataDTO {
    private String partyId;
    private String partyName;
    private int seats;
    private double percentage;
    private long totalVotes;
    private String color;  // New field

    public PartyDataDTO(String partyId, String partyName, int seats, double percentage, long totalVotes) {
        this.partyId = partyId;
        this.partyName = partyName;
        this.seats = seats;
        this.percentage = percentage;
        this.totalVotes = totalVotes;
        this.color = PartyColorUtils.getPartyColor(partyName); // Generate color here
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    // Getters and Setters
    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
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

    public PartyResult toEntity(Election election) {
        PartyResult result = new PartyResult();
        result.setElection(election);

        // Create Party using its constructor to ensure ID is generated correctly
        String partyCode = partyId.split("_")[1]; // Extract partyCode from partyId
        Party party = new Party(partyCode, this.partyName, election);
        result.setParty(party);

        result.setSeats(this.seats);
        result.setPercentage(this.percentage);
        result.setTotalVotes(this.totalVotes);
        result.setAreas(1); // Default

        return result;
    }
}