package elections.demos.backend.dto;

public class LoginResponse {
    private String email;
    private String token;
    private String username;
    private String role;

    public LoginResponse() {
    }

    public LoginResponse(String email, String token, String username) {
        this.email = email;
        this.token = token;
        this.username = username;
    }

    public LoginResponse(String email, String token, String username, String role) {
        this.email = email;
        this.token = token;
        this.username = username;
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
