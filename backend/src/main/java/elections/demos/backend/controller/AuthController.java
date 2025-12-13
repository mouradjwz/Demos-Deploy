package elections.demos.backend.controller;

import elections.demos.backend.dto.LoginRequest;
import elections.demos.backend.dto.LoginResponse;
import elections.demos.backend.dto.RegisterRequest;
import elections.demos.backend.model.User;
import elections.demos.backend.service.UserService;
import elections.demos.backend.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = {"Content-Type", "Authorization"})
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword()
            );

            String token = jwtUtil.generateToken(user.getEmail());
            LoginResponse response = new LoginResponse(user.getEmail(), token, user.getUsername(), user.getRole());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.loginUser(request.getEmail(), request.getPassword());
            String token = jwtUtil.generateToken(user.getEmail());

            System.out.println(user);
            System.out.println(token);
            
            LoginResponse response = new LoginResponse(user.getEmail(), token, user.getUsername(), user.getRole());

            System.out.println(response);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
}
