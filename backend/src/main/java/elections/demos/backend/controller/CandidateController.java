package elections.demos.backend.controller;

import elections.demos.backend.dto.GetCandidatesDTO;
import elections.demos.backend.service.CandidateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/{electionId}/candidate")
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @GetMapping("/party/{partyId}")
    public ResponseEntity<GetCandidatesDTO> getCandidatesFromParty(
            @PathVariable String electionId,
            @PathVariable String partyId) {
        GetCandidatesDTO response = candidateService.getCandidatesByParty(electionId, partyId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}