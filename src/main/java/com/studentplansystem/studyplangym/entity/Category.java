package com.studentplansystem.studyplangym.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int minRequired;
    private int maxAllowed;
    private boolean optional;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private List<SubjectOption> subjects;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMinRequired() {
        return minRequired;
    }

    public int getMaxAllowed() {
        return maxAllowed;
    }

    public boolean isOptional() {
        return optional;
    }

    public List<SubjectOption> getSubjects() {
        return subjects;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMinRequired(int minRequired) {
        this.minRequired = minRequired;
    }

    public void setMaxAllowed(int maxAllowed) {
        this.maxAllowed = maxAllowed;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public void setSubjects(List<SubjectOption> subjects) {
        this.subjects = subjects;
    }
}