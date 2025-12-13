package elections.demos.backend.controller;

import elections.demos.backend.dto.PartyDataDTO;
import elections.demos.backend.service.ElectionDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/elections")
@CrossOrigin(origins = "*")
public class ElectionDataController {

    private final ElectionDataService electionDataService;

    public ElectionDataController(ElectionDataService electionDataService) {
        this.electionDataService = electionDataService;
    }

    /**
     * Eén dynamisch endpoint:
     * Voorbeeld: GET /api/elections/TK2021/results
     * GET /api/elections/TK2023/results
     * Optionele query params:
     * - folderName (default: electiondata)
     * - totalSeats (default: 150)
     */
    @GetMapping("/{electionId}/results")
    public ResponseEntity<List<PartyDataDTO>> getResultsByYear(
            @PathVariable String electionId,
            @RequestParam(defaultValue = "electiondata") String folderName,
            @RequestParam(defaultValue = "150") int totalSeats
    ) {
        List<PartyDataDTO> results = electionDataService.getResults(electionId, folderName, totalSeats);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/dto/{electionId}")
    public ResponseEntity<List<PartyDataDTO>> getResultsAsDTOByElection(
            @PathVariable String electionId,
            @RequestParam(defaultValue = "electiondata") String folderName,
            @RequestParam(defaultValue = "150") int totalSeats
    ) {
        List<PartyDataDTO> dtos =
                electionDataService.getResultsAsDTOByElection(electionId, folderName, totalSeats);
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importYears(
            @RequestParam(defaultValue = "TK2017,TK2021,TK2023") String years,
            @RequestParam(defaultValue = "electiondata") String folderName,
            @RequestParam(defaultValue = "150") int totalSeats
    ) {
        Map<String, Object> out = new HashMap<>();
        List<String> imported = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        Arrays.stream(years.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .forEach(electionId -> {
                    try {
                        electionDataService.getResults(electionId, folderName, totalSeats);
                        imported.add(electionId);
                    } catch (Exception ex) {
                        failed.add(electionId + " (" + ex.getMessage() + ")");
                    }
                });

        out.put("imported", imported);
        out.put("failed", failed);
        out.put("message", "Import klaar");
        return ResponseEntity.ok(out);
    }

}