package com.projectpulse.section;

import com.projectpulse.section.dto.SectionSummaryResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SectionService {

    private final SectionRepository sectionRepository;

    public SectionService(SectionRepository sectionRepository) {
        this.sectionRepository = sectionRepository;
    }

    public List<SectionSummaryResponse> findSections(String name) {
        return sectionRepository.findByNameContaining(name).stream()
                .map(section -> new SectionSummaryResponse(
                        section.getId(),
                        section.getName(),
                        section.getStartDate(),
                        section.getEndDate(),
                        section.getTeams().stream()
                                .map(t -> t.getName())
                                .sorted()
                                .toList()
                ))
                .toList();
    }
}
