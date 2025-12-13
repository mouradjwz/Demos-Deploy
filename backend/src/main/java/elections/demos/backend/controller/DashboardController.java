
package elections.demos.backend.controller;

import elections.demos.backend.dto.PartyDataDTO;
import elections.demos.backend.model.Election;
import elections.demos.backend.model.PartyResult;
import elections.demos.backend.repository.PartyResultRepository;
import elections.demos.backend.service.DutchElectionService;
import elections.demos.backend.service.DutchPartyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {
    private final PartyResultRepository partyResultRepository;
    private final DutchPartyService dutchPartyService;
    private final DutchElectionService dutchElectionService;

    @Autowired
    public DashboardController(PartyResultRepository partyResultRepository,
                               DutchPartyService dutchPartyService,
                               DutchElectionService dutchElectionService) {
        this.partyResultRepository = partyResultRepository;
        this.dutchPartyService = dutchPartyService;
        this.dutchElectionService = dutchElectionService;
    }

    @PostMapping("/import/all")
    public ResponseEntity<Map<String, Object>> importAll(
            @RequestParam(defaultValue = "/app/electiondata") String folderName) {  // ← changed default  

        Map<String, Object> result = new HashMap<>();
        List<Election> elections = new ArrayList<>();
        String[] availableYears = {"TK2021", "TK2023", "TK2017"}; // Add more as needed

        for (String electionId : availableYears) {
            try {
                Election election = dutchElectionService.readResults(electionId, folderName);
                dutchPartyService.calculateAndStoreResults(election);
                elections.add(election);
            } catch (Exception e) {
                System.err.println("Failed to import " + electionId + ": " + e.getMessage());
            }
        }

        result.put("importedElections", elections);
        result.put("message", "Successfully imported " + elections.size() + " elections");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/elections")
    public ResponseEntity<List<String>> getAvailableElections() {
        List<String> elections = List.of("TK2021", "TK2023", "TK2017"); // Or fetch from database
        return ResponseEntity.ok(elections);
    }

    @GetMapping("/results/{electionId}")
    public ResponseEntity<List<PartyResult>> getResultsByElection(
            @PathVariable String electionId) {
        List<PartyResult> results = partyResultRepository.findByElection_ElectionId(electionId);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/dto/{electionId}")
    public ResponseEntity<List<PartyDataDTO>> getResultsAsDTOByElection(
            @PathVariable String electionId) {
        try {
            System.out.println("Fetching DTO for election: " + electionId);

            // Check if the election exists
            if (!dutchElectionService.electionExists(electionId)) {
                System.err.println("Election not found: " + electionId);
                return ResponseEntity.notFound().build();
            }

            // Calculate seats
            List<PartyDataDTO> dtos = dutchPartyService.calculateSeats(electionId, 150);
            if (dtos.isEmpty()) {
                System.err.println("No party data found for election: " + electionId);
                return ResponseEntity.noContent().build();
            }

            System.out.println("Returning " + dtos.size() + " parties for election: " + electionId);
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            System.err.println("Error getting DTO for election " + electionId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}