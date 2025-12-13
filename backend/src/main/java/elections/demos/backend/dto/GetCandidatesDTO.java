package elections.demos.backend.dto;

import java.util.List;

public class GetCandidatesDTO {
    private List<CandidateDTO> candidates;

    public GetCandidatesDTO(List<CandidateDTO> candidates) {
        this.candidates = candidates;
    }

    public List<CandidateDTO> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<CandidateDTO> candidates) {
        this.candidates = candidates;
    }
}