package elections.demos.backend.controller;

import elections.demos.backend.model.Comment;
import elections.demos.backend.model.ForumPost;
import elections.demos.backend.service.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

/**
 * Minimal Forum controller to route post creation through the backend.
 * Posting is enabled via the service layer which keeps controller thin.
 */
@RestController
@RequestMapping("forum")
@CrossOrigin(origins = "*", allowedHeaders = {"Content-Type", "Authorization"})
public class ForumController {
    private static final Logger logger = LoggerFactory.getLogger(ForumController.class);

    @Autowired
    private ForumService forumService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "Forum backend reachable"
        ));
    }

    @GetMapping("/posts")
    public ResponseEntity<List<ForumPost>> getPosts(@RequestParam(required = false) Integer electionYear,
                                                    @RequestParam(required = false) String party,
                                                    @RequestParam(required = false) String region) {
        List<ForumPost> posts = forumService.getPosts(electionYear, party, region);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/stats")
    public ResponseEntity<elections.demos.backend.dto.ForumStatsDTO> getStats() {
        return ResponseEntity.ok(forumService.getForumStats());
    }

    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        logger.info("Received createPost request from {} with Authorization={}", request.getRemoteAddr(), request.getHeader("Authorization"));
        logger.debug("createPost body (before override): {}", body);
        // If the request is authenticated, force the post author to the authenticated principal
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String authUser = auth.getName();
            // overwrite any provided author/userId to avoid spoofing from client
            body.put("author", authUser);
            body.put("userId", authUser);
            logger.debug("Overriding post author to authenticated user: {}", authUser);
        }
        try {
            ForumPost saved = forumService.createPost(body);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<?> getPostById(@PathVariable Long id) {
        return forumService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/posts/{id}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        logger.info("Received addComment request for post {} from {} with Authorization={}", id, request.getRemoteAddr(), request.getHeader("Authorization"));
        logger.debug("addComment body (before override): {}", body);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String authUser = auth.getName();
            body.put("userId", authUser);
            logger.debug("Overriding comment userId to authenticated user: {}", authUser);
        }
        return forumService.getPostById(id).map(post -> {
            try {
                Comment comment = forumService.addComment(id, body);
                return ResponseEntity.ok(comment);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
            }
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/posts/{id}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(forumService.getCommentsForPost(id));
    }

    // helper to resolve user id: prefer authenticated principal, then optional request body userId, else fallback to anon based on remote address
    private String resolveUserId(HttpServletRequest request, Map<String, String> body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return auth.getName();
        }
        if (body != null) {
            String userId = body.get("userId");
            if (userId != null && !userId.isBlank()) return userId;
        }
        String ip = request != null ? request.getRemoteAddr() : "unknown";
        // normalize ip for safe storage
        String norm = ip.replaceAll("[^a-zA-Z0-9.-]", "_");
        return "anon-" + norm;
    }

    // --- Reaction endpoints for posts ---
    @PostMapping("/posts/{id}/like")
    public ResponseEntity<?> likePost(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body, HttpServletRequest request) {
        String user = resolveUserId(request, body);
        try {
            ForumPost updated = forumService.likePost(id, user);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/posts/{id}/dislike")
    public ResponseEntity<?> dislikePost(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body, HttpServletRequest request) {
        String user = resolveUserId(request, body);
        try {
            ForumPost updated = forumService.dislikePost(id, user);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/posts/{id}/reaction")
    public ResponseEntity<?> removePostReaction(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body, HttpServletRequest request) {
        String user = resolveUserId(request, body);
        try {
            ForumPost updated = forumService.removePostReaction(id, user);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // --- Reaction endpoints for comments ---
    @PostMapping("/posts/{postId}/comments/{commentId}/like")
    public ResponseEntity<?> likeComment(@PathVariable Long postId, @PathVariable Long commentId, @RequestBody(required = false) Map<String, String> body, HttpServletRequest request) {
        String user = resolveUserId(request, body);
        try {
            Comment updated = forumService.likeComment(commentId, user);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/posts/{postId}/comments/{commentId}/dislike")
    public ResponseEntity<?> dislikeComment(@PathVariable Long postId, @PathVariable Long commentId, @RequestBody(required = false) Map<String, String> body, HttpServletRequest request) {
        String user = resolveUserId(request, body);
        try {
            Comment updated = forumService.dislikeComment(commentId, user);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}/reaction")
    public ResponseEntity<?> removeCommentReaction(@PathVariable Long postId, @PathVariable Long commentId, @RequestBody(required = false) Map<String, String> body, HttpServletRequest request) {
        String user = resolveUserId(request, body);
        try {
            Comment updated = forumService.removeCommentReaction(commentId, user);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // --- Admin-only delete endpoints ---
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String requesterEmail = null;
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            requesterEmail = auth.getName();
        }
        try {
            forumService.deletePost(id, requesterEmail);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.contains("Unauthorized")) return ResponseEntity.status(401).body(Map.of("message", msg));
            if (msg.contains("Forbidden")) return ResponseEntity.status(403).body(Map.of("message", msg));
            if (msg.toLowerCase().contains("not found")) return ResponseEntity.notFound().build();
            return ResponseEntity.badRequest().body(Map.of("message", msg));
        }
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long postId, @PathVariable Long commentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String requesterEmail = null;
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            requesterEmail = auth.getName();
        }
        try {
            forumService.deleteComment(commentId, requesterEmail);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.contains("Unauthorized")) return ResponseEntity.status(401).body(Map.of("message", msg));
            if (msg.contains("Forbidden")) return ResponseEntity.status(403).body(Map.of("message", msg));
            if (msg.toLowerCase().contains("not found")) return ResponseEntity.notFound().build();
            return ResponseEntity.badRequest().body(Map.of("message", msg));
        }
    }
}
