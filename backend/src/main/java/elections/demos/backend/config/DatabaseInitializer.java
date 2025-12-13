package elections.demos.backend.config;

import elections.demos.backend.model.User;
import elections.demos.backend.repository.UserRepository;
import elections.demos.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import elections.demos.backend.model.Stat;
import elections.demos.backend.repository.StatRepository;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

@Configuration
@Order(2)
public class DatabaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, UserService userService, DataSource dataSource, StatRepository statRepository) {
        return args -> {
            logger.info("========================================");
            logger.info("DATABASE CONNECTION TEST");
            logger.info("========================================");

            // Test database connection
            try (Connection connection = dataSource.getConnection()) {
                logger.info("✅ Successfully connected to database!");
                logger.info("Database URL: {}", connection.getMetaData().getURL());
                logger.info("Database Product: {}", connection.getMetaData().getDatabaseProductName());
                logger.info("Database Version: {}", connection.getMetaData().getDatabaseProductVersion());
            } catch (Exception e) {
                logger.error("❌ Failed to connect to database: {}", e.getMessage());
                return;
            }

            logger.info("========================================");
            logger.info("CHECKING USER TABLE");
            logger.info("========================================");

            // Check if user table exists and has data
            long userCount = userRepository.count();
            logger.info("Current number of users in database: {}", userCount);

            // Add dummy data if table is empty
            if (userCount == 0) {
                logger.info("User table is empty. Adding dummy data...");

                User user1 = userService.registerUser("john_doe", "john@example.com", "password123");
                User user2 = userService.registerUser("jane_smith", "jane@example.com", "password456");
                User user3 = userService.registerUser("bob_wilson", "bob@example.com", "password789");
                User user4 = userService.registerUser("Mourad", "mourad@example.com", "password789");

                logger.info("✅ Successfully added 4 dummy users with hashed passwords!");
                logger.info("   - User 1: {} ({})", user1.getUsername(), user1.getEmail());
                logger.info("   - User 2: {} ({})", user2.getUsername(), user2.getEmail());
                logger.info("   - User 3: {} ({})", user3.getUsername(), user3.getEmail());
                logger.info("   - User 4: {} ({})", user4.getUsername(), user4.getEmail());
            } else {
                logger.info("User table already contains {} users. Skipping dummy data insertion.", userCount);
                
                // List existing users
                logger.info("Existing users:");
                userRepository.findAll().forEach(user -> 
                    logger.info("   - ID: {}, Username: {}, Email: {}", 
                        user.getId(), user.getUsername(), user.getEmail())
                );
            }

            // ========================================
            // 👇 STATS INITIALIZATION (NEW SECTION)
            // ========================================
            logger.info("========================================");
            logger.info("CHECKING STAT TABLE");
            logger.info("========================================");

            long statCount = statRepository.count();
            logger.info("Current number of stats in database: {}", statCount);

            if (statCount == 0) {
                logger.info("Stat table is empty. Adding default stats...");

                List<Stat> defaultStats = List.of(
                        new Stat("Verkiezingen", 15, "Tweede Kamer verkiezingen sinds 1946", true),
                        new Stat("Partijen",     50, "Politieke partijen geanalyseerd",      true),
                        new Stat("Gemeenten",   344, "Nederlandse gemeenten in kaart",       false),
                        new Stat("Datasets",     25, "Verschillende databronnen",            true)
                );

                statRepository.saveAll(defaultStats);
                logger.info(" Successfully added {} default stats!", defaultStats.size());
            } else {
                logger.info("Stat table already contains {} entries. Skipping seeding.", statCount);
            }

            logger.info("========================================");
            logger.info("DATABASE INITIALIZATION COMPLETE");
            logger.info("========================================");
        };
    }
}