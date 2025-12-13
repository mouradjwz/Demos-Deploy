package elections.demos.backend.service;

import elections.demos.backend.dto.GetCandidatesDTO;

public interface CandidateService {
    GetCandidatesDTO getCandidatesByParty(String electionId, String partyId);
}