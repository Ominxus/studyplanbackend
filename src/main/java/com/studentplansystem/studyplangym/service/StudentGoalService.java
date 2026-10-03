package com.studentplansystem.studyplangym.service;

import com.studentplansystem.studyplangym.dto.StudentGoalRequest;
import com.studentplansystem.studyplangym.dto.StudentGoalResponse;
import com.studentplansystem.studyplangym.entity.StudentGoal;
import com.studentplansystem.studyplangym.entity.User;
import com.studentplansystem.studyplangym.repository.StudentGoalRepository;
import com.studentplansystem.studyplangym.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
public class StudentGoalService {

    private static final Set<String> ALLOWED_STATUSES =
            Set.of(
                    "ACTIVE",
                    "COMPLETED",
                    "CANCELLED"
            );

    private final StudentGoalRepository goalRepository;
    private final UserRepository userRepository;

    public StudentGoalService(
            StudentGoalRepository goalRepository,
            UserRepository userRepository
    ) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
    }

    public StudentGoalResponse createGoal(
            String username,
            StudentGoalRequest request
    ) {
        User user = getUser(username);

        validateRequest(request);

        StudentGoal goal = new StudentGoal();

        goal.setUser(user);
        goal.setTitle(request.getTitle().trim());
        goal.setDescription(request.getDescription());
        goal.setTargetDate(request.getTargetDate());

        goal.setPriority(
                request.getPriority() == null
                        ? 3
                        : request.getPriority()
        );

        goal.setStatus(
                normalizeStatus(request.getStatus())
        );

        return toResponse(
                goalRepository.save(goal)
        );
    }

    public List<StudentGoalResponse> getGoals(
            String username
    ) {
        User user = getUser(username);

        return goalRepository
                .findByUserIdAndStatusNotOrderByTargetDateAsc(
                        user.getId(),
                        "CANCELLED"
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public StudentGoalResponse updateGoal(
            String username,
            Long goalId,
            StudentGoalRequest request
    ) {
        User user = getUser(username);

        validateRequest(request);

        StudentGoal goal =
                goalRepository
                        .findByIdAndUserId(
                                goalId,
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Goal not found."
                                )
                        );

        goal.setTitle(
                request.getTitle().trim()
        );

        goal.setDescription(
                request.getDescription()
        );

        goal.setTargetDate(
                request.getTargetDate()
        );

        goal.setPriority(
                request.getPriority() == null
                        ? 3
                        : request.getPriority()
        );

        if (request.getStatus() != null &&
                !request.getStatus().isBlank()) {

            goal.setStatus(
                    normalizeStatus(
                            request.getStatus()
                    )
            );
        }

        return toResponse(
                goalRepository.save(goal)
        );
    }

    public StudentGoalResponse completeGoal(
            String username,
            Long goalId
    ) {
        User user = getUser(username);

        StudentGoal goal =
                goalRepository
                        .findByIdAndUserId(
                                goalId,
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Goal not found."
                                )
                        );

        if ("CANCELLED".equals(goal.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A cancelled goal cannot be completed."
            );
        }

        goal.setStatus("COMPLETED");

        return toResponse(
                goalRepository.save(goal)
        );
    }

    public void cancelGoal(
            String username,
            Long goalId
    ) {
        User user = getUser(username);

        StudentGoal goal =
                goalRepository
                        .findByIdAndUserId(
                                goalId,
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Goal not found."
                                )
                        );

        goal.setStatus("CANCELLED");

        goalRepository.save(goal);
    }

    private void validateRequest(
            StudentGoalRequest request
    ) {
        if (request.getTitle() == null ||
                request.getTitle().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Goal title is required."
            );
        }

        if (request.getTitle().trim().length() > 200) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Goal title cannot exceed 200 characters."
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

        String status =
                normalizeStatus(
                        request.getStatus()
                );

        if (!ALLOWED_STATUSES.contains(status)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid goal status."
            );
        }
    }

    private String normalizeStatus(
            String status
    ) {
        if (status == null ||
                status.isBlank()) {
            return "ACTIVE";
        }

        return status
                .trim()
                .toUpperCase();
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

    private StudentGoalResponse toResponse(
            StudentGoal goal
    ) {
        return new StudentGoalResponse(
                goal.getId(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getTargetDate(),
                goal.getPriority(),
                goal.getStatus(),
                goal.getCreatedAt(),
                goal.getUpdatedAt()
        );
    }
}
