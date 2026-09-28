package com.studentplansystem.studyplangym.service;

import com.studentplansystem.studyplangym.dto.StudentCourseRequest;
import com.studentplansystem.studyplangym.dto.StudentCourseResponse;
import com.studentplansystem.studyplangym.entity.StudentCourse;
import com.studentplansystem.studyplangym.entity.User;
import com.studentplansystem.studyplangym.repository.StudentCourseRepository;
import com.studentplansystem.studyplangym.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class StudentCourseService {

    private final StudentCourseRepository studentCourseRepository;
    private final UserRepository userRepository;

    public StudentCourseService(
            StudentCourseRepository studentCourseRepository,
            UserRepository userRepository
    ) {
        this.studentCourseRepository = studentCourseRepository;
        this.userRepository = userRepository;
    }

    public StudentCourseResponse createCourse(
            String username,
            StudentCourseRequest request
    ) {
        User user = getUser(username);

        validateRequest(request);

        StudentCourse course = new StudentCourse();
        course.setUser(user);
        course.setName(request.getName().trim());

        if (request.getCode() != null && !request.getCode().isBlank()) {
            course.setCode(request.getCode().trim());
        }

        course.setDifficulty(
                request.getDifficulty() == null ? 3 : request.getDifficulty()
        );

        course.setPriority(
                request.getPriority() == null ? 3 : request.getPriority()
        );

        course.setNotes(request.getNotes());
        course.setActive(true);

        return toResponse(studentCourseRepository.save(course));
    }

    public List<StudentCourseResponse> getCourses(String username) {
        User user = getUser(username);

        return studentCourseRepository
                .findByUserIdAndActiveTrue(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authenticated user could not be found."
                        )
                );
    }

    private void validateRequest(StudentCourseRequest request) {

        if (request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Course name is required."
            );
        }

        if (request.getName().trim().length() > 150) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Course name cannot exceed 150 characters."
            );
        }

        if (request.getCode() != null &&
                request.getCode().trim().length() > 50) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Course code cannot exceed 50 characters."
            );
        }

        if (request.getDifficulty() != null &&
                (request.getDifficulty() < 1 ||
                 request.getDifficulty() > 5)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Difficulty must be between 1 and 5."
            );
        }

        if (request.getPriority() != null &&
                (request.getPriority() < 1 ||
                 request.getPriority() > 5)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Priority must be between 1 and 5."
            );
        }
    }

    private StudentCourseResponse toResponse(StudentCourse course) {
        return new StudentCourseResponse(
                course.getId(),
                course.getName(),
                course.getCode(),
                course.getDifficulty(),
                course.getPriority(),
                course.getNotes(),
                course.isActive(),
                course.getCreatedAt()
        );
    }
}
