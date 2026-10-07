ALTER TABLE personal_study_plan
ADD COLUMN source_plan_id BIGINT NULL AFTER user_id;

CREATE INDEX idx_personal_study_plan_source
ON personal_study_plan(source_plan_id);

ALTER TABLE personal_study_plan
ADD CONSTRAINT fk_personal_study_plan_source
FOREIGN KEY (source_plan_id)
REFERENCES personal_study_plan(id)
ON DELETE SET NULL;
