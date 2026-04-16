package com.projectpulse.user;

import com.projectpulse.user.dto.StudentDetailResponse;
import com.projectpulse.user.dto.StudentSearchResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class StudentService {

    private final UserRepository userRepository;

    public StudentService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<StudentSearchResponse> searchStudents(
            String firstName, String lastName, String email,
            String sectionName, String teamName,
            Long sectionId, Long teamId) {

        // Convert blank strings to null so the IS NULL check in the query works correctly
        return userRepository.searchStudents(
                        blank(firstName), blank(lastName), blank(email),
                        blank(sectionName), blank(teamName),
                        sectionId, teamId)
                .stream()
                .map(p -> new StudentSearchResponse(
                        p.getId(), p.getFirstName(), p.getLastName(),
                        p.getEmail(), p.getSectionName(), p.getTeamName()))
                .toList();
    }

    public StudentDetailResponse getStudent(Long id) {
        StudentSearchProjection p = userRepository.findStudentById(id)
                .orElseThrow(() -> new NoSuchElementException("Student not found"));
        return new StudentDetailResponse(
                p.getId(), p.getFirstName(), p.getLastName(),
                p.getEmail(), p.getSectionName(), p.getTeamName());
    }

    private String blank(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
