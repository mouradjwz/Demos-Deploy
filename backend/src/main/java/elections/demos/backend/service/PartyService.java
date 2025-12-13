package elections.demos.backend.service;

import elections.demos.backend.dto.GetPartiesDTO;

public interface PartyService {

    GetPartiesDTO getAllParties(String electionId);
}
