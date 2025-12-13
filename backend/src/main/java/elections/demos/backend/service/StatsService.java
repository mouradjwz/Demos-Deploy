package elections.demos.backend.service;

import elections.demos.backend.dto.StatDto;
import elections.demos.backend.model.Stat;
import elections.demos.backend.repository.StatRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatsService {
    private final StatRepository statRepository;

    public StatsService(StatRepository statRepository) {
        this.statRepository = statRepository;
    }

    public List<StatDto> getStats() {
        return statRepository.findAll(Sort.by("id").ascending())
                .stream()
                .map(this::toDto)
                .toList();
    }

    private StatDto toDto(Stat stat) {
        return new StatDto(stat.getTitle(), stat.getValue(), stat.getSubtitle(), stat.isShowPlus());
    }
}
