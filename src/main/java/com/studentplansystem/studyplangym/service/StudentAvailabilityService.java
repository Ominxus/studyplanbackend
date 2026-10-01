package com.studentplansystem.studyplangym.service;

import com.studentplansystem.studyplangym.dto.StudentAvailabilityRequest;
import com.studentplansystem.studyplangym.dto.StudentAvailabilityResponse;
import com.studentplansystem.studyplangym.entity.StudentAvailability;
import com.studentplansystem.studyplangym.entity.User;
import com.studentplansystem.studyplangym.repository.StudentAvailabilityRepository;
import com.studentplansystem.studyplangym.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
public class StudentAvailabilityService {

    private final StudentAvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;

    public StudentAvailabilityService(
            StudentAvailabilityRepository availabilityRepository,
            UserRepository userRepository
    ) {
        this.availabilityRepository = availabilityRepository;
        this.userRepository = userRepository;
    }

    public StudentAvailabilityResponse createAvailability(
            String username,
            StudentAvailabilityRequest request
    ) {
        User user = getUser(username);

        validateRequest(request);

        checkForOverlap(
                user.getId(),
                null,
                request
        );

        StudentAvailability availability =
                new StudentAvailability();

        availability.setUser(user);
        availability.setDayOfWeek(
                request.getDayOfWeek()
        );
        availability.setStartTime(
                request.getStartTime()
        );
        availability.setEndTime(
                request.getEndTime()
        );
        availability.setActive(true);

        return toResponse(
                availabilityRepository.save(
                        availability
                )
        );
    }

    public List<StudentAvailabilityResponse> getAvailability(
            String username
    ) {
        User user = getUser(username);

        return availabilityRepository
                .findByUserIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(
                        user.getId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public StudentAvailabilityResponse updateAvailability(
            String username,
            Long availabilityId,
            StudentAvailabilityRequest request
    ) {
        User user = getUser(username);

        validateRequest(request);

        StudentAvailability availability =
                availabilityRepository
                        .findByIdAndUserIdAndActiveTrue(
                                availabilityId,
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Availability slot not found."
                                )
                        );

        checkForOverlap(
                user.getId(),
                availabilityId,
                request
        );

        availability.setDayOfWeek(
                request.getDayOfWeek()
        );
        availability.setStartTime(
                request.getStartTime()
        );
        availability.setEndTime(
                request.getEndTime()
        );

        return toResponse(
                availabilityRepository.save(
                        availability
                )
        );
    }

    public void deactivateAvailability(
            String username,
            Long availabilityId
    ) {
        User user = getUser(username);

        StudentAvailability availability =
                availabilityRepository
                        .findByIdAndUserIdAndActiveTrue(
                                availabilityId,
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Availability slot not found."
                                )
                        );

        availability.setActive(false);

        availabilityRepository.save(
                availability
        );
    }

    private void validateRequest(
            StudentAvailabilityRequest request
    ) {
        if (request.getDayOfWeek() == null ||
                request.getDayOfWeek() < 1 ||
                request.getDayOfWeek() > 7) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Day of week must be between 1 and 7."
            );
        }

        if (request.getStartTime() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start time is required."
            );
        }

        if (request.getEndTime() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "End time is required."
            );
        }

        if (!request.getStartTime()
                .isBefore(request.getEndTime())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start time must be before end time."
            );
        }
    }

    private void checkForOverlap(
            Long userId,
            Long ignoredAvailabilityId,
            StudentAvailabilityRequest request
    ) {
        List<StudentAvailability> existingSlots =
                availabilityRepository
                        .findByUserIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(
                                userId
                        );

        boolean overlaps = existingSlots.stream()
                .filter(slot ->
                        slot.getDayOfWeek()
                                == request.getDayOfWeek()
                )
                .filter(slot ->
                        ignoredAvailabilityId == null ||
                                !slot.getId().equals(
                                        ignoredAvailabilityId
                                )
                )
                .anyMatch(slot ->
                        request.getStartTime()
                                .isBefore(slot.getEndTime())
                                &&
                        request.getEndTime()
                                .isAfter(slot.getStartTime())
                );

        if (overlaps) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This availability period overlaps with an existing slot."
            );
        }
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

    private StudentAvailabilityResponse toResponse(
            StudentAvailability availability
    ) {
        String dayName = DayOfWeek
                .of(availability.getDayOfWeek())
                .getDisplayName(
                        TextStyle.FULL,
                        Locale.ENGLISH
                );

        return new StudentAvailabilityResponse(
                availability.getId(),
                availability.getDayOfWeek(),
                dayName,
                availability.getStartTime(),
                availability.getEndTime(),
                availability.isActive()
        );
    }
}
