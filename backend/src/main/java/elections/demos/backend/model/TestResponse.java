package elections.demos.backend.model;

public class TestResponse {

    private String message;
    private boolean success;

    public TestResponse() {
    }

    public TestResponse( String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "TestResponse{" +
                ", message='" + message + '\'' +
                ", success=" + success +
                '}';
    }
}
