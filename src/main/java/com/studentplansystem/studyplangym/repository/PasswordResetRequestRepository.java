package com.studentplansystem.studyplangym.repository;

import com.studentplansystem.studyplangym.entity.PasswordResetRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequest, Long> {

    List<PasswordResetRequest> findByStatusOrderByRequestedAtDesc(String status);

    boolean existsByUsernameAndStatus(String username, String status);
}