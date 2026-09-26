-- =========================================================
-- Thesis Study Planning Schema
-- Migration 001
--
-- Adds the new student-centered planning functionality.
-- Existing internship tables are intentionally left unchanged.
-- =========================================================


-- ---------------------------------------------------------
-- 1. Courses studied by an individual student
-- ---------------------------------------------------------

CREATE TABLE IF NOT EXISTS student_course (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,

    name VARCHAR(150) NOT NULL,
    code VARCHAR(50) DEFAULT NULL,

    difficulty TINYINT NOT NULL DEFAULT 3,
    priority TINYINT NOT NULL DEFAULT 3,

    notes TEXT DEFAULT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    KEY idx_student_course_user (user_id),

    CONSTRAINT fk_student_course_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_student_course_difficulty
        CHECK (difficulty BETWEEN 1 AND 5),

    CONSTRAINT chk_student_course_priority
        CHECK (priority BETWEEN 1 AND 5)
);


-- ---------------------------------------------------------
-- 2. Exams, assignments, projects and other deadlines
-- ---------------------------------------------------------

CREATE TABLE IF NOT EXISTS course_deadline (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_course_id BIGINT NOT NULL,

    title VARCHAR(200) NOT NULL,

    deadline_type VARCHAR(30) NOT NULL DEFAULT 'OTHER',

    due_at DATETIME NOT NULL,

    estimated_minutes INT DEFAULT NULL,

    importance TINYINT NOT NULL DEFAULT 3,

    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    notes TEXT DEFAULT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    KEY idx_course_deadline_course (student_course_id),
    KEY idx_course_deadline_due_at (due_at),

    CONSTRAINT fk_course_deadline_course
        FOREIGN KEY (student_course_id)
        REFERENCES student_course(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_course_deadline_importance
        CHECK (importance BETWEEN 1 AND 5),

    CONSTRAINT chk_course_deadline_estimated_minutes
        CHECK (
            estimated_minutes IS NULL
            OR estimated_minutes > 0
        )
);


-- ---------------------------------------------------------
-- 3. Times when the student is available to study
--
-- day_of_week:
-- 1 = Monday
-- 2 = Tuesday
-- 3 = Wednesday
-- 4 = Thursday
-- 5 = Friday
-- 6 = Saturday
-- 7 = Sunday
-- ---------------------------------------------------------

CREATE TABLE IF NOT EXISTS student_availability (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,

    day_of_week TINYINT NOT NULL,

    start_time TIME NOT NULL,
    end_time TIME NOT NULL,

    active TINYINT(1) NOT NULL DEFAULT 1,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    KEY idx_student_availability_user (user_id),

    CONSTRAINT fk_student_availability_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_student_availability_day
        CHECK (day_of_week BETWEEN 1 AND 7),

    CONSTRAINT chk_student_availability_time
        CHECK (start_time < end_time)
);


-- ---------------------------------------------------------
-- 4. Personal study preferences
--
-- One preference record per user.
-- ---------------------------------------------------------

CREATE TABLE IF NOT EXISTS student_preference (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,

    preferred_session_minutes INT NOT NULL DEFAULT 60,
    maximum_session_minutes INT NOT NULL DEFAULT 90,
    break_minutes INT NOT NULL DEFAULT 15,

    preferred_study_period VARCHAR(20) NOT NULL DEFAULT 'ANY',

    maximum_daily_minutes INT NOT NULL DEFAULT 180,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    UNIQUE KEY uk_student_preference_user (user_id),

    CONSTRAINT fk_student_preference_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_preferred_session_minutes
        CHECK (preferred_session_minutes > 0),

    CONSTRAINT chk_maximum_session_minutes
        CHECK (maximum_session_minutes > 0),

    CONSTRAINT chk_break_minutes
        CHECK (break_minutes >= 0),

    CONSTRAINT chk_maximum_daily_minutes
        CHECK (maximum_daily_minutes > 0)
);


-- ---------------------------------------------------------
-- 5. Personal study goals
-- ---------------------------------------------------------

CREATE TABLE IF NOT EXISTS student_goal (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,

    title VARCHAR(200) NOT NULL,
    description TEXT DEFAULT NULL,

    target_date DATE DEFAULT NULL,

    priority TINYINT NOT NULL DEFAULT 3,

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    KEY idx_student_goal_user (user_id),

    CONSTRAINT fk_student_goal_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_student_goal_priority
        CHECK (priority BETWEEN 1 AND 5)
);


-- ---------------------------------------------------------
-- 6. Generated personal study plans
-- ---------------------------------------------------------

CREATE TABLE IF NOT EXISTS personal_study_plan (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,

    plan_name VARCHAR(150) DEFAULT NULL,

    start_date DATE NOT NULL,
    end_date DATE NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    generation_method VARCHAR(30) NOT NULL DEFAULT 'MANUAL',

    summary TEXT DEFAULT NULL,

    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    KEY idx_personal_study_plan_user (user_id),

    CONSTRAINT fk_personal_study_plan_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_personal_study_plan_dates
        CHECK (end_date >= start_date)
);


-- ---------------------------------------------------------
-- 7. Individual sessions inside a generated study plan
-- ---------------------------------------------------------

CREATE TABLE IF NOT EXISTS study_session (
    id BIGINT NOT NULL AUTO_INCREMENT,

    plan_id BIGINT NOT NULL,

    student_course_id BIGINT DEFAULT NULL,
    deadline_id BIGINT DEFAULT NULL,
    goal_id BIGINT DEFAULT NULL,

    title VARCHAR(200) NOT NULL,

    start_at DATETIME NOT NULL,
    end_at DATETIME NOT NULL,

    planned_minutes INT NOT NULL,

    actual_minutes INT DEFAULT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',

    rationale TEXT DEFAULT NULL,
    notes TEXT DEFAULT NULL,

    completed_at DATETIME DEFAULT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    KEY idx_study_session_plan (plan_id),
    KEY idx_study_session_course (student_course_id),
    KEY idx_study_session_deadline (deadline_id),
    KEY idx_study_session_goal (goal_id),
    KEY idx_study_session_start_at (start_at),

    CONSTRAINT fk_study_session_plan
        FOREIGN KEY (plan_id)
        REFERENCES personal_study_plan(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_study_session_course
        FOREIGN KEY (student_course_id)
        REFERENCES student_course(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_study_session_deadline
        FOREIGN KEY (deadline_id)
        REFERENCES course_deadline(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_study_session_goal
        FOREIGN KEY (goal_id)
        REFERENCES student_goal(id)
        ON DELETE SET NULL,

    CONSTRAINT chk_study_session_times
        CHECK (end_at > start_at),

    CONSTRAINT chk_study_session_planned_minutes
        CHECK (planned_minutes > 0),

    CONSTRAINT chk_study_session_actual_minutes
        CHECK (
            actual_minutes IS NULL
            OR actual_minutes >= 0
        )
);
