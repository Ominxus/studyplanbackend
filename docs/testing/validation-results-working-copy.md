# 4. VALIDATION AND EVALUATION

## 4.1 Validation Methodology

The developed study-planning system was validated incrementally throughout implementation. The purpose of the validation was not only to verify that individual REST endpoints operated correctly, but also to determine whether the planning mechanisms produced logically consistent results when exposed to controlled academic planning scenarios.

Several complementary validation methods were used. REST API requests were used to test backend functionality under authenticated student access. Database queries were then used to independently verify persisted values such as generated plans, study-session durations, completion states, actual study time and plan lineage. Frontend interaction testing was used during implementation to confirm that the student interface correctly reflected backend state changes.

The planning algorithms were additionally evaluated through controlled scenario-based tests. In these tests, known input values were deliberately selected so that the expected scheduling result could be calculated before executing the planner. The generated output was then compared with the expected result.

The validation covered four major areas:

1. deterministic study-plan generation;
2. progress-aware and adaptive replanning;
3. study-plan lifecycle and history management;
4. AI-assisted planning recommendations.

The deterministic and adaptive planning components were evaluated mainly through measurable scheduling outcomes, such as total workload scheduled, session duration, daily limits, availability constraints and remaining workload.

The AI-assisted component was evaluated differently because its purpose is advisory rather than deterministic. AI responses were therefore assessed according to five criteria: factual grounding, relevance, actionability, consistency with structured planning data and clarity. Each criterion was rated on a five-point scale. The evaluation was scenario-based and intended to validate prototype behaviour rather than to provide a statistically generalizable assessment of the AI model.

---

## 4.2 Deterministic Planner Validation

### 4.2.1 Progress-Aware Workload Calculation

One of the important extensions to the deterministic planner was the ability to consider previously completed study time when calculating remaining workload.

A Networking Final Exam deadline contained an estimated workload of 480 minutes. Across previously generated plans, the student had recorded 525 minutes of completed study time.

The remaining workload was therefore calculated as:

`max(0, 480 - 525) = 0 minutes`

When plan generation was requested, the system correctly rejected the generation attempt because no estimated workload remained. The backend returned an HTTP 400 response stating that there was no remaining deadline workload to schedule within the selected planning period.

To validate partial progress separately, the estimated workload was temporarily increased to 600 minutes while completed study remained 525 minutes. The expected remaining workload was therefore 75 minutes.

The planner generated exactly one 75-minute session. This demonstrated that the deterministic planner does not automatically regenerate the complete original workload and instead considers recorded progress when calculating future study requirements.

### 4.2.2 Insufficient Availability

A controlled scenario was created with 300 minutes of remaining workload and only one available study period on Monday from 18:30 to 21:30. This provided 180 minutes of available capacity.

The expected result was:

- remaining workload: 300 minutes;
- available capacity: 180 minutes;
- schedulable workload: 180 minutes;
- unscheduled workload: 120 minutes.

The generated plan contained one session from 18:30 to 21:30 with a duration of 180 minutes. The plan summary correctly reported that two hours of workload could not fit within the selected availability and planning period.

This result demonstrates that the system does not claim that all workload has been scheduled when the student's available time is insufficient.

### 4.2.3 Maximum Daily Study Limit

The maximum daily study limit was evaluated using a scenario with 500 minutes of remaining workload and a Friday availability period from 00:00 to 17:00. Although the raw availability was much larger than the required workload, the configured maximum daily study time was 360 minutes.

The planner generated two 180-minute study sessions, resulting in exactly 360 minutes of scheduled study. A 30-minute break was preserved between the sessions.

The remaining 140 minutes were correctly reported as unscheduled.

This test confirms that the daily study limit acts as an effective scheduling constraint even when substantially more availability exists.

### 4.2.4 Maximum Session Duration

The maximum study-session duration was tested using 300 minutes of remaining workload. For this scenario, both the preferred and maximum session duration were temporarily configured to 240 minutes.

The planner divided the workload into:

- one 240-minute session;
- one 60-minute session.

The complete 300-minute workload was scheduled and no individual session exceeded the configured 240-minute maximum. A required break was also retained between the two sessions.

This demonstrates that workload is divided into multiple sessions when necessary rather than violating the configured maximum session length.

### 4.2.5 Preferred Study Period and Fallback Behaviour

Preferred study period is treated as a soft constraint rather than a mandatory restriction.

During earlier planner validation, an evening study preference was configured. Where sufficient evening availability existed, the planner placed study sessions within the preferred period.

When the preferred period did not provide sufficient capacity, the planner used additional non-preferred availability and included this fact in the generated session rationale and plan summary.

This behaviour is important because treating the preference as a hard constraint could prevent otherwise feasible study plans from being generated. The implemented approach attempts to satisfy the preference first while still allowing fallback scheduling when necessary.

### 4.2.6 Competing Deadline Prioritization

A controlled scenario was used to evaluate prioritization when two courses competed for limited study time.

The first work item was associated with the Networking Final Exam. Its calculated priority score was 48.

A temporary Software Testing deadline was created with a due date only three days away. The Software Testing course also had high course priority and difficulty values. Its calculated priority score was 54.

Only 180 minutes of study capacity were available.

The planner allocated the available 180 minutes to Software Testing, which had the higher score. The lower-scoring Networking workload could not fit into the remaining period.

This result demonstrates that prioritization is based on the combined planning factors rather than on database ordering or course identity.

### 4.2.7 Effect of Deadline Urgency

A second prioritization scenario used the same courses, workload characteristics, importance values and availability as the previous test. Only the Software Testing deadline date was changed.

After moving the deadline from 15 October to 20 November 2026, its urgency contribution was removed and its priority score fell from 54 to 39.

The Networking work item retained a score of 48.

The resulting schedule changed accordingly. Networking was scheduled first for 120 minutes. Following the configured 30-minute break, the remaining 30 minutes of the available period were allocated to Software Testing.

The comparison demonstrates that deadline urgency has a measurable influence on scheduling priority.

The two scenarios can be summarized as follows:

| Scenario | Networking score | Software Testing score | First scheduled workload |
| --- | ---: | ---: | --- |
| Urgent Software Testing deadline | 48 | 54 | Software Testing |
| Later Software Testing deadline | 48 | 39 | Networking |

This provides direct evidence that the weighted prioritization method influences the generated schedule as designed.

---

## 4.3 Adaptive Replanning Validation

### 4.3.1 Progress-Based Adaptive Replanning

Adaptive replanning was tested to determine whether recorded actual study time correctly reduces future workload.

In an initial scenario, the Networking deadline contained an estimated workload of 480 minutes. A completed study session recorded 165 minutes of actual study time.

The expected remaining workload was therefore:

`480 - 165 = 315 minutes`

The adaptive planner generated two new sessions with durations of 135 and 180 minutes, for a total of 315 minutes.

The original plan was marked as `SUPERSEDED`, while the newly generated plan used the `ADAPTIVE` generation method and stored a reference to its source plan.

This demonstrated that the adaptive planner uses actual completed study rather than simply regenerating the original estimated workload.

### 4.3.2 Sequential Adaptive Replanning

A more extensive scenario was designed to determine whether adaptive replanning could operate repeatedly across multiple generations.

For this test, the Networking workload was temporarily set to 1125 minutes. The student already had 525 minutes of recorded completed study, leaving 600 minutes remaining.

A deterministic base plan was generated for the 600-minute workload.

After completing one 180-minute session, cumulative completed study increased to:

`525 + 180 = 705 minutes`

The expected remaining workload became:

`1125 - 705 = 420 minutes`

Adaptive Plan 1 correctly generated 420 minutes of new study.

A further 180-minute session was then completed from the adaptive plan. Cumulative completed study increased to:

`705 + 180 = 885 minutes`

The expected remaining workload became:

`1125 - 885 = 240 minutes`

Adaptive Plan 2 generated exactly 240 minutes of study time.

The resulting lineage was:

| Plan | Generation method | Source plan | Status after final replan |
| --- | --- | --- | --- |
| Plan 15 | DETERMINISTIC | — | SUPERSEDED |
| Plan 16 | ADAPTIVE | Plan 15 | SUPERSEDED |
| Plan 17 | ADAPTIVE | Plan 16 | GENERATED |

The final adaptive-plan summary independently reported 14 hours 45 minutes of completed study from 18 hours 45 minutes of identified workload, leaving four hours of remaining study. These values correspond to 885, 1125 and 240 minutes respectively.

This scenario confirms that adaptive replanning can be performed repeatedly while retaining cumulative progress and preserving plan lineage.

### 4.3.3 Superseded Plan Protection

A session belonging to a superseded plan was intentionally submitted to the session-completion endpoint.

The request was rejected with HTTP 400 and the message:

`This session belongs to a superseded study plan.`

This prevents historical plans from modifying current study progress after a replacement plan has already been generated.

---

## 4.4 Plan Lifecycle and History Validation

### 4.4.1 Single Current Plan

The study-plan lifecycle was designed so that no more than one plan for a student can have the current `GENERATED` status.

During validation, generation of a new plan automatically changed previously current plans to `SUPERSEDED`.

A database query after generation confirmed that only one plan remained `GENERATED`.

The system also permits a state in which no current generated plan exists. In this state, the `/api/student/plans/latest` endpoint returns HTTP 404 with the message:

`No current study plan found.`

This behaviour ensures that a superseded historical plan is never incorrectly presented as the student's current plan.

### 4.4.2 Plan History Statistics

The plan-history endpoint was validated independently against SQL calculations.

The endpoint returned seven historical plans in descending generation-time order:

`7, 6, 5, 4, 3, 2, 1`

For every plan, the following values returned by the API matched independently calculated database values:

- total number of study sessions;
- number of completed sessions;
- total planned study minutes;
- actual completed study minutes;
- generation method;
- source-plan relationship.

For example, Plan 7 contained three sessions, one completed session, 480 planned minutes and 180 actual completed minutes. Plan 6 contained two sessions, one completed session, 315 planned minutes and 180 actual completed minutes.

Adaptive lineage was also correctly represented. Plan 5 referenced Plan 4 as its source, while Plan 6 referenced Plan 5.

This confirms that the history endpoint provides a consistent representation of both deterministic and adaptive plan evolution.

---

## 4.5 AI-Assisted Planning Evaluation

### 4.5.1 Role of the AI Layer

The AI component is not responsible for generating or validating the final timetable. Hard scheduling constraints remain under the control of the deterministic and adaptive planning logic.

Instead, the AI module receives a structured planning context containing relevant student information such as active courses, pending deadlines, goals, availability, preferences, recorded progress and the current generated plan when one exists.

The AI produces a structured response containing:

- academic overview;
- top priority;
- recommendations;
- risk flags;
- current-plan assessment.

The purpose of this architecture is to use AI for interpretation and recommendation while retaining deterministic control over constraints such as availability, valid session durations and remaining workload.

### 4.5.2 Evaluation Rubric

Three controlled AI scenarios were formally evaluated.

Each response was rated from one to five using the following criteria:

| Criterion | Description |
| --- | --- |
| Factual grounding | Whether statements were supported by the supplied planning context |
| Relevance | Whether the response focused on the actual planning situation |
| Actionability | Whether recommendations gave useful next actions |
| Consistency | Whether the response agreed with structured workload, progress and scheduling data |
| Clarity | Whether the result was understandable and clearly expressed |

The maximum score for each scenario was 25 points.

The ratings represent an internal scenario-based prototype evaluation and should not be interpreted as results from an independent user study or external expert assessment.

### 4.5.3 Competing Workloads

A temporary Software Testing exam was created with:

- due date: 13 October 2026;
- estimated workload: 180 minutes;
- importance: 5/5;
- no completed study time.

Networking simultaneously contained 525 minutes of completed study against an estimated 480 minutes and had a later final examination on 15 November.

The AI correctly identified Software Testing as the immediate priority.

It explicitly stated that 180 minutes remained for Software Testing and recognised that Networking had already exceeded its estimated study workload. Rather than recommending additional fixed Networking workload, it suggested checking readiness through topic review and practice.

The AI also correctly identified that there was no current generated study plan.

No unsupported workload values or student facts were introduced.

The response received the following internal evaluation:

| Criterion | Score |
| --- | ---: |
| Factual grounding | 5/5 |
| Relevance | 5/5 |
| Actionability | 5/5 |
| Consistency | 5/5 |
| Clarity | 5/5 |
| **Total** | **25/25** |

### 4.5.4 Insufficient Availability Risk Detection

A second AI scenario used a Software Testing examination due on 13 October with 420 minutes of estimated preparation remaining.

Only 180 minutes of declared availability existed before the deadline.

The AI correctly identified the approaching examination as the immediate priority and recognised that the available capacity was insufficient for the remaining workload.

Importantly, it did not incorrectly claim that the complete 420-minute workload could be scheduled inside the available 180-minute period.

Instead, it recommended using the existing Monday availability for focused preparation while also identifying additional study time if possible.

The response further identified the absence of a current generated study plan.

The result received:

| Criterion | Score |
| --- | ---: |
| Factual grounding | 5/5 |
| Relevance | 5/5 |
| Actionability | 5/5 |
| Consistency | 5/5 |
| Clarity | 5/5 |
| **Total** | **25/25** |

### 4.5.5 Preferred-Period Mismatch

The final formal AI scenario evaluated whether the advisory layer could distinguish between availability and a study preference.

The student preference was configured as `MORNING`. An urgent Software Testing examination was due Tuesday morning with 180 minutes of study workload remaining.

The only declared availability before the deadline was Monday from 18:30 to 21:30.

The AI explicitly recognised that the available evening study period conflicted with the student's preferred morning period. However, it did not incorrectly treat this preference as a hard constraint.

It recommended using the feasible Monday evening window because it was sufficient to cover the 180-minute workload before the deadline, while also noting that this left little additional buffer.

The response received:

| Criterion | Score |
| --- | ---: |
| Factual grounding | 5/5 |
| Relevance | 5/5 |
| Actionability | 5/5 |
| Consistency | 5/5 |
| Clarity | 5/5 |
| **Total** | **25/25** |

### 4.5.6 AI Evaluation Summary

Across the three controlled scenarios, the AI layer remained consistent with the structured planning context and did not contradict the deterministic scheduling model.

The tests demonstrate three useful advisory capabilities:

1. interpreting competing academic priorities;
2. recognising workload-capacity risks;
3. explaining conflicts between soft preferences and feasible study periods.

The AI layer therefore adds qualitative interpretation to the planning system, while the deterministic and adaptive components remain responsible for quantitative scheduling and hard constraint enforcement.

Because only a small number of controlled scenarios were evaluated, these results should be interpreted as functional validation of the implemented prototype rather than evidence of general AI recommendation quality.

---

## 4.6 Overall Results and Discussion

The validation results show that the three planning layers fulfil different but complementary roles.

The deterministic planner provides reproducible schedule generation. It converts structured academic information into study sessions while respecting availability, session-duration limits, break requirements, daily limits and remaining workload. The weighted priority mechanism also produces observable changes when deadline urgency and competing course characteristics change.

Adaptive replanning extends this baseline by introducing student progress into future planning decisions. Completed actual study time reduces the workload considered by later plans, and repeated replanning preserves both cumulative progress and the history of previous plans.

The AI component performs a different function. It does not create the authoritative timetable. Instead, it interprets structured data and communicates higher-level observations such as insufficient study capacity, immediate academic priorities and preference conflicts.

This separation reduces dependence on non-deterministic AI output for hard scheduling decisions. A generated timetable remains based on explicit application logic that can be inspected and tested, while AI is used where natural-language reasoning and recommendation are more appropriate.

The resulting architecture can therefore be summarized as:

`Structured student data → deterministic scheduling → progress-aware adaptation → AI-assisted interpretation`

This hybrid approach provides both explainability and flexibility. The deterministic and adaptive layers make scheduling behaviour reproducible, while the AI layer helps communicate planning risks and recommendations in a more student-oriented form.

---

## 4.7 Limitations of the Evaluation

Several limitations remain.

First, the validation was primarily performed through controlled scenarios using one development dataset. Although these scenarios demonstrate that the implemented logic behaves correctly for the tested cases, they do not represent a large-scale evaluation with a diverse student population.

Second, the deterministic priority weights were selected as part of the prototype design and have not been empirically calibrated through a user study or optimization dataset. The validation demonstrates that the weights operate consistently, but it does not establish that the selected values are optimal for all students.

Third, the formal AI evaluation contains only a small number of scenarios. The responses were evaluated internally using a predefined rubric rather than by independent experts or multiple human raters. The obtained scores should therefore be interpreted as prototype functional-validation results rather than general evidence of model accuracy.

Fourth, the system currently relies on students to provide realistic workload estimates, availability and preferences. The quality of generated schedules depends on the quality of these inputs.

Finally, the current evaluation focuses primarily on functional correctness and planning logic. A broader usability study could investigate whether students find the generated plans understandable, useful and practical in real academic use.

These limitations define possible areas for future evaluation without preventing the implemented system from being assessed against its current technical objectives.

---

## 4.8 Validation Conclusion

The performed validation demonstrates that the developed prototype satisfies the principal planning behaviours required by the thesis.

The deterministic planner respects explicit scheduling constraints and responds predictably to changes in workload, availability, preferences, urgency and priorities. Progress-aware generation and adaptive replanning correctly incorporate completed actual study time and preserve plan lineage. Lifecycle controls ensure that outdated plans cannot continue to modify current progress, while the history component provides a consistent record of plan evolution.

The AI-assisted component successfully interpreted several planning situations using the same structured student context. It recognised urgent competing workloads, insufficient availability and conflicts between preferred and feasible study periods without replacing the deterministic scheduling rules.

Together, the results support the use of a hybrid architecture in which deterministic logic provides scheduling reliability, adaptive functionality responds to student progress and AI provides an additional explanatory and recommendation layer.
