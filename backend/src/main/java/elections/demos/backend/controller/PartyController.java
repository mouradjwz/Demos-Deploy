package elections.demos.backend.controller;

import elections.demos.backend.dto.GetPartiesDTO;
import elections.demos.backend.service.PartyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/{electionId}/party")
public class PartyController {

    private final PartyService partyService;

    public PartyController(PartyService partyService) {
        this.partyService = partyService;
    }

    @GetMapping("/all")
    public ResponseEntity<GetPartiesDTO> getAllParties(@PathVariable String electionId) {
        GetPartiesDTO response = partyService.getAllParties(electionId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


}
