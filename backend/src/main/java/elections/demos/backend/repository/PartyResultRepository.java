package elections.demos.backend.repository;

import elections.demos.backend.model.Election;
import elections.demos.backend.model.Party;
import elections.demos.backend.model.PartyResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PartyResultRepository extends JpaRepository<PartyResult, Long> {
    List<PartyResult> findByElection(Election election);
    List<PartyResult> findByElection_ElectionId(String electionId);
    List<PartyResult> findByParty(Party party);

    @Modifying
    @Query("DELETE FROM PartyResult pr WHERE pr.election.id = :electionId")
    void deleteByElectionId(@Param("electionId") String electionId);

    @Query("""
    SELECT r.regionNumber, p.name, SUM(pr.totalVotes)
    FROM PartyResult pr
    JOIN pr.party p
    JOIN pr.election e
    JOIN Region r ON r.election = e
    WHERE e.electionId = :electionId
      AND r.regionCategory = 'KIESKRING'
    GROUP BY r.regionNumber, p.name
    ORDER BY r.regionNumber ASC, SUM(pr.totalVotes) DESC
""")
List<Object[]> findVotesPerPartyPerKieskring(String electionId);
}