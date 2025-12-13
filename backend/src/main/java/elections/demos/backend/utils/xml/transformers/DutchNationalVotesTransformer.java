package elections.demos.backend.utils.xml.transformers;

import elections.demos.backend.model.Election;
import elections.demos.backend.utils.xml.VotesTransformer;

import java.util.Map;

/**
 * Just prints to content of electionData to the standard output.>br/>
 * <b>This class needs heavy modification!</b>
 */
public class DutchNationalVotesTransformer implements VotesTransformer {
    private final Election election;

    /**
     * Creates a new transformer for handling the votes at the national level. It expects an instance of
     * Election that can be used for storing the results.
     *
     * @param election the election in which the votes wil be stored.
     */
    public DutchNationalVotesTransformer(Election election) {
        this.election = election;
    }

    @Override
    public void registerPartyVotes(boolean aggregated, Map<String, String> ed) {

        String partyId = firstNonNull(ed.get("AffiliationIdentifier-Id"), ed.get("RegisteredPartyIdentifier-Id"), ed.get("AffiliationIdentifier-ShortCode"), ed.get("AffiliationIdentifier-RegisteredName"));

        String partyName = firstNonNull(ed.get("RegisteredName"), ed.get("AffiliationIdentifier-RegisteredName"), ed.get("RegisteredPartyName"), ed.get("AffiliationIdentifier-ShortCode"), partyId);

        String votesString = firstNonNull(ed.get("ValidVotes"), ed.get("Selection/ValidVotes"));
        if (partyId == null || votesString == null) {
            System.out.println("Skipping party due to missing data: partyId=" + partyId + ", votes=" + votesString);
            return;
        }

        long votes = parseLong(votesString);
        if (votes == 0) {
            System.out.println("Skipping party with zero votes: " + partyName + " (" + partyId + ")");
            return;
        }

        System.out.println("Registering votes for party: " + partyName + " (" + partyId + ") = " + votes);
        election.addPartyVotes(partyId, votes, partyName);
    }

    @Override
    public void registerCandidateVotes(boolean aggregated, Map<String, String> electionData) {
        System.out.printf("%s candidate votes: %s\n", aggregated ? "National" : "Constituency", electionData);
    }

    @Override
    public void registerMetadata(boolean aggregated, Map<String, String> electionData) {
        System.out.printf("%s meta data: %s\n", aggregated ? "National" : "Constituency", electionData);
    }

    private static long parseLong(String s) {
        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private static String firstNonNull(String... ss) {
        for (String s : ss) if (s != null && !s.isBlank()) return s;
        return null;
    }
}
