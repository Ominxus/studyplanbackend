package com.studentplansystem.studyplangym.repository;

import com.studentplansystem.studyplangym.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}