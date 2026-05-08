package com.studentplansystem.studyplangym.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "school_year_option")
public class SchoolYearOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String label;

    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}