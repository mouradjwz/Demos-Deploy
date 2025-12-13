package elections.demos.backend.controller;

import elections.demos.backend.dto.TestResponseDto;
import elections.demos.backend.model.TestResponse;
import elections.demos.backend.service.TestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping
    public ResponseEntity<TestResponseDto> getTest() {
        TestResponse result = testService.getTestResult();
        String message = result.getMessage();
        boolean isSuccess = result.isSuccess();

        TestResponseDto response = new TestResponseDto(message, isSuccess);
        return ResponseEntity.status(HttpStatus.OK).body(response);


    }

}
