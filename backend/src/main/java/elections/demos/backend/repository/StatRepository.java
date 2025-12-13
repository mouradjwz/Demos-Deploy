package elections.demos.backend.repository;

import elections.demos.backend.model.Stat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatRepository extends JpaRepository<Stat, Long> {}
