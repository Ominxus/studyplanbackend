# Study Plan System — Validation Plan

## Purpose

This document records controlled validation scenarios for the personalized
study-planning system.

The evaluation covers:

1. deterministic schedule generation;
2. progress-aware workload calculation;
3. adaptive replanning;
4. plan lifecycle consistency;
5. AI planning insights.

---

## Deterministic Planner Tests

### D01 — Full workload scheduling

**Input**
- Pending deadline with known estimated workload
- Sufficient availability
- No completed study time

**Expected**
- Total planned minutes equal estimated workload
- Sessions remain inside availability
- Session length does not exceed configured maximum
- Daily study limit is respected

**Status**
Not yet executed formally.

---

### D02 — Partial completed workload

**Input**
- Estimated workload: 600 minutes
- Previously completed actual study: 525 minutes

**Expected**
- Remaining workload = 75 minutes
- Exactly 75 minutes are scheduled

**Observed**
- One 75-minute session was generated.

**Result**
PASS

---

### D03 — Completed workload exceeds estimate

**Input**
- Estimated workload: 480 minutes
- Previously completed actual study: 525 minutes

**Expected**
- Remaining workload = 0
- No additional study sessions generated

**Observed**
- Generation rejected with HTTP 400:
  "There is no remaining deadline workload to schedule in this planning period."

**Result**
PASS

---

### D04 — Preferred study period

**Input**
- Preferred study period: EVENING
- Availability includes evening-compatible windows

**Expected**
- Preferred-period capacity is used before non-preferred availability

**Observed**
- Sessions were scheduled during evening availability when sufficient capacity existed.

**Result**
PASS

---

### D05 — Preferred-period fallback

**Input**
- Preferred period does not contain enough available capacity
- Additional non-preferred availability exists

**Expected**
- Remaining workload is scheduled outside the preferred period
- Rationale explains the fallback

**Observed**
- Adaptive sessions used non-preferred Friday capacity and explicitly explained why.

**Result**
PASS

---

## Progress and Adaptive Planner Tests

### A01 — Actual progress reduces remaining workload

**Input**
- Original workload: 480 minutes
- Completed actual study: 165 minutes

**Expected**
- Remaining workload = 315 minutes

**Observed**
- Adaptive plan generated 315 minutes.

**Result**
PASS

---

### A02 — Adaptive plan lineage

**Expected**
- New plan uses generation method ADAPTIVE
- source_plan_id references previous plan
- Previous plan becomes SUPERSEDED

**Observed**
- Adaptive plan #5 referenced source plan #4.
- Plan #4 became SUPERSEDED.

**Result**
PASS

---

### A03 — Superseded session protection

**Expected**
- A PLANNED session belonging to a SUPERSEDED plan cannot be completed

**Observed**
- PATCH request returned HTTP 400:
  "This session belongs to a superseded study plan."

**Result**
PASS

---

## Plan Lifecycle Tests

### L01 — Single current plan

**Expected**
- At most one plan per student has status GENERATED
- Older plans automatically become SUPERSEDED

**Observed**
- After generation of a new test plan, all previous plans became SUPERSEDED.
- Query returned exactly one GENERATED plan.

**Result**
PASS

---

### L02 — Current-plan lookup

**Expected**
- `/api/student/plans/latest` returns only a GENERATED plan
- If none exists, endpoint returns HTTP 404

**Observed**
- With all historical plans SUPERSEDED, endpoint returned HTTP 404:
  "No current study plan found."

**Result**
PASS

---

## AI Planning Assistant Tests

### AI01 — Structured response

**Expected**
Response contains:
- academicOverview
- topPriority
- recommendations
- riskFlags
- planAssessment

**Observed**
All structured fields returned successfully.

**Result**
PASS

---

### AI02 — Progress grounding

**Input**
- Estimated workload: 480 minutes
- Recorded completed study: 525 minutes

**Expected**
- AI should recognize that estimated workload has already been completed
- AI should not claim that additional workload remains

**Observed**
- AI correctly stated that recorded study exceeded the estimated workload.
- AI recommended targeted review rather than blindly scheduling more work.

**Result**
PASS

---

### AI03 — No-current-plan awareness

**Input**
- No plan with status GENERATED

**Expected**
- AI should not treat a SUPERSEDED historical plan as current

**Observed**
- AI correctly stated that no current generated study plan exists.

**Result**
PASS

---

## AI Quality Evaluation Rubric

Each AI scenario will later be rated from 1 to 5 for:

| Criterion | Meaning |
|---|---|
| Factual grounding | Uses only supplied student data |
| Relevance | Advice addresses the actual planning situation |
| Actionability | Recommendations give useful next actions |
| Consistency | Advice agrees with deterministic/progress data |
| Clarity | Response is understandable and concise |

Maximum score per scenario: 25.

---

## Remaining Formal Scenarios

### D06 — Insufficient total availability

**Input**
- Remaining workload: 300 minutes
- Planning period: Monday 12 October 2026 only
- Available study time: 18:30–21:30 = 180 minutes

**Expected**
- Planner schedules only the available 180 minutes
- No session exceeds the availability window
- Remaining 120 minutes are reported as unscheduled

**Observed**
- One session was generated from 18:30 to 21:30
- Planned duration: 180 minutes
- Summary reported 2 hours of workload could not fit

**Result**
PASS

### D07 — Maximum daily study limit

**Input**
- Remaining workload: 500 minutes
- Planning period: Friday 16 October 2026 only
- Raw availability: 00:00–17:00
- Maximum daily study time: 360 minutes
- Break duration: 30 minutes
- Preferred study period: EVENING

**Expected**
- Total scheduled study does not exceed 360 minutes
- Break requirement is respected
- Remaining workload is reported as unscheduled

**Observed**
- Two 180-minute sessions were generated
- Total planned study: 360 minutes
- A 30-minute break separated the sessions
- 140 minutes (2 hr 20 min) remained unscheduled
- Both sessions were correctly identified as outside the preferred evening period

**Result**
PASS

### D08 — Maximum session duration

**Input**
- Remaining workload: 300 minutes
- Preferred session duration: 240 minutes
- Maximum session duration: 240 minutes
- Break duration: 30 minutes
- Planning period: Friday 16 October 2026

**Expected**
- No individual study session exceeds 240 minutes
- Total scheduled workload remains 300 minutes
- Break requirement is respected

**Observed**
- One 240-minute session was generated
- One 60-minute session was generated
- No session exceeded the configured maximum
- A 30-minute break separated the sessions
- All 300 minutes of remaining workload were scheduled

**Result**
PASS

### D09 — Multiple competing deadlines

**Input**
- Planning period: Monday 12 October 2026
- Available capacity: 180 minutes
- Networking remaining workload: 120 minutes
- Networking priority score: 48
- Software Testing workload: 180 minutes
- Software Testing deadline due within 3 days
- Software Testing priority score: 54

**Expected**
- Limited capacity forces the planner to choose between competing deadlines
- Higher-scoring Software Testing work is scheduled before Networking

**Observed**
- One 180-minute Software Testing session was generated
- Software Testing received priority score 54
- Networking received lower priority score 48 and did not fit into the available period

**Result**
PASS

### D10 — Urgency changes priority ordering

**Input**
- Same courses, importance values, difficulty values, priorities, goals and planning period as D09
- Only the Software Testing due date was moved from 15 October to 20 November 2026

**Expected**
- Software Testing loses its urgency bonus
- Networking becomes the higher-priority workload
- Scheduling order changes accordingly

**Observed**
- Networking priority score remained 48
- Software Testing priority score fell from 54 to 39
- Networking was scheduled first for 120 minutes
- After the required 30-minute break, the remaining 30 minutes of availability were allocated to Software Testing

**Result**
PASS

### A04 — Multiple adaptive replans in sequence

**Input**
- Identified Networking workload temporarily set to 1125 minutes
- Existing completed study before the scenario: 525 minutes
- Initial remaining workload: 600 minutes
- Base deterministic plan generated for the remaining workload

**Sequence**
1. Base deterministic plan #15 scheduled 600 minutes.
2. One 180-minute session was completed.
3. Cumulative completed study increased to 705 minutes.
4. Adaptive plan #16 was generated from plan #15.
5. Remaining workload was correctly calculated as 420 minutes.
6. Another 180-minute session was completed from plan #16.
7. Cumulative completed study increased to 885 minutes.
8. Adaptive plan #17 was generated from plan #16.
9. Remaining workload was correctly calculated as 240 minutes.

**Expected**
- Completed actual study is accumulated across the complete plan history
- Each adaptive plan schedules only the remaining workload
- Each newly generated adaptive plan references its immediate source plan
- The previous current plan becomes SUPERSEDED
- Only the newest plan remains GENERATED

**Observed**
- Plan #15: DETERMINISTIC, SUPERSEDED
- Plan #16: ADAPTIVE, source plan #15, SUPERSEDED
- Plan #17: ADAPTIVE, source plan #16, GENERATED
- First adaptive remaining workload: 420 minutes
- Second adaptive remaining workload: 240 minutes
- Final summary recognised 885 completed minutes from 1125 total minutes
- All 240 remaining minutes were scheduled

**Result**
PASS

### L03 — Plan history ordering and statistics

**Input**
- Existing historical deterministic and adaptive study plans
- History requested through `/api/student/plans/history`
- Database statistics independently calculated using SQL

**Expected**
- Plans are returned in descending generation-time order
- Session counts match the database
- Completed-session counts match the database
- Planned-minute totals match the database
- Actual completed-minute totals match the database
- Adaptive source-plan relationships are preserved

**Observed**
- Plans were returned newest-first in the order #7, #6, #5, #4, #3, #2, #1
- Every returned session count matched the SQL calculation
- Every completed-session count matched the SQL calculation
- Every planned-minute total matched the SQL calculation
- Every actual completed-minute total matched the SQL calculation
- Adaptive lineage was correctly reported:
  - Plan #5 referenced source plan #4
  - Plan #6 referenced source plan #5

**Result**
PASS

### AI04 — Competing courses and deadlines

**Input**
- Networking Final Exam due 15 November 2026
- Networking estimated workload: 480 minutes
- Networking completed study: 525 minutes
- Active Networking preparation goal
- Temporary Software Testing exam due 13 October 2026
- Software Testing estimated workload: 180 minutes
- Importance: 5/5
- No completed Software Testing study time
- No current GENERATED study plan

**Expected**
- AI identifies Software Testing as the immediate priority
- AI recognises that Networking has already met or exceeded its estimated workload
- AI does not invent additional required Networking workload
- AI identifies relevant availability before the urgent deadline
- AI recognises that no current generated study plan exists

**Observed**
- Software Testing was correctly identified as the top priority
- The response stated that all 180 minutes remained for Software Testing
- The response correctly identified Monday evening as the available study window before the exam
- Networking was correctly reported as having 525 completed minutes against a 480-minute estimate
- The response recommended readiness checking rather than blindly assigning more Networking workload
- The response correctly stated that no generated study plan currently exists
- No unsupported student facts or workload values were introduced

**AI quality rubric**
- Factual grounding: 5/5
- Relevance: 5/5
- Actionability: 5/5
- Consistency: 5/5
- Clarity: 5/5
- Total: 25/25

**Result**
PASS

- AI05: insufficient availability risk detection
- AI06: preference mismatch detection
