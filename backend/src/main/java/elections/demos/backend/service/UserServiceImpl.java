package elections.demos.backend.service;

import elections.demos.backend.model.User;
import elections.demos.backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id " + id));
    }

    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, User userDetails) {
        User currentUser = getUserById(id);
        // Alleen username aanpassen als er iets is meegestuurd
        if (userDetails.getUsername() != null && !userDetails.getUsername().isBlank()) {
            currentUser.setUsername(userDetails.getUsername());
        }
        currentUser.setEmail(userDetails.getEmail());
        currentUser.setPassword(userDetails.getPassword());

        return userRepository.save(currentUser);
    }
    // updates user by email
    @Override
    public User updateUsernameByEmail(String email, String newUsername) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email " + email));

        user.setUsername(newUsername);
        return userRepository.save(user);
    }



    @Override
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

    @Override
    public User registerUser(String username, String email, String password) {
        // Check if user already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User with this email already exists");
        }

        // Hash the password
        String hashedPassword = passwordEncoder.encode(password);

        // Create and save new user
        User newUser = new User(username, email, hashedPassword);
        return userRepository.save(newUser);
    }

    @Override
    public User loginUser(String email, String password) {
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        return user;
    }

    @Override
    public User updateUserRole(Long id, String newRole, String requesterEmail) {
        if (requesterEmail == null) {
            throw new RuntimeException("Unauthorized: requester not authenticated");
        }
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("Requester not found"));
        if (!"ADMIN".equalsIgnoreCase(requester.getRole())) {
            throw new RuntimeException("Forbidden: requester is not an admin");
        }

        if (!"USER".equalsIgnoreCase(newRole) && !"ADMIN".equalsIgnoreCase(newRole)) {
            throw new RuntimeException("Invalid role: must be USER or ADMIN");
        }

        User user = getUserById(id);
        user.setRole(newRole.toUpperCase());
        return userRepository.save(user);
    }
}