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

- D08: maximum session duration
- D09: multiple competing deadlines
- D10: urgency changes priority ordering
- A04: multiple adaptive replans in sequence
- L03: plan history order and statistics
- AI04: competing courses/deadlines
- AI05: insufficient availability risk detection
- AI06: preference mismatch detection
