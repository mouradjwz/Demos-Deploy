package elections.demos.backend.repository;

import elections.demos.backend.model.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
    List<ForumPost> findByElectionYear(Integer electionYear);
    List<ForumPost> findByPartyIgnoreCase(String party);
    List<ForumPost> findByRegionIgnoreCase(String region);
    List<ForumPost> findByElectionYearAndParty(Integer electionYear, String party);
    List<ForumPost> findByElectionYearAndRegion(Integer electionYear, String region);
    List<ForumPost> findByPartyIgnoreCaseAndRegion(String party, String region);
    List<ForumPost> findByElectionYearAndPartyAndRegion(Integer electionYear, String party, String region);
}
