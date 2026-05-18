package com.studentplansystem.studyplangym.repository;

import com.studentplansystem.studyplangym.entity.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceStatusRepository extends JpaRepository<MaintenanceStatus, Long> {
}