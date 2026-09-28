package com.studentplansystem.studyplangym.dto;

public class StudentCourseRequest {

    private String name;
    private String code;
    private Integer difficulty;
    private Integer priority;
    private String notes;

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public Integer getPriority() {
        return priority;
    }

    public String getNotes() {
        return notes;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

