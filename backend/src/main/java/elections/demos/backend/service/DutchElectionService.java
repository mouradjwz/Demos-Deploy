package elections.demos.backend.service;

import elections.demos.backend.dto.ExistingElectionDTO;
import elections.demos.backend.model.Election;

import java.util.List;

public interface DutchElectionService {
    Election readResults(String electionId, String folderName);

    boolean electionExists(String electionId);

    List<ExistingElectionDTO> getAvailableElections();
}