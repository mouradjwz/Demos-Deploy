package elections.demos.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.HashSet;
import java.util.Set;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "forum_post")
public class ForumPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "description", nullable = false, length = 2048)
    private String description;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "user_id", nullable = false)
    private String userId;

    // new optional metadata fields for filtering
    @Column(name = "election_year")
    private Integer electionYear;

    @Column(name = "party", length = 255)
    private String party;

    @Column(name = "region", length = 255)
    private String region;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private java.util.List<Comment> comments = new java.util.ArrayList<>();

    // Track which users liked / disliked this post. Use ElementCollection so it is persisted to separate tables.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "post_likes", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "user_id")
    private Set<String> likedBy = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "post_dislikes", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "user_id")
    private Set<String> dislikedBy = new HashSet<>();

    public ForumPost() {
        this.createdAt = LocalDateTime.now();
    }

    public ForumPost(String description, String title, String userId) {
        this.createdAt = LocalDateTime.now();
        this.description = description;
        this.title = title;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    // New getters/setters for filterable metadata
    public Integer getElectionYear() { return electionYear; }
    public void setElectionYear(Integer electionYear) { this.electionYear = electionYear; }

    public String getParty() { return party; }
    public void setParty(String party) { this.party = party; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public java.util.List<Comment> getComments() { return comments; }
    public void setComments(java.util.List<Comment> comments) { this.comments = comments; }

    // Likes/dislikes helpers
    public Set<String> getLikedBy() { return likedBy; }
    public void setLikedBy(Set<String> likedBy) { this.likedBy = likedBy; }

    public Set<String> getDislikedBy() { return dislikedBy; }
    public void setDislikedBy(Set<String> dislikedBy) { this.dislikedBy = dislikedBy; }

    public int getLikeCount() { return likedBy == null ? 0 : likedBy.size(); }
    public int getDislikeCount() { return dislikedBy == null ? 0 : dislikedBy.size(); }

    public void like(String userId) {
        if (userId == null) return;
        // remove from dislikes if present
        if (dislikedBy != null) dislikedBy.remove(userId);
        if (likedBy == null) likedBy = new HashSet<>();
        likedBy.add(userId);
    }

    public void dislike(String userId) {
        if (userId == null) return;
        if (likedBy != null) likedBy.remove(userId);
        if (dislikedBy == null) dislikedBy = new HashSet<>();
        dislikedBy.add(userId);
    }

    public void removeReaction(String userId) {
        if (userId == null) return;
        if (likedBy != null) likedBy.remove(userId);
        if (dislikedBy != null) dislikedBy.remove(userId);
    }
}
