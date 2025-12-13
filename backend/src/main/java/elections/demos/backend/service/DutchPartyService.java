package elections.demos.backend.service;

import elections.demos.backend.dto.PartyDataDTO;
import elections.demos.backend.model.Election;
import elections.demos.backend.model.Party;
import elections.demos.backend.model.PartyResult;
import elections.demos.backend.model.PartyKieskringResult;
import elections.demos.backend.repository.ElectionRepository;
import elections.demos.backend.repository.PartyRepository;
import elections.demos.backend.repository.PartyResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import elections.demos.backend.dto.KieskringTopPartiesDTO;
import elections.demos.backend.dto.PartyVotesDTO;
import elections.demos.backend.repository.PartyKieskringResultRepository;


import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DutchPartyService {
    private final ElectionRepository electionRepository;
    private final PartyRepository partyRepository;
    private final PartyResultRepository partyResultRepository;
    private final PartyKieskringResultRepository partyKieskringResultRepository;


    @Autowired
    public DutchPartyService(ElectionRepository electionRepository, PartyRepository partyRepository, PartyResultRepository partyResultRepository,
                             PartyKieskringResultRepository partyKieskringResultRepository) {
        this.electionRepository = electionRepository;
        this.partyRepository = partyRepository;
        this.partyResultRepository = partyResultRepository;
        this.partyKieskringResultRepository = partyKieskringResultRepository;
    }

    public List<PartyDataDTO> calculateSeats(String electionId, int totalSeats) {
        System.out.println("Calculating seats for election: " + electionId);

        // Fetch the election
        Election election = electionRepository.findByElectionId(electionId).orElseThrow(() -> new IllegalArgumentException("Election not found: " + electionId));
        System.out.println("Election found: " + election.getElectionId());

        // Extract votes per party
        Map<String, Long> votesPerParty = extractVotesPerParty(election);
        if (votesPerParty.isEmpty()) {
            System.err.println("No votes found for election: " + electionId);
            return List.of();
        }

        // Calculate seats
        List<PartyDataDTO> seatResults = calculateSeatsWithVotes(votesPerParty, totalSeats);
        System.out.println("Calculated " + seatResults.size() + " party results for election: " + electionId);
        return seatResults;
    }

    @Transactional
    public List<PartyResult> calculateAndStoreResults(Election election) {
        partyResultRepository.deleteByElectionId(election.getElectionId());
        Map<String, Long> votesPerParty = extractVotesPerParty(election);
        List<PartyDataDTO> seatResults = calculateSeatsWithVotes(votesPerParty, election.getNumberOfSeats());
        return savePartyResults(seatResults, election);
    }

    private Election getElectionById(String electionId) {
        return electionRepository.findByElectionId(electionId).orElseThrow(() -> new IllegalArgumentException("Election not found: " + electionId));
    }

    private Map<String, Long> extractVotesPerParty(Election election) {
        return election.getPartyResults().stream().collect(Collectors.toMap(pr -> pr.getParty().getId(), PartyResult::getTotalVotes));
    }

    private List<PartyDataDTO> calculateSeatsWithVotes(Map<String, Long> votesPerParty, int totalSeats) {
        long totalValidVotes = votesPerParty.values().stream().mapToLong(Long::longValue).sum();
        if (totalValidVotes == 0) return List.of();

        double quota = (double) totalValidVotes / totalSeats;
        Map<String, Integer> seats = new HashMap<>();
        Map<String, Double> remainders = new HashMap<>();
        int assigned = 0;

        for (var entry : votesPerParty.entrySet()) {
            double quotient = entry.getValue() / quota;
            int baseSeats = (int) Math.floor(quotient);
            seats.put(entry.getKey(), baseSeats);
            assigned += baseSeats;
            remainders.put(entry.getKey(), quotient - baseSeats);
        }

        assignRemainingSeats(seats, remainders, totalSeats - assigned);

        return convertToPartyDataDTOs(seats, votesPerParty, totalValidVotes);
    }

    private void assignRemainingSeats(Map<String, Integer> seats, Map<String, Double> remainders, int remainingSeats) {
        if (remainingSeats > 0) {
            remainders.entrySet().stream().sorted((a, b) -> Double.compare(b.getValue(), a.getValue())).limit(remainingSeats).forEach(entry -> seats.merge(entry.getKey(), 1, Integer::sum));
        }
    }

    private List<PartyDataDTO> convertToPartyDataDTOs(Map<String, Integer> seats, Map<String, Long> votesPerParty, long totalValidVotes) {
        return seats.entrySet().stream().map(entry -> {
            String partyId = entry.getKey();
            long votes = votesPerParty.get(partyId);
            double percentage = (votes * 100.0) / totalValidVotes;
            String partyName = partyRepository.findById(partyId).map(Party::getName).orElse("Unknown Party");
            return new PartyDataDTO(partyId, partyName, entry.getValue(), percentage, votes);
        }).sorted(Comparator.comparingInt(PartyDataDTO::getSeats).reversed()).collect(Collectors.toList());
    }

    private List<PartyResult> savePartyResults(List<PartyDataDTO> seatResults, Election election) {
        List<PartyResult> results = seatResults.stream().map(dto -> createPartyResultEntity(dto, election)).collect(Collectors.toList());
        return partyResultRepository.saveAll(results);
    }

    private PartyResult createPartyResultEntity(PartyDataDTO dto, Election election) {
        Party party = partyRepository.findById(dto.getPartyId())
                .orElseGet(() -> {
                    System.out.println("Saving new party: " + dto.getPartyId() + " (" + dto.getPartyName() + ")");
                    Party newParty = new Party(dto.getPartyId().split("_")[1], dto.getPartyName(), election);
                    return partyRepository.save(newParty);
                });

        PartyResult result = new PartyResult();
        result.setElection(election);
        result.setParty(party);
        result.setSeats(dto.getSeats());
        result.setPercentage(dto.getPercentage());
        result.setTotalVotes(dto.getTotalVotes());
        result.setAreas(1); // Default value, can be updated based on actual data
        return result;
    }

    //Mourad
    public List<KieskringTopPartiesDTO> getTopPartiesByKieskringen(String electionId) {
        List<Object[]> rows = partyKieskringResultRepository.findVotesPerPartyPerKieskring(electionId);

        Map<Integer, List<PartyVotesDTO>> grouped = new HashMap<>();

        for (Object[] row : rows) {
            Integer kieskring = (Integer) row[0];
            String party = (String) row[1];
            Long votes = (Long) row[2];

            grouped.computeIfAbsent(kieskring, k -> new ArrayList<>())
                   .add(new PartyVotesDTO(party, votes));
        }

        // Sorteer top 3
        return grouped.entrySet().stream()
                .map(e -> new KieskringTopPartiesDTO(
                        e.getKey(),
                        e.getValue().stream()
                                .sorted((PartyVotesDTO a, PartyVotesDTO b) -> Long.compare(b.getVotes(), a.getVotes()))
                                .limit(3)
                                .toList()
                ))
                .toList();
    }

    /**
     * Sla de per-kieskring stemmen (gevuld door DutchConstituencyVotesTransformer) op in de database.
     */
    @Transactional
    public void calculateKieskringResults(String electionId, Map<Integer, Map<String, Long>> kieskringData) {
        partyKieskringResultRepository.deleteByElectionId(electionId);

        if (kieskringData == null || kieskringData.isEmpty()) {
            System.out.println("No kieskring votes found for election " + electionId + " – did the constituency files run?");
            return;
        }

        List<PartyKieskringResult> toSave = new ArrayList<>();
        kieskringData.forEach((kieskring, perParty) ->
                perParty.forEach((partyName, votes) ->
                        toSave.add(new PartyKieskringResult(kieskring, partyName, votes, electionId))
                )
        );

        partyKieskringResultRepository.saveAll(toSave);
        System.out.println("Saved " + toSave.size() + " kieskring results for election " + electionId);
    }
}