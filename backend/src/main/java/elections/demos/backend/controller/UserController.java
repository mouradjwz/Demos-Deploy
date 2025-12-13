package elections.demos.backend.controller;

import elections.demos.backend.model.User;
import elections.demos.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;


@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService service){
        this.userService = service;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.createUser(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Update the username based on their email address
    @PutMapping("/username")
    public ResponseEntity<?> updateUsernameByEmail(
            @RequestParam String email,
            @RequestBody Map<String, String> body) {

        // Extract new username from JSON payload
        String newUsername = body.get("username");

        // Input validation — voorkomt lege of ongeldige updates
        if (newUsername == null || newUsername.isBlank()) {
            log.warn("Bad request on update username: email='{}', username='{}'", email, newUsername);

            // Returned as 400 omdat het probleem ligt bij de client
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "Username cannot be empty",
                            "status", 400
                    ));
        }

        // minimaal 4 tekens vereist
        if (newUsername.trim().length() < 4) {
            log.warn("Username too short: email='{}', username='{}'", email, newUsername);

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "Username must be at least 4 characters",
                            "status", 400
                    ));
        }

        try {
            // slaat hem op in de database
            User updated = userService.updateUsernameByEmail(email, newUsername);

            log.info("Username updated successfully: email='{}' → username='{}'",
                    email, newUsername);

            // 200 OK — request succesvol uitgevoerd
            return ResponseEntity.ok(updated);

        } catch (RuntimeException ex) {
            // Bijvoorbeeld: user met dit emailadres bestaat niet
            log.error("Failed to update username for email='{}': {}", email, ex.getMessage());

            // 404 Not Found — resource bestaat niet
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error", ex.getMessage(),
                            "status", 404
                    ));
        }
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String requesterEmail = null;
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            requesterEmail = auth.getName();
        }

        String newRole = body.get("role");
        if (newRole == null || newRole.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Role cannot be empty", "status", 400));
        }

        try {
            User updated = userService.updateUserRole(id, newRole, requesterEmail);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.contains("Unauthorized")) return ResponseEntity.status(401).body(msg);
            if (msg.contains("Forbidden")) return ResponseEntity.status(403).body(msg);
            if (msg.contains("Invalid role")) return ResponseEntity.status(400).body(msg);
            return ResponseEntity.badRequest().body(msg);
        }
    }
}