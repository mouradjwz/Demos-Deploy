package elections.demos.backend.repository;

import elections.demos.backend.model.Election;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ElectionRepository extends JpaRepository<Election, String> {
    Optional<Election> findByElectionId(String electionId);

    boolean existsByElectionId(String electionId);


}