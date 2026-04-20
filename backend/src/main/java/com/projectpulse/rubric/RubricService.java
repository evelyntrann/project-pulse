package com.projectpulse.rubric;

import com.projectpulse.rubric.dto.CriterionResponse;
import com.projectpulse.rubric.dto.RubricCreateRequest;
import com.projectpulse.rubric.dto.RubricResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class RubricService {

    private final RubricRepository rubricRepository;

    public RubricService(RubricRepository rubricRepository) {
        this.rubricRepository = rubricRepository;
    }

    @Transactional
    public RubricResponse createRubric(RubricCreateRequest request, Long adminId) {
        if (rubricRepository.existsByName(request.name())) {
            throw new DuplicateRubricException("Rubric '" + request.name() + "' already exists");
        }

        RubricEntity rubric = new RubricEntity();
        rubric.setName(request.name());
        rubric.setCreatedBy(adminId);

        List<CriterionEntity> criteria = request.criteria().stream()
                .map(c -> {
                    CriterionEntity criterion = new CriterionEntity();
                    criterion.setRubric(rubric);
                    criterion.setName(c.name());
                    criterion.setDescription(c.description());
                    criterion.setMaxScore(c.maxScore());
                    criterion.setOrderIndex(c.orderIndex());
                    return criterion;
                })
                .toList();

        rubric.setCriteria(criteria);
        RubricEntity saved = rubricRepository.save(rubric);
        return toResponse(saved);
    }

    public List<RubricResponse> listRubrics() {
        return rubricRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public RubricResponse getRubric(Long id) {
        RubricEntity rubric = rubricRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Rubric not found"));
        return toResponse(rubric);
    }

    private RubricResponse toResponse(RubricEntity rubric) {
        List<CriterionResponse> criteriaResponses = rubric.getCriteria().stream()
                .map(c -> new CriterionResponse(
                        c.getId(), c.getName(), c.getDescription(), c.getMaxScore(), c.getOrderIndex()))
                .toList();
        return new RubricResponse(rubric.getId(), rubric.getName(), criteriaResponses);
    }
}
