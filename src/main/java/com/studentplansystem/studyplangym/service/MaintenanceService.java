package com.studentplansystem.studyplangym.service;

import com.studentplansystem.studyplangym.entity.MaintenanceStatus;
import com.studentplansystem.studyplangym.repository.MaintenanceStatusRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MaintenanceService {

    private final MaintenanceStatusRepository repository;

    public MaintenanceService(MaintenanceStatusRepository repository) {
        this.repository = repository;
    }

    public MaintenanceStatus getStatus() {
        return repository.findAll()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    MaintenanceStatus status = new MaintenanceStatus();
                    status.setEnabled(false);
                    status.setTitle("Website is under maintenance");
                    status.setMessage("The study plan system is currently being updated. Please come back later.");
                    status.setUpdatedAt(LocalDateTime.now());
                    return repository.save(status);
                });
    }

    public boolean isMaintenanceEnabled() {
        return getStatus().isEnabled();
    }

    public MaintenanceStatus updateStatus(MaintenanceStatus updatedStatus) {
        MaintenanceStatus status = getStatus();

        status.setEnabled(updatedStatus.isEnabled());

        if (updatedStatus.getTitle() != null && !updatedStatus.getTitle().trim().isEmpty()) {
            status.setTitle(updatedStatus.getTitle().trim());
        }

        if (updatedStatus.getMessage() != null && !updatedStatus.getMessage().trim().isEmpty()) {
            status.setMessage(updatedStatus.getMessage().trim());
        }

        status.setUpdatedAt(LocalDateTime.now());

        return repository.save(status);
    }
}