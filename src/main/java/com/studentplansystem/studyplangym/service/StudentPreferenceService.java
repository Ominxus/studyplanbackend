package com.studentplansystem.studyplangym.service;

import com.studentplansystem.studyplangym.dto.StudentPreferenceRequest;
import com.studentplansystem.studyplangym.dto.StudentPreferenceResponse;
import com.studentplansystem.studyplangym.entity.StudentPreference;
import com.studentplansystem.studyplangym.entity.User;
import com.studentplansystem.studyplangym.repository.StudentPreferenceRepository;
import com.studentplansystem.studyplangym.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
public class StudentPreferenceService {

    private static final Set<String> ALLOWED_PERIODS =
            Set.of(
                    "ANY",
                    "MORNING",
                    "AFTERNOON",
                    "EVENING",
                    "NIGHT"
            );

    private final StudentPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    public StudentPreferenceService(
            StudentPreferenceRepository preferenceRepository,
            UserRepository userRepository
    ) {
        this.preferenceRepository =
                preferenceRepository;
        this.userRepository = userRepository;
    }

    public StudentPreferenceResponse getPreferences(
            String username
    ) {
        User user = getUser(username);

        StudentPreference preference =
                preferenceRepository
                        .findByUserId(user.getId())
                        .orElseGet(() ->
                                createDefaultPreference(
                                        user
                                )
                        );

        return toResponse(preference);
    }

    public StudentPreferenceResponse updatePreferences(
            String username,
            StudentPreferenceRequest request
    ) {
        User user = getUser(username);

        validateRequest(request);

        StudentPreference preference =
                preferenceRepository
                        .findByUserId(user.getId())
                        .orElseGet(() -> {
                            StudentPreference newPreference =
                                    new StudentPreference();

                            newPreference.setUser(user);

                            return newPreference;
                        });

        preference.setPreferredSessionMinutes(
                request.getPreferredSessionMinutes()
        );

        preference.setMaximumSessionMinutes(
                request.getMaximumSessionMinutes()
        );

        preference.setBreakMinutes(
                request.getBreakMinutes()
        );

        preference.setPreferredStudyPeriod(
                normalizePeriod(
                        request.getPreferredStudyPeriod()
                )
        );

        preference.setMaximumDailyMinutes(
                request.getMaximumDailyMinutes()
        );

        return toResponse(
                preferenceRepository.save(preference)
        );
    }

    private StudentPreference createDefaultPreference(
            User user
    ) {
        StudentPreference preference =
                new StudentPreference();

        preference.setUser(user);

        return preferenceRepository.save(
                preference
        );
    }

    private void validateRequest(
            StudentPreferenceRequest request
    ) {
        if (request.getPreferredSessionMinutes() == null ||
                request.getPreferredSessionMinutes() <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Preferred session length must be greater than zero."
            );
        }

        if (request.getMaximumSessionMinutes() == null ||
                request.getMaximumSessionMinutes() <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Maximum session length must be greater than zero."
            );
        }

        if (request.getMaximumSessionMinutes() <
                request.getPreferredSessionMinutes()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Maximum session length cannot be shorter than preferred session length."
            );
        }

        if (request.getBreakMinutes() == null ||
                request.getBreakMinutes() <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Break length must be greater than zero."
            );
        }

        if (request.getMaximumDailyMinutes() == null ||
                request.getMaximumDailyMinutes() <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Maximum daily study time must be greater than zero."
            );
        }

        if (request.getMaximumDailyMinutes() <
                request.getPreferredSessionMinutes()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Maximum daily study time cannot be shorter than one preferred session."
            );
        }

        String period =
                normalizePeriod(
                        request.getPreferredStudyPeriod()
                );

        if (!ALLOWED_PERIODS.contains(period)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid preferred study period."
            );
        }
    }

    private String normalizePeriod(
            String period
    ) {
        if (period == null ||
                period.isBlank()) {
            return "ANY";
        }

        return period
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

    private StudentPreferenceResponse toResponse(
            StudentPreference preference
    ) {
        return new StudentPreferenceResponse(
                preference.getId(),
                preference.getPreferredSessionMinutes(),
                preference.getMaximumSessionMinutes(),
                preference.getBreakMinutes(),
                preference.getPreferredStudyPeriod(),
                preference.getMaximumDailyMinutes()
        );
    }
}
