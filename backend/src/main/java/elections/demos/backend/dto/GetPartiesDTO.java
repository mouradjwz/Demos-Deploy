package elections.demos.backend.dto;

import java.util.List;

public class GetPartiesDTO {
    private List<PartyDTO> parties;

    public GetPartiesDTO(List<PartyDTO> parties) {
        this.parties = parties;
    }

    public List<PartyDTO> getParties() {
        return parties;
    }

    public void setParties(List<PartyDTO> parties) {
        this.parties = parties;
    }
}