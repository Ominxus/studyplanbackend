package com.studentplansystem.studyplangym.service;

import com.studentplansystem.studyplangym.dto.CourseDeadlineRequest;
import com.studentplansystem.studyplangym.dto.CourseDeadlineResponse;
import com.studentplansystem.studyplangym.entity.CourseDeadline;
import com.studentplansystem.studyplangym.entity.StudentCourse;
import com.studentplansystem.studyplangym.entity.User;
import com.studentplansystem.studyplangym.repository.CourseDeadlineRepository;
import com.studentplansystem.studyplangym.repository.StudentCourseRepository;
import com.studentplansystem.studyplangym.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
public class CourseDeadlineService {

    private static final Set<String> ALLOWED_TYPES =
            Set.of("EXAM", "ASSIGNMENT", "PROJECT", "OTHER");

    private static final Set<String> ALLOWED_STATUSES =
            Set.of("PENDING", "COMPLETED", "CANCELLED");

    private final CourseDeadlineRepository courseDeadlineRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final UserRepository userRepository;

    public CourseDeadlineService(
            CourseDeadlineRepository courseDeadlineRepository,
            StudentCourseRepository studentCourseRepository,
            UserRepository userRepository
    ) {
        this.courseDeadlineRepository = courseDeadlineRepository;
        this.studentCourseRepository = studentCourseRepository;
        this.userRepository = userRepository;
    }

    public CourseDeadlineResponse createDeadline(
            String username,
            CourseDeadlineRequest request
    ) {
        User user = getUser(username);

        validateRequest(request);

        StudentCourse course = studentCourseRepository
                .findByIdAndUserId(request.getCourseId(), user.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Course not found."
                        )
                );

        if (!course.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot add a deadline to an inactive course."
            );
        }

        CourseDeadline deadline = new CourseDeadline();

        deadline.setStudentCourse(course);
        deadline.setTitle(request.getTitle().trim());

        deadline.setDeadlineType(
                normalizeType(request.getDeadlineType())
        );

        deadline.setDueAt(request.getDueAt());
        deadline.setEstimatedMinutes(request.getEstimatedMinutes());

        deadline.setImportance(
                request.getImportance() == null
                        ? 3
                        : request.getImportance()
        );

        deadline.setStatus(
                normalizeStatus(request.getStatus())
        );

        deadline.setNotes(request.getNotes());

        return toResponse(
                courseDeadlineRepository.save(deadline)
        );
    }

    public List<CourseDeadlineResponse> getDeadlines(
            String username,
            Long courseId
    ) {
        User user = getUser(username);

        List<CourseDeadline> deadlines;

        if (courseId == null) {
            deadlines =
                    courseDeadlineRepository
                            .findByStudentCourseUserIdOrderByDueAtAsc(
                                    user.getId()
                            );
        } else {
            studentCourseRepository
                    .findByIdAndUserId(courseId, user.getId())
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Course not found."
                            )
                    );

            deadlines =
                    courseDeadlineRepository
                            .findByStudentCourseIdAndStudentCourseUserIdOrderByDueAtAsc(
                                    courseId,
                                    user.getId()
                            );
        }

        return deadlines
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private User getUser(String username) {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authenticated user could not be found."
                        )
                );
    }

    private void validateRequest(
            CourseDeadlineRequest request
    ) {
        if (request.getCourseId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Course is required."
            );
        }

        if (request.getTitle() == null ||
                request.getTitle().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Deadline title is required."
            );
        }

        if (request.getTitle().trim().length() > 200) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Deadline title cannot exceed 200 characters."
            );
        }

        if (request.getDueAt() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Deadline date and time are required."
            );
        }

        if (request.getEstimatedMinutes() != null &&
                request.getEstimatedMinutes() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estimated minutes must be greater than zero."
            );
        }

        if (request.getImportance() != null &&
                (request.getImportance() < 1 ||
                 request.getImportance() > 5)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Importance must be between 1 and 5."
            );
        }

        String type = normalizeType(
                request.getDeadlineType()
        );

        if (!ALLOWED_TYPES.contains(type)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid deadline type."
            );
        }

        String status = normalizeStatus(
                request.getStatus()
        );

        if (!ALLOWED_STATUSES.contains(status)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid deadline status."
            );
        }
    }

    private String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            return "OTHER";
        }

        return type.trim().toUpperCase();
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "PENDING";
        }

        return status.trim().toUpperCase();
    }

    private CourseDeadlineResponse toResponse(
            CourseDeadline deadline
    ) {
        return new CourseDeadlineResponse(
                deadline.getId(),
                deadline.getStudentCourse().getId(),
                deadline.getStudentCourse().getName(),
                deadline.getTitle(),
                deadline.getDeadlineType(),
                deadline.getDueAt(),
                deadline.getEstimatedMinutes(),
                deadline.getImportance(),
                deadline.getStatus(),
                deadline.getNotes()
        );
    }
}
