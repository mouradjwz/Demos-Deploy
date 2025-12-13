package elections.demos.backend.utils.xml.transformers;

import elections.demos.backend.model.Election;
import elections.demos.backend.model.Party;
import elections.demos.backend.model.PartyResult;
import elections.demos.backend.utils.xml.VotesTransformer;

import java.util.Map;
import java.util.Optional;

public class DutchPartyTransformer implements VotesTransformer {
    private final Election election;

    public DutchPartyTransformer(Election election) {
        if (election == null) {
            throw new IllegalArgumentException("Election cannot be null");
        }
        this.election = election;
    }

    @Override
    public void registerPartyVotes(boolean aggregated, Map<String, String> ed) {
        if (!aggregated) {
            System.out.println("Skipping non-aggregated votes");
            return;
        }

        String partyId = extractPartyId(ed);
        if (partyId == null) {
            System.out.println("No party ID found, skipping");
            return;
        }

        String partyName = firstNonNull(
                ed.get("RegisteredName"),
                ed.get("AffiliationIdentifier-RegisteredName"),
                ed.get("RegisteredPartyName"),
                ed.get("AffiliationIdentifier-ShortCode"),
                partyId
        );

        String votesString = firstNonNull(
                ed.get("ValidVotes"),
                ed.get("Selection/ValidVotes")
        );
        if (votesString == null) {
            System.out.println("No votes found for party: " + partyId);
            return;
        }

        long votes = parseLong(votesString);
        if (votes == 0) {
            System.out.println("Votes for party " + partyId + " are zero, skipping");
            return;
        }

        System.out.println("Processing votes for party: " + partyId + " - " + partyName + " = " + votes);

        createOrAccumulatePartyResult(partyId, partyName, votes);
    }

    @Override
    public void registerCandidateVotes(boolean aggregated, Map<String, String> ed) {
        // Not implemented for now
    }

    @Override
    public void registerMetadata(boolean aggregated, Map<String, String> ed) {
        // Not implemented for now
    }

    private String extractPartyId(Map<String, String> ed) {
        String basePartyId = firstNonNull(
                ed.get("AffiliationIdentifier-Id"),
                ed.get("RegisteredPartyIdentifier-Id"),
                ed.get("AffiliationIdentifier-ShortCode"),
                ed.get("AffiliationIdentifier-RegisteredName")
        );

        if (basePartyId != null) {
            String electionScopedId = election.getElectionId() + "_" + basePartyId;
            System.out.println("Created election-scoped party ID: " + electionScopedId);
            return electionScopedId;
        }

        return null;
    }

    private void createOrAccumulatePartyResult(String partyId, String partyName, long votes) {
        Optional<Party> partyOptional = election.getParties().stream()
                .filter(p -> p.getId().equals(partyId))
                .findFirst();

        Party party = partyOptional.orElseGet(() -> {
            Party newParty = new Party(partyId.split("_")[1], partyName, election);
            election.addParty(newParty);
            System.out.println("Created new party: " + partyName + " (" + partyId + ")");
            return newParty;
        });

        Optional<PartyResult> existingResult = election.getPartyResults().stream()
                .filter(pr -> pr.getParty() != null && partyId.equals(pr.getParty().getId()))
                .findFirst();

        if (existingResult.isPresent()) {
            PartyResult result = existingResult.get();
            long oldVotes = result.getTotalVotes();
            result.setTotalVotes(oldVotes + votes);
            System.out.println("Accumulated votes for " + partyName + ": " + oldVotes + " + " + votes + " = " + result.getTotalVotes());
        } else {
            PartyResult newResult = new PartyResult();
            newResult.setElection(election);
            newResult.setParty(party);
            newResult.setTotalVotes(votes);
            newResult.setAreas(1);

            party.addPartyResult(newResult);
            election.addPartyResult(newResult);

            System.out.println("Created PartyResult for " + partyName + " with " + votes + " votes.");
        }
    }

    private static long parseLong(String s) {
        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private static String firstNonNull(String... ss) {
        for (String s : ss) {
            if (s != null && !s.isBlank()) {
                return s.trim();
            }
        }
        return null;
    }
}