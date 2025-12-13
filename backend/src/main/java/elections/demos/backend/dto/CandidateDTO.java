package elections.demos.backend.dto;

public class CandidateDTO {
    private String id;
    private String fullName;
    private String firstName;
    private String lastName;
    private String initials;
    private String gender;

    public CandidateDTO(String id, String fullName, String firstName, String lastName,
                        String initials, String gender) {
        this.id = id;
        this.fullName = fullName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.initials = initials;
        this.gender = gender;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

}