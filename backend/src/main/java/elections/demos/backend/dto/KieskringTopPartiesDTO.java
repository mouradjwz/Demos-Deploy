package elections.demos.backend.dto;

import java.util.List;

public class KieskringTopPartiesDTO {
    private int kieskring;
    private List<PartyVotesDTO> topParties;

    public KieskringTopPartiesDTO() {}

    public KieskringTopPartiesDTO(int kieskring, List<PartyVotesDTO> topParties) {
        this.kieskring = kieskring;
        this.topParties = topParties;
    }

    public int getKieskring() {
        return kieskring;
    }

    public void setKieskring(int kieskring) {
        this.kieskring = kieskring;
    }

    public List<PartyVotesDTO> getTopParties() {
        return topParties;
    }

    public void setTopParties(List<PartyVotesDTO> topParties) {
        this.topParties = topParties;
    }

    @Override
    public String toString() {
        return "KieskringTopPartiesDTO{" +
                "kieskring=" + kieskring +
                ", topParties=" + topParties +
                '}';
    }
}
