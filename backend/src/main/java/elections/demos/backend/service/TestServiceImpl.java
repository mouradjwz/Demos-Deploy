package elections.demos.backend.service;

import elections.demos.backend.model.TestResponse;

import org.springframework.stereotype.Service;

@Service
public class TestServiceImpl implements TestService {

    @Override
    public TestResponse getTestResult() {
        try {
            return new TestResponse("Test successful!", true);
        } catch (Exception e) {
            return new TestResponse("Error occurred: " + e.getMessage(), false);
        }
    }

}
