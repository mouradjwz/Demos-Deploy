package elections.demos.backend.utils.xml.transformers;

import elections.demos.backend.model.Election;
import elections.demos.backend.utils.xml.VotesTransformer;

import java.util.Map;

/**
 * Just prints to content of electionData to the standard output.>br/>
 * <b>This class needs heavy modification!</b>
 */
public class DutchConstituencyVotesTransformer implements VotesTransformer {
    private final Election election;

    /**
     * Creates a new transformer for handling the votes at the constituency level. It expects an instance of
     * Election that can be used for storing the results.
     * @param election the election in which the votes wil be stored.
     */
    public DutchConstituencyVotesTransformer(Election election) {
        this.election = election;
    }

//     @Override
//     public void registerPartyVotes(boolean aggregated, Map<String, String> electionData) {
// //        System.out.printf("%s party votes: %s\n", aggregated ? "Constituency" : "Municipality", electionData);
//     }

    @Override
    public void registerPartyVotes(boolean aggregated, Map<String, String> electionData) {
        if (!aggregated) {
            return; // alleen geaggregeerde (kieskring) resultaten
        }

        // Kieskringnaam (bijv. "Amsterdam")
        String contestName = electionData.get("ContestName");

        // Partijnaam – in jouw sample stond deze in RegisteredName
        String partyName = firstNonNull(
                electionData.get("RegisteredName"),
                electionData.get("AffiliationIdentifier-RegisteredName")
        );

        String votesStr = electionData.get("ValidVotes");
        long votes = parseLong(votesStr);

        if (partyName == null || votes <= 0L || contestName == null) {
            return;
        }

        // Laat Election het mappen naar de juiste KIESKRING-regio/nummer en aggregeren
        election.addKieskringPartyVotes(contestName, partyName, votes);
    }

    @Override
    public void registerCandidateVotes(boolean aggregated, Map<String, String> electionData) {
//        System.out.printf("%s candidate votes: %s\n", aggregated ? "Constituency" : "Municipality", electionData);
    }

    @Override
    public void registerMetadata(boolean aggregated, Map<String, String> electionData) {
//        System.out.printf("%s meta data: %s\n", aggregated ? "Constituency" : "Municipality", electionData);
    }

    private static long parseLong(String s) {
        try {
            return s == null ? 0L : Long.parseLong(s);
        } catch (Exception e) {
            return 0L;
        }
    }

    private static String firstNonNull(String... ss) {
        if (ss == null) return null;
        for (String s : ss) {
            if (s != null && !s.isBlank()) return s.trim();
        }
        return null;
    }
}
