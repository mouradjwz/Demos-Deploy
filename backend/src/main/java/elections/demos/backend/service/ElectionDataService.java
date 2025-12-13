package elections.demos.backend.service;

import elections.demos.backend.dto.PartyDataDTO;
import elections.demos.backend.model.Election;
import elections.demos.backend.model.Party;
import elections.demos.backend.model.PartyResult;
import elections.demos.backend.repository.ElectionRepository;
import elections.demos.backend.repository.PartyRepository;
import elections.demos.backend.repository.PartyResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ElectionDataService {

    private final DutchElectionService electionService;
    private final ElectionRepository electionRepo;
    private final PartyRepository partyRepo;
    private final PartyResultRepository resultRepo;

    public ElectionDataService(
            DutchElectionService electionService,
            ElectionRepository electionRepo,
            PartyRepository partyRepo,
            PartyResultRepository resultRepo
    ) {
        this.electionService = electionService;
        this.electionRepo = electionRepo;
        this.partyRepo = partyRepo;
        this.resultRepo = resultRepo;
    }

    /**
     * Haalt resultaten voor een specifieke verkiezing op.
     * Als de resultaten nog niet bestaan, worden ze berekend en opgeslagen.
     */
    public List<PartyDataDTO> getResults(String electionId, String folderName, int totalSeats) {
        // ✅ 1. Check if election already exists in DB
        List<PartyResult> existingResults = resultRepo.findByElection_ElectionId(electionId);
        if (!existingResults.isEmpty()) {
            System.out.println("Returning existing results for election: " + electionId);
            return convertToDTOs(existingResults);
        }

        // ✅ 2. Parse XML via DutchElectionService if results don't exist
        Election parsedElection = electionService.readResults(electionId, folderName);
        if (parsedElection == null) {
            throw new IllegalStateException("Parsing failed for election: " + electionId);
        }

        Map<String, Long> votesPerParty = parsedElection.getVotesPerParty();
        Map<String, String> namePerParty = parsedElection.getNamePerParty();
        long totalValidVotes = parsedElection.getTotalValidVotes();

        // ✅ 3. Calculate seats & percentages
        Map<String, Integer> seatsPerParty = computeSeats(votesPerParty, totalSeats);
        Map<String, Double> pctPerParty = computePercentages(votesPerParty, totalValidVotes);

        // ✅ 4. Save election (if new)
        Election election = electionRepo.findByElectionId(electionId)
                .orElseGet(() -> {
                    System.out.println("Saving new election: " + electionId);
                    return electionRepo.save(new Election(electionId));
                });

        // ✅ 5. Create PartyResults and associate with Election
        List<PartyResult> newResults = seatsPerParty.entrySet().stream()
                .map(entry -> {
                    String partyId = normalizePartyId(entry.getKey(), electionId);
                    int seats = entry.getValue();
                    double pct = pctPerParty.getOrDefault(partyId, 0.0);
                    long votes = votesPerParty.getOrDefault(partyId, 0L);
                    String name = namePerParty.getOrDefault(partyId, partyId);

                    Party party = partyRepo.findById(partyId)
                            .orElseGet(() -> {
                                System.out.println("Saving new party: " + partyId + " (" + name + ")");
                                Party newParty = new Party(partyId.split("_")[1], name, election);
                                return partyRepo.save(newParty);
                            });
                    PartyResult result = new PartyResult();
                    result.setElection(election);
                    result.setParty(party);
                    result.setSeats(seats); // Set calculated seats
                    result.setPercentage(pct); // Set calculated percentage
                    result.setTotalVotes(votes);
                    System.out.println("Created PartyResult: " + partyId + " - Seats: " + seats + ", Votes: " + votes);
                    return result;
                })
                .collect(Collectors.toList());

        resultRepo.saveAll(newResults);
        System.out.println("Saved " + newResults.size() + " PartyResults for election: " + electionId);

        // ✅ 6. Return as DTOs
        List<PartyDataDTO> dtos = convertToDTOs(newResults);
        System.out.println("Returning " + dtos.size() + " DTOs for election: " + electionId);
        return dtos;
    }

    private String normalizePartyId(String partyId, String electionId) {
        if (partyId != null && partyId.contains("_")) {
            String[] parts = partyId.split("_");
            if (parts.length == 2 && parts[0].equals(electionId)) {
                return partyId; // Already normalized
            }
            return electionId + "_" + parts[parts.length - 1];
        }
        return electionId + "_" + partyId; // Ensure prefix if missing
    }
    public List<PartyDataDTO> getResultsAsDTOByElection(String electionId,
                                                        String folderName,
                                                        int totalSeats) {
        return getResults(electionId, folderName, totalSeats);
    }

    // ===== Helper: converteert PartyResult → DTO =====
    private List<PartyDataDTO> convertToDTOs(List<PartyResult> results) {
        return results.stream()
                .sorted(Comparator.comparingInt(PartyResult::getSeats).reversed())
                .map(r -> new PartyDataDTO(
                        r.getParty().getId(),
                        r.getParty().getName(),
                        r.getSeats(),
                        r.getPercentage(),
                        r.getTotalVotes()
                ))
                .collect(Collectors.toList());
    }

    // ===== Helper: berekent zetels (D’Hondt methode) =====
    private Map<String, Integer> computeSeats(Map<String, Long> votesPerParty, int totalSeats) {
        Map<String, Integer> seats = new HashMap<>();
        if (votesPerParty == null || votesPerParty.isEmpty() || totalSeats <= 0) return seats;

        votesPerParty.keySet().forEach(p -> seats.put(p, 0));

        for (int i = 0; i < totalSeats; i++) {
            String best = null;
            double bestScore = -1.0;
            for (var e : votesPerParty.entrySet()) {
                int s = seats.get(e.getKey());
                double score = (double) e.getValue() / (s + 1);
                if (score > bestScore) {
                    bestScore = score;
                    best = e.getKey();
                }
            }
            if (best == null) break;
            seats.put(best, seats.get(best) + 1);
        }
        return seats;
    }

    // ===== Helper: berekent percentages =====
    private Map<String, Double> computePercentages(Map<String, Long> votesPerParty, long totalValidVotes) {
        Map<String, Double> percentages = new HashMap<>();
        if (totalValidVotes <= 0) return percentages;

        for (var e : votesPerParty.entrySet()) {
            percentages.put(e.getKey(), (e.getValue() * 100.0) / totalValidVotes);
        }
        return percentages;
    }
}
