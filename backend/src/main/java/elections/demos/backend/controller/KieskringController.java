package elections.demos.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import elections.demos.backend.dto.KieskringTopPartiesDTO;
import elections.demos.backend.service.DutchElectionService;
import elections.demos.backend.service.DutchPartyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kieskring")
@CrossOrigin(origins = "*")
public class KieskringController {

    private static final Logger log = LoggerFactory.getLogger(KieskringController.class);

    private final DutchPartyService dutchPartyService;
    private final DutchElectionService dutchElectionService;

    public KieskringController(DutchPartyService dutchPartyService,
                               DutchElectionService dutchElectionService) {
        this.dutchPartyService = dutchPartyService;
        this.dutchElectionService = dutchElectionService;
    }

    // add logger for error and debug
    @GetMapping("/top/{electionId}")
    public ResponseEntity<?> getTopParties(@PathVariable String electionId) {
//    public ResponseEntity<List<KieskringTopPartiesDTO>> getTopParties(@PathVariable String electionId) {

        log.info("Received request for top parties for electionId={}", electionId);

        // 400 – bad request
        if (electionId.isEmpty()) {
            log.warn("Bad request: empty electionId");
            Map<String, String> body = new HashMap<>();
            body.put("message", "Parameter 'electionId' must not be empty.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }

        // 404 – not found
        if (!dutchElectionService.electionExists(electionId)) {
            log.warn("Election with id={} not found", electionId);
            Map<String, String> body = new HashMap<>();
            body.put("message", "Election with id '" + electionId + "' was not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }

        List<KieskringTopPartiesDTO> result = dutchPartyService.getTopPartiesByKieskringen(electionId);

        if (result.isEmpty()) {
            log.info("No top parties found for electionId={}", electionId);
            return ResponseEntity.noContent().build();// 204
        }

        return ResponseEntity.ok(result);
    }
}
