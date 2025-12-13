package elections.demos.backend.utils.xml.transformers;

import elections.demos.backend.model.Candidate;
import elections.demos.backend.model.Election;
import elections.demos.backend.model.Party;
import elections.demos.backend.utils.xml.CandidateTransformer;

import java.util.HashMap;
import java.util.Map;

public class DutchCandidateTransformer implements CandidateTransformer {
    private final Election election;
    private final Map<String, Party> partyCache = new HashMap<>();
    private final Map<String, Candidate> candidateCache = new HashMap<>();

    public DutchCandidateTransformer(Election election) {
        this.election = election;
    }

    @Override
    public void registerCandidate(Map<String, String> electionData) {
        String candidateId = electionData.get("CandidateIdentifier-Id");
        String partyCode = electionData.get("AffiliationIdentifier-Id");

        if (candidateId == null || candidateId.isBlank() || partyCode == null || partyCode.isBlank()) {
            System.out.println("Missing or invalid candidate ID or party code, skipping.");
            return;
        }

        String uniqueKey = partyCode + "_" + candidateId;

        if (candidateCache.containsKey(uniqueKey)) {
            System.out.println("Candidate already exists in cache: " + uniqueKey);
            return;
        }

        Candidate existingCandidate = findExistingCandidate(candidateId, partyCode);
        if (existingCandidate != null) {
            candidateCache.put(uniqueKey, existingCandidate);
            System.out.println("Candidate already exists: " + uniqueKey);
            return;
        }

        String partyName = electionData.get("RegisteredName");
        String initials = electionData.get("NamePrefix");
        String firstName = electionData.get("FirstName");
        String lastName = electionData.get("LastName");
        String gender = electionData.get("Gender");
        String localityName = electionData.get("LocalityName");

        Party party = findOrCreateParty(partyCode, partyName);

        Candidate candidate = new Candidate(candidateId, election, initials, firstName, lastName, gender, localityName, party);
        party.addCandidate(candidate);
        candidateCache.put(uniqueKey, candidate);

        System.out.println("Registered candidate: " + uniqueKey + " (" + firstName + " " + lastName + ")");
    }

    private Candidate findExistingCandidate(String candidateId, String partyCode) {
        return election.getParties().stream()
                .filter(party -> party.getPartyCode().equals(partyCode))
                .flatMap(party -> party.getCandidates().stream())
                .filter(candidate -> candidate.getId().equals(candidateId))
                .findFirst()
                .orElse(null);
    }

    private Party findOrCreateParty(String partyCode, String partyName) {
        if (partyCache.containsKey(partyCode)) {
            Party cachedParty = partyCache.get(partyCode);
            if (!cachedParty.getName().equals(partyName)) {
                System.out.println("Warning: Party name mismatch for code " + partyCode +
                        ". Cached: " + cachedParty.getName() + ", XML: " + partyName);
            }
            return cachedParty;
        }

        for (Party existingParty : election.getParties()) {
            if (existingParty.getPartyCode() != null && existingParty.getPartyCode().equals(partyCode)) {
                if (!existingParty.getName().equals(partyName)) {
                    System.out.println("Warning: Party name mismatch for code " + partyCode +
                            ". Database: " + existingParty.getName() + ", XML: " + partyName);
                }
                partyCache.put(partyCode, existingParty);
                return existingParty;
            }
        }

        Party newParty = new Party(partyCode, partyName, election);
        election.addParty(newParty);
        partyCache.put(partyCode, newParty);

        System.out.println("Created new party: " + newParty.getName() + " (" + partyCode + ")");
        return newParty;
    }
}