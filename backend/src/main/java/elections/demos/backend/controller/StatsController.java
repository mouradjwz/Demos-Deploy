package elections.demos.backend.controller;

import elections.demos.backend.dto.StatDto;
import elections.demos.backend.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    public ResponseEntity<List<StatDto>> getStats() {
        return ResponseEntity.ok(statsService.getStats());
    }
}