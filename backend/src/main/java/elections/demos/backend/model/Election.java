package elections.demos.backend.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.*;


/**
 * This will hold the information for one specific election.<br/>
 * <b>This class is by no means production ready! You need to alter it extensively!</b>
 * <Election>
 * <ElectionIdentifier Id="TK2023">
 * <ElectionName>Tweede Kamer der Staten-Generaal 2023</ElectionName>
 * <ElectionCategory>TK</ElectionCategory>
 * <kr:ElectionSubcategory>TK</kr:ElectionSubcategory>
 * <kr:ElectionDate>2023-11-22</kr:ElectionDate>
 * </ElectionIdentifier>
 */
@Entity
@Table(name = "election")
public class Election {
    @Id
    @Column(unique = true, nullable = false)
    private String electionId;

    private String electionName;
    private String electionCategory;
    private String electionSubCategory;
    private String votingMethod;
    private final int numberOfSeats = 150;
    private int issueDate;

    @OneToMany(mappedBy = "election", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Party> parties = new ArrayList<>();

    @OneToMany(mappedBy = "election", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Region> regions = new ArrayList<>();

    @OneToMany(mappedBy = "election", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartyResult> partyResults = new ArrayList<>();

    // ======== Transient rekenbuffers (niet in DB) ========
    @Transient
    private final Map<String, Long> votesPerParty = new HashMap<>();

    @Transient
    private final Map<String, String> namePerParty = new HashMap<>();

    // kieskring -> (partyName -> votes)
    @Transient
    private final Map<Integer, Map<String, Long>> kieskringPartyVotes = new HashMap<>();

    @Transient
    private long totalValidVotes = 0L;

    public Election() {
    }

    public Election(String electionId) {
        this.electionId = electionId;
    }

    public Election(String electionId, String electionName) {
        this.electionId = electionId;
        this.electionName = electionName;
    }

    public void addParty(Party party) {
        if (!parties.contains(party)) {
            parties.add(party);
            party.setElection(this);
        }
    }

    public void addRegion(Region region) {
        if (!regions.contains(region)) {
            regions.add(region);
            region.setElection(this);
        }
    }

    public String getElectionId() {
        return electionId;
    }

    public void setElectionId(String electionId) {
        this.electionId = electionId;
    }

    public String getElectionName() {
        return electionName;
    }

    public void setElectionName(String electionName) {
        this.electionName = electionName;
    }

    public String getElectionCategory() {
        return electionCategory;
    }

    public void setElectionCategory(String electionCategory) {
        this.electionCategory = electionCategory;
    }

    public String getElectionSubCategory() {
        return electionSubCategory;
    }

    public void setElectionSubCategory(String electionSubCategory) {
        this.electionSubCategory = electionSubCategory;
    }

    public String getVotingMethod() {
        return votingMethod;
    }

    public void setVotingMethod(String votingMethod) {
        this.votingMethod = votingMethod;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public int getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(int issueDate) {
        this.issueDate = issueDate;
    }

    public List<Party> getParties() {
        return parties;
    }

    public void setParties(List<Party> parties) {
        this.parties = parties;
    }

    public List<PartyResult> getPartyResults() {
        return partyResults;
    }

    public void setPartyResults(List<PartyResult> partyResults) {
        this.partyResults = partyResults;
    }

    public void addPartyResult(PartyResult partyResult) {
        if (partyResult != null) {
            partyResult.setElection(this);
            partyResults.add(partyResult);
        }
    }

    public List<Region> getRegions() {
        return regions;
    }

    public void setRegions(List<Region> regions) {
        this.regions = regions;
    }

    // ======== API voor parser/service ========

    /**
     * Voeg stemmen toe tijdens het parsen (accumuleert per partij).
     */
    public void addPartyVotes(String partyId, long votes, String partyName) {
        if (partyId == null) return;
        votesPerParty.merge(partyId, votes, Long::sum);
        if (partyName != null && !partyName.isBlank()) {
            namePerParty.put(partyId, partyName);
        }
        totalValidVotes += votes;
    }

    /**
     * Voeg stemmen toe op kieskring-niveau tijdens het parsen.
     * Gebruikt de ContestName (bijv. "Amsterdam") om de juiste KIESKRING-regio te vinden.
     */
    public void addKieskringPartyVotes(String contestName, String partyName, long votes) {
        if (contestName == null || partyName == null || votes <= 0) {
            return;
        }

        // Zoek bijbehorende KIESKRING-regio op basis van naam
        Region match = regions.stream()
                .filter(r -> "KIESKRING".equalsIgnoreCase(r.getRegionCategory()))
                .filter(r -> r.getRegionName() != null && r.getRegionName().equalsIgnoreCase(contestName))
                .findFirst()
                .orElse(null);

        if (match == null || match.getRegionNumber() == null) {
            return; // geen passende regio gevonden
        }

        Integer kk = match.getRegionNumber();
        kieskringPartyVotes
                .computeIfAbsent(kk, x -> new HashMap<>())
                .merge(partyName, votes, Long::sum);
    }

    public Map<Integer, Map<String, Long>> getKieskringPartyVotes() {
        return kieskringPartyVotes;
    }

    /**
     * Onveranderbare view op stemmen per partij (alleen voor rekenen).
     */
    public Map<String, Long> getVotesPerParty() {
        return Collections.unmodifiableMap(votesPerParty);
    }

    /**
     * Onveranderbare view op partijnamen per partij-id (alleen voor tonen/DTO).
     */
    public Map<String, String> getNamePerParty() {
        return Collections.unmodifiableMap(namePerParty);
    }

    /**
     * Totaal aantal geldige stemmen (voor percentage-berekening).
     */
    public long getTotalValidVotes() {
        return totalValidVotes;
    }

}
