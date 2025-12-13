package elections.demos.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.util.HashSet;
import java.util.Set;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "comment")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonBackReference
    private ForumPost post;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Reaction tracking for comments
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "comment_likes", joinColumns = @JoinColumn(name = "comment_id"))
    @Column(name = "user_id")
    private Set<String> likedBy = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "comment_dislikes", joinColumns = @JoinColumn(name = "comment_id"))
    @Column(name = "user_id")
    private Set<String> dislikedBy = new HashSet<>();

    public Comment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ForumPost getPost() { return post; }
    public void setPost(ForumPost post) { this.post = post; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Likes/dislikes helpers for comments
    public Set<String> getLikedBy() { return likedBy; }
    public void setLikedBy(Set<String> likedBy) { this.likedBy = likedBy; }

    public Set<String> getDislikedBy() { return dislikedBy; }
    public void setDislikedBy(Set<String> dislikedBy) { this.dislikedBy = dislikedBy; }

    public int getLikeCount() { return likedBy == null ? 0 : likedBy.size(); }
    public int getDislikeCount() { return dislikedBy == null ? 0 : dislikedBy.size(); }

    public void like(String userId) {
        if (userId == null) return;
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
