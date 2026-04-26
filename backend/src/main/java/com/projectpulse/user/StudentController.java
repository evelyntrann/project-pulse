package com.projectpulse.user;

import com.projectpulse.common.ApiResponse;
import com.projectpulse.user.dto.StudentDetailResponse;
import com.projectpulse.user.dto.StudentRegisterRequest;
import com.projectpulse.user.dto.StudentSearchResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody StudentRegisterRequest request) {
        studentService.registerStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<StudentDetailResponse>> getStudent(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.getStudent(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<List<StudentSearchResponse>>> searchStudents(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String sectionName,
            @RequestParam(required = false) String teamName,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long teamId) {

        return ResponseEntity.ok(ApiResponse.ok(
                studentService.searchStudents(firstName, lastName, email,
                        sectionName, teamName, sectionId, teamId)));
    }
}
