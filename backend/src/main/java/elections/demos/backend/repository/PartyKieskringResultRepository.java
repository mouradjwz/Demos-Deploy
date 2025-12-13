package elections.demos.backend.repository;

import elections.demos.backend.model.PartyKieskringResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PartyKieskringResultRepository extends JpaRepository<PartyKieskringResult, Long> {

    void deleteByElectionId(String electionId);

    @Query("""
       SELECT p.kieskring, p.party, SUM(p.votes)
       FROM PartyKieskringResult p
       WHERE p.electionId = :electionId
       GROUP BY p.kieskring, p.party
    """)
    List<Object[]> findVotesPerPartyPerKieskring(String electionId);
}
