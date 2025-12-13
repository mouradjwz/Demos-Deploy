package elections.demos.backend.utils.xml.transformers;

import elections.demos.backend.model.Election;
import elections.demos.backend.model.Party;
import elections.demos.backend.model.Region;
import elections.demos.backend.utils.xml.DefinitionTransformer;

import java.util.HashMap;
import java.util.Map;

/**
 * Just prints to content of electionData to the standard output.>br/>
 * <b>This class needs heavy modification!</b>
 */
public class DutchDefinitionTransformer implements DefinitionTransformer {
    private final Election election;
    private final Map<String, Region> regionCache = new HashMap<>();
    private final Map<String, Party> partyCache = new HashMap<>();

    /**
     * Creates a new transformer for handling the structure of the constituencies, municipalities and the parties.
     * It expects an instance of Election that can be used for storing the results.
     *
     * @param election the election in which the votes wil be stored.
     */
    public DutchDefinitionTransformer(Election election) {
        this.election = election;
    }

    /**
     * </Contest>
     * <kr:NumberOfSeats>150</kr:NumberOfSeats>
     * <kr:PreferenceThreshold>25</kr:PreferenceThreshold>
     * <kr:ElectionTree>
     * <kr:Region RegionCategory="STAAT">
     * <kr:RegionName>Nederland</kr:RegionName>
     * </kr:Region>
     * <kr:Region RegionNumber="1" RegionCategory="KIESKRING" SuperiorRegionCategory="STAAT">
     * <kr:RegionName>Groningen</kr:RegionName>
     * <kr:Committee CommitteeCategory="HSB"/>
     * </kr:Region>
     * <kr:Region RegionNumber="2" RegionCategory="KIESKRING" SuperiorRegionCategory="STAAT">
     * <kr:RegionName>Leeuwarden</kr:RegionName>
     * <kr:Committee CommitteeCategory="HSB"/>
     * </kr:Region>
     * <kr:Region RegionNumber="3" RegionCategory="KIESKRING" SuperiorRegionCategory="STAAT">
     * <kr:RegionName>Assen</kr:RegionName>
     * <kr:Committee CommitteeCategory="HSB"/>
     * </kr:Region>
     * <kr:Region RegionNumber="4" RegionCategory="KIESKRING" SuperiorRegionCategory="STAAT">
     * <kr:RegionName>Zwolle</kr:RegionName>
     * <kr:Committee CommitteeCategory="HSB"/>
     * </kr:Region>
     * <kr:Region RegionNumber="5" RegionCategory="KIESKRING" SuperiorRegionCategory="STAAT">
     * <kr:RegionName>Lelystad</kr:RegionName>
     * <kr:Committee CommitteeCategory="HSB"/>
     * </kr:Region>
     * <kr:Region RegionNumber="6" RegionCategory="KIESKRING" SuperiorRegionCategory="STAAT">
     * <kr:RegionName>Nijmegen</kr:RegionName>
     * <kr:Committee CommitteeCategory="HSB"/>
     * </kr:Region>
     * <kr:Region RegionNumber="7" RegionCategory="KIESKRING" SuperiorRegionCategory="STAAT">
     * <kr:RegionName>Arnhem</kr:RegionName>
     * <kr:Committee CommitteeCategory="HSB"/>
     * </kr:Region>
     * <kr:Region RegionNumber="8" RegionCategory="KIESKRING" SuperiorRegionCategory="STAAT">
     * <kr:RegionName>Utrecht</kr:RegionName>
     * <kr:Committee CommitteeCategory="HSB"/>
     * </kr:Region>
     * <kr:Region RegionNumber="9" RegionCategory="KIESKRING" SuperiorRegionCategory="STAAT">
     * <kr:RegionName>Amsterdam</kr:RegionName>
     * <kr:Committee CommitteeCategory="HSB"/>
     * </kr:Region>
     * <kr:Region RegionNumber="10" RegionCategory="KIESKRING" SuperiorRegionCategory="STAAT">
     * <kr:RegionName>Haarlem</kr:RegionName>
     * <kr:Committee CommitteeCategory="HSB"/>
     * </kr:Region>
     * <kr:Region RegionNumber="11" RegionCategory="KIESKRING" SuperiorRegionCategory="STAAT">
     * <kr:RegionName>Den Helder</kr:RegionName>
     * <kr:Committee CommitteeCategory="HSB"/>
     * </kr:Region>
     */
//                <kr:Region RegionCategory="STAAT">
//            <kr:RegionName>Nederland</kr:RegionName>
//                </kr:Region>
//                <kr:Region RegionNumber="1" RegionCategory="KIESKRING" SuperiorRegionCategory="STAAT">
//                    <kr:RegionName>Groningen</kr:RegionName>
//                    <kr:Committee CommitteeCategory="HSB"/>
//            </kr:Region>
//                <kr:Region RegionNumber="2" RegionCategory="KIESKRING" SuperiorRegionCategory="STAAT">
//                    <kr:RegionName>Leeuwarden</kr:RegionName>
//                    <kr:Committee CommitteeCategory="HSB"/>
//            </kr:Region>
//                <kr:Region RegionNumber="3" RegionCategory="KIESKRING" SuperiorRegionCategory="STAAT">
//                    <kr:RegionName>Assen</kr:RegionName>
//                    <kr:Committee CommitteeCategory="HSB"/>
//            </kr:Region>
//                <kr:Region RegionNumber="4" RegionCategory="KIESKRING" SuperiorRegionCategory="STAAT">
//                    <kr:RegionName>Zwolle</kr:RegionName>
//                    <kr:Committee CommitteeCategory="HSB"/>
//            </kr:Region>
    @Override
    public void registerRegion(Map<String, String> electionData) {
        String regionNumber = electionData.get("Region-RegionNumber");
        String regionCategory = electionData.get("Region-RegionCategory");
        String regionName = electionData.get("RegionName");
        String ElectionName = electionData.get("ElectionName");
        election.setElectionName(ElectionName);
        String superiorRegionCategory = electionData.get("Region-SuperiorRegionCategory");
        String committeeCategory = electionData.get("Committee-CommitteeCategory");

        if (regionCategory != null && regionName != null) {
            Integer regionNum = null;
            if (regionNumber != null && !regionNumber.isEmpty()) {
                regionNum = Integer.parseInt(regionNumber);
            }

            Region region = new Region(election.getElectionId(), regionNum, regionCategory, regionName);
            region.setSuperiorRegionCategory(superiorRegionCategory);
            region.setCommitteeCategory(committeeCategory);

            election.addRegion(region);
            String regionId = region.getId();
            regionCache.put(regionId, region);

            System.out.println("Registered region: " + regionName + " (" + regionCategory + ")");
        }
    }

    @Override
    public void registerParty(Map<String, String> electionData) {
        String partyCode = electionData.get("AffiliationIdentifier-Id");
        String partyName = electionData.get("RegisteredName");

        if (partyCode == null || partyCode.isBlank()) {
            System.out.println("Skipping entry: Missing or invalid party code.");
            return;
        }

        if (partyName == null || partyName.isBlank()) {
            System.out.println("Skipping entry: Missing or invalid party name for code: " + partyCode);
            return;
        }

        // Check if the party already exists in the cache
        if (partyCache.containsKey(partyCode)) {
            System.out.println("Party already registered: " + partyName + " (" + partyCode + ")");
            return;
        }

        // Create and register the new party
        Party party = new Party(partyCode, partyName, election);
        election.addParty(party);
        partyCache.put(partyCode, party);

        System.out.println("Registered party: " + partyName + " (" + partyCode + ")");
    }
}