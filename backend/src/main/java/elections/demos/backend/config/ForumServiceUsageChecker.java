package elections.demos.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import elections.demos.backend.service.ForumService;

@Component
public class ForumServiceUsageChecker implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(ForumServiceUsageChecker.class);
    private final ForumService forumService;

    public ForumServiceUsageChecker(ForumService forumService) {
        this.forumService = forumService;
    }

    @Override
    public void run(String... args) {
        try {
            int count = forumService.getAllPosts().size();
            logger.info("ForumService in use on startup — current posts: {}", count);
        } catch (Exception e) {
            // don't fail application startup if DB isn't available; just log
            logger.debug("ForumService startup check failed (DB might be down): {}", e.getMessage());
        }
    }
}
