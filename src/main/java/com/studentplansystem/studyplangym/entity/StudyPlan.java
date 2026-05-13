package com.studentplansystem.studyplangym.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "study_plan")
public class StudyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String classYear;
    private String studentNumber;
    private String schoolYear;

    private boolean editRequested = false;
    private boolean editApproved = false;

    @Column(name = "edit_request_status")
    private String editRequestStatus = "NONE";

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "study_plan_id")
    private List<SubjectEntry> subjects;

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getClassYear() {
        return classYear;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public String getSchoolYear() {
        return schoolYear;
    }

    public boolean isEditRequested() {
        return editRequested;
    }

    public boolean isEditApproved() {
        return editApproved;
    }

    public String getEditRequestStatus() {
        return editRequestStatus;
    }

    public List<SubjectEntry> getSubjects() {
        return subjects;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setClassYear(String classYear) {
        this.classYear = classYear;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public void setSchoolYear(String schoolYear) {
        this.schoolYear = schoolYear;
    }

    public void setEditRequested(boolean editRequested) {
        this.editRequested = editRequested;
    }

    public void setEditApproved(boolean editApproved) {
        this.editApproved = editApproved;
    }

    public void setEditRequestStatus(String editRequestStatus) {
        this.editRequestStatus = editRequestStatus;
    }

    public void setSubjects(List<SubjectEntry> subjects) {
        this.subjects = subjects;
    }
}