package elections.demos.backend.dto;

/**
 * A Data Transfer Object for Election details.
 */
public class ExistingElectionDTO {
    private String electionId;
    private String electionName;

    public ExistingElectionDTO(String electionId, String electionName) {
        this.electionId = electionId;
        this.electionName = electionName;
    }

    public String getElectionId() {
        return electionId;
    }

    public void setElectionId(String electionId) {
        this.electionId = electionId;
    }

    public String getElectionName() {
        return electionName;
    }

    public void setElectionName(String electionName) {
        this.electionName = electionName;
    }
}