package com.studentplansystem.studyplangym.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "subject_entry")
public class SubjectEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;

    @Column(name = "grade_iii_hours")
    private int gradeIiiHours;

    @Column(name = "grade_iv_hours")
    private int gradeIvHours;

    private String selectedGrade;

    public String getSelectedGrade() {
        return selectedGrade;
    }

    public void setSelectedGrade(String selectedGrade) {
        this.selectedGrade = selectedGrade;
    }

    public Long getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public int getGradeIiiHours() {
        return gradeIiiHours;
    }

    public int getGradeIvHours() {
        return gradeIvHours;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setGradeIiiHours(int gradeIiiHours) {
        this.gradeIiiHours = gradeIiiHours;
    }

    public void setGradeIvHours(int gradeIvHours) {
        this.gradeIvHours = gradeIvHours;
    }
}