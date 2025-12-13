package elections.demos.backend.service;

import elections.demos.backend.model.User;
import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    User getUserById(Long id);
    User createUser(User user);
    User updateUser(Long id, User user);
    User updateUsernameByEmail(String email, String newUsername);

    void deleteUser(Long id);
    User registerUser(String username, String email, String password);
    User loginUser(String email, String password);
    User updateUserRole(Long id, String newRole, String requesterEmail);
}