package elections.demos.backend;

import elections.demos.backend.service.DutchElectionService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Main class for the Elections Spring Boot application.
 * This class is the starting point for the application and handles the
 * application startup event.
 */
@SpringBootApplication
public class BackendApplication {

    /**
     * Main method to run the Spring Boot application.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    CommandLineRunner loadElectionData(@Qualifier("dutchElectionServiceImpl") DutchElectionService electionService) {
        return args -> {
            try {
                System.out.println("Loading TK2023 election data...");
//                electionService.readResults("TK2021", "EML_bestanden_lobotomized_TK2021");
//                electionService.readResults("TK2023", "EML_bestanden_lobotomized_TK2023");
//                electionService.readResults("TK2021", "EML_bestanden_TK2021");
//                electionService.readResults("TK2023", "EML_bestanden_TK2023");
//                electionService.readResults("TK2023", "TK2023_HvA_UvA");
            } catch (Exception e) {
                System.err.println("Error loading initial election data: " + e.getMessage());
                throw e;
            }
        };
    }
}