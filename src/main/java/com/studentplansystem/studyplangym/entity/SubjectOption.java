package com.studentplansystem.studyplangym.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "subject_option")
public class SubjectOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String groupName;

    @Column(name = "grade_iii_hours")
    private int gradeIiiHours;

    @Column(name = "grade_iv_hours")
    private int gradeIvHours;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGroupName() {
        return groupName;
    }

    public int getGradeIiiHours() {
        return gradeIiiHours;
    }

    public int getGradeIvHours() {
        return gradeIvHours;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setGradeIiiHours(int gradeIiiHours) {
        this.gradeIiiHours = gradeIiiHours;
    }

    public void setGradeIvHours(int gradeIvHours) {
        this.gradeIvHours = gradeIvHours;
    }
}