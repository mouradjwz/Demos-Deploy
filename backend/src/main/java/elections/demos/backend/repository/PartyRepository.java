package elections.demos.backend.repository;

import elections.demos.backend.model.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartyRepository extends JpaRepository<Party, String> {
    List<Party> findByElectionElectionId(String electionId);

    Optional<Party> findByName(String name);
}
