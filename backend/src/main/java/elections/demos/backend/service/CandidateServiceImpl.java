package elections.demos.backend.service;

import elections.demos.backend.dto.CandidateDTO;
import elections.demos.backend.dto.GetCandidatesDTO;
import elections.demos.backend.model.Candidate;
import elections.demos.backend.model.Party;
import elections.demos.backend.repository.PartyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CandidateServiceImpl implements CandidateService {

    private final PartyRepository partyRepository;

    public CandidateServiceImpl(PartyRepository partyRepository) {
        this.partyRepository = partyRepository;
    }

    @Override
    public GetCandidatesDTO getCandidatesByParty(String electionId, String partyId) {
        String compositePartyId = electionId + "_" + partyId;

        Party party = partyRepository.findById(compositePartyId)
                .orElseThrow(() -> new RuntimeException("Party not found: " + compositePartyId));

        List<CandidateDTO> candidateDTOs = party.getCandidates().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new GetCandidatesDTO(candidateDTOs);
    }

    private CandidateDTO convertToDTO(Candidate candidate) {
        return new CandidateDTO(
                candidate.getId(),
                candidate.getFullName(),
                candidate.getFirstName(),
                candidate.getLastName(),
                candidate.getInitials(),
                candidate.getGender());
    }
}