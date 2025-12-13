package elections.demos.backend.service;

import elections.demos.backend.model.Comment;
import elections.demos.backend.model.ForumPost;
import elections.demos.backend.repository.CommentRepository;
import elections.demos.backend.repository.ForumPostRepository;
import elections.demos.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ForumService {
    private final ForumPostRepository forumPostRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public ForumService(ForumPostRepository forumPostRepository, CommentRepository commentRepository, UserRepository userRepository) {
        this.forumPostRepository = forumPostRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    // Aggregate forum statistics
    public elections.demos.backend.dto.ForumStatsDTO getForumStats() {
        long activePosts = forumPostRepository.count();
        long reactions = commentRepository.count();
        long postLikes = forumPostRepository.findAll()
                .stream()
                .mapToLong(p -> p.getLikedBy() == null ? 0 : p.getLikedBy().size())
                .sum();
        long commentLikes = commentRepository.findAll()
                .stream()
                .mapToLong(c -> c.getLikedBy() == null ? 0 : c.getLikedBy().size())
                .sum();
        long likes = postLikes + commentLikes;
        long users = userRepository.count();
        return new elections.demos.backend.dto.ForumStatsDTO(activePosts, reactions, likes, users);
    }

    public List<ForumPost> getAllPosts() {
        return forumPostRepository.findAll();
    }

    // New: fetch posts with optional filters (now supports electionYear, party, region)
    public List<ForumPost> getPosts(Integer electionYear, String party, String region) {
        boolean hasYear = electionYear != null;
        boolean hasParty = party != null && !party.isBlank();
        boolean hasRegion = region != null && !region.isBlank();

        if (!hasYear && !hasParty && !hasRegion) {
            return forumPostRepository.findAll();
        }
        if (hasYear && !hasParty && !hasRegion) {
            return forumPostRepository.findByElectionYear(electionYear);
        }
        if (!hasYear && hasParty && !hasRegion) {
            return forumPostRepository.findByPartyIgnoreCase(party.trim());
        }
        if (!hasYear && !hasParty && hasRegion) {
            return forumPostRepository.findByRegionIgnoreCase(region.trim());
        }
        if (hasYear && hasParty && !hasRegion) {
            return forumPostRepository.findByElectionYearAndParty(electionYear, party.trim());
        }
        if (hasYear && !hasParty && hasRegion) {
            return forumPostRepository.findByElectionYearAndRegion(electionYear, region.trim());
        }
        if (!hasYear && hasParty && hasRegion) {
            return forumPostRepository.findByPartyIgnoreCaseAndRegion(party.trim(), region.trim());
        }
        // all present
        return forumPostRepository.findByElectionYearAndPartyAndRegion(electionYear, party.trim(), region.trim());
    }

    public Optional<ForumPost> getPostById(Long id) {
        return forumPostRepository.findById(id);
    }

    public ForumPost createPost(Map<String, Object> body) {
        String description = (String) body.getOrDefault("excerpt", body.get("description"));
        String title = (String) body.get("title");
        String userId = (String) body.getOrDefault("author", body.get("userId"));
        if (description == null || title == null || userId == null) {
            throw new IllegalArgumentException("Missing required fields");
        }
        ForumPost post = new ForumPost(description, title, userId);
        // read optional metadata
        Object eyObj = body.get("electionYear");
        if (eyObj != null) {
            try {
                if (eyObj instanceof Number) {
                    post.setElectionYear(((Number) eyObj).intValue());
                } else {
                    post.setElectionYear(Integer.parseInt(eyObj.toString()));
                }
            } catch (NumberFormatException ignored) {
                // ignore invalid number format, keep null
            }
        }
        Object partyObj = body.get("party");
        if (partyObj != null) {
            String partyStr = partyObj.toString();
            if (!partyStr.isBlank()) post.setParty(partyStr.trim());
        }
        Object regionObj = body.get("region");
        if (regionObj != null) {
            String regionStr = regionObj.toString();
            if (!regionStr.isBlank()) post.setRegion(regionStr.trim());
        }
        return forumPostRepository.save(post);
    }

    public Comment addComment(Long postId, Map<String, String> body) {
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        String userId = body.get("userId");
        String content = body.get("content");
        if (userId == null || content == null) {
            throw new IllegalArgumentException("Missing fields");
        }
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsForPost(Long postId) {
        return commentRepository.findByPostId(postId);
    }

    public Optional<Comment> getCommentById(Long commentId) {
        return commentRepository.findById(commentId);
    }

    // Reaction methods for posts
    public ForumPost likePost(Long postId, String userId) {
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        post.like(userId);
        return forumPostRepository.save(post);
    }

    public ForumPost dislikePost(Long postId, String userId) {
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        post.dislike(userId);
        return forumPostRepository.save(post);
    }

    public ForumPost removePostReaction(Long postId, String userId) {
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        post.removeReaction(userId);
        return forumPostRepository.save(post);
    }

    // Reaction methods for comments
    public Comment likeComment(Long commentId, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        comment.like(userId);
        return commentRepository.save(comment);
    }

    public Comment dislikeComment(Long commentId, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        comment.dislike(userId);
        return commentRepository.save(comment);
    }

    public Comment removeCommentReaction(Long commentId, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        comment.removeReaction(userId);
        return commentRepository.save(comment);
    }

    // small convenience methods could be added here (delete post, delete comment, update etc.)

    // Delete a post (admin only)
    public void deletePost(Long postId, String requesterEmail) {
        if (requesterEmail == null) {
            throw new RuntimeException("Unauthorized: requester not authenticated");
        }
        var requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("Requester not found"));
        if (!"ADMIN".equalsIgnoreCase(requester.getRole())) {
            throw new RuntimeException("Forbidden: requester is not an admin");
        }
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        forumPostRepository.delete(post);
    }

    // Delete a comment (admin only)
    public void deleteComment(Long commentId, String requesterEmail) {
        if (requesterEmail == null) {
            throw new RuntimeException("Unauthorized: requester not authenticated");
        }
        var requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("Requester not found"));
        if (!"ADMIN".equalsIgnoreCase(requester.getRole())) {
            throw new RuntimeException("Forbidden: requester is not an admin");
        }
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        commentRepository.delete(comment);
    }
}
