package elections.demos.backend.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Admin bootstrap disabled per user request. No automatic role changes will be made on startup.
    }
}
