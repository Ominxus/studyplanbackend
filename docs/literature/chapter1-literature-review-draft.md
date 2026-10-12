# 1. LITERATURE REVIEW

The purpose of this chapter is to establish the theoretical and research background for the developed personalized study-planning system. The system combines structured learner information, deterministic schedule generation, progress-aware adaptation and an artificial-intelligence advisory layer. For this reason, the literature review covers several related areas rather than treating artificial intelligence as the only theoretical foundation.

The most relevant areas are personalized learning and learner modelling, recommender systems in technology-enhanced learning, artificial intelligence and explainability in higher education, self-regulated learning and time management, and adaptive educational support. These areas provide the basis for positioning the developed system as a hybrid study-planning application in which automated scheduling and AI-assisted recommendations support, rather than replace, student decision-making.

## 1.1 Personalized Learning and Learner Modelling

Personalized learning aims to adapt educational support to differences between individual learners. The information used for personalization may include learning goals, preferences, prior behaviour, performance, knowledge state or contextual information. In digital learning environments, personalization can therefore be understood as the process of using learner-specific information to provide learning experiences, recommendations or support that are more relevant than a uniform approach.

Joy and Pillai (2022), in their review of e-learning content recommender systems, identify learner characteristics, interests, preferences and learning goals as recurring inputs used by recommendation approaches. Their review also shows that personalization is not necessarily based on a single learner characteristic. Instead, systems frequently combine several sources of information when determining which content or activity may be relevant to an individual learner.

This concept is directly relevant to the developed study-planning system. The system does not build personalization around a single preference. It combines course difficulty, course priority, deadline importance, estimated workload, deadline urgency, recurring availability, preferred study duration, break preferences, preferred study period and academic goals. These variables create an explicit learner-planning profile that can be inspected by both the application and the student.

The decision to use explicit structured information is also related to explainability. A recommendation based on clearly stored values can be traced back to the student's own input. For example, when a study session is prioritized because an examination has high importance and is approaching soon, the reason can be communicated directly. This differs from an opaque personalization approach in which the user cannot easily understand why a recommendation was produced.

Zhong (2023) reviewed personalized learning research in higher education and examined areas including learning-content structure, learning-material sequencing and support for learner readiness. The review demonstrates continued interest in personalization in higher education, but also indicates that personalized learning approaches remain diverse and are implemented in different ways depending on the learning context.

Similarly, du Plooy, Casteleijn and Franzsen (2024) examined personalized adaptive learning in higher education. Their review highlights the potential of adaptive approaches to support academic performance and engagement, while also identifying implementation challenges. These findings are relevant to the present thesis because personalization is not treated as an automatically beneficial feature. The developed system therefore separates the collection of learner information, deterministic planning rules, adaptive behaviour and AI recommendations so that each component can be independently evaluated.

The learner model used in this thesis is consequently intentionally limited and understandable. The system does not attempt to infer psychological characteristics or construct an extensive hidden profile. Instead, it focuses on information directly connected to study planning. This supports both personalization and transparency.

## 1.2 Recommender Systems in Technology-Enhanced Learning

Recommender systems are widely used to help users identify relevant items or actions when the available information or number of possible choices becomes difficult to manage. Within education, however, recommendations must consider more than preference or similarity.

Manouselis et al. (2013) describe learning recommender systems as a distinct recommendation context because educational recommendations must consider learning objectives and educational usefulness. A recommendation that is attractive to the learner is not necessarily the recommendation that best supports the learner's academic goals.

This distinction is especially important for study planning. A student may prefer to study one course or work only during a specific period of the day, but another deadline may be more urgent or important. A study-planning system must therefore balance preference with academic constraints.

The research collected by Manouselis et al. (2014) demonstrates the range of recommender-system approaches used in technology-enhanced learning. Educational recommendation can involve resources, learning activities, learning paths or other forms of support. The developed system differs from a conventional content recommender because its primary output is not a learning resource. Instead, it recommends how academic workload should be distributed over time.

Deschênes (2020) provides another important perspective by examining recommender systems in relation to learner agency. Learner agency concerns the learner's ability to participate in and maintain control over the learning process. This is particularly relevant when automated systems make decisions or recommendations.

The present study-planning system is designed to preserve this control. Students explicitly enter their courses, deadlines, goals, availability and preferences. Generated study sessions remain visible and include reasons explaining why they were prioritized or placed at a particular time. Students can also record actual study time, after which the system can adapt future planning.

The AI component follows the same principle. Artificial intelligence produces an academic overview, priority recommendation, risk flags and suggested actions, but it does not silently alter the database or directly construct the authoritative schedule. The student therefore receives additional interpretation without losing visibility into the deterministic planning process.

Educational recommender-system research consequently supports two major design decisions in this thesis. First, recommendations should be based on educationally meaningful information rather than preference alone. Second, automated recommendations should support the learner's decision-making rather than unnecessarily removing learner control.

## 1.3 Artificial Intelligence, Explainability and Generative AI in Higher Education

Artificial intelligence has become an increasingly important research area within higher education. Zawacki-Richter et al. (2019) identified several major categories of AI applications in higher education, including profiling and prediction, assessment and evaluation, adaptive systems and personalization, and intelligent tutoring systems.

For the developed system, adaptive systems and personalization are particularly relevant. The objective is not to create a general conversational assistant but to interpret structured academic planning information and support student decisions.

Crompton and Burke (2023) provide a more recent systematic review of artificial intelligence in higher education. Their review demonstrates that the field has continued to expand and that AI is being applied across multiple educational activities and contexts. The increasing variety of AI applications reinforces the need to define clearly what role AI performs within a particular system.

In this thesis, the role of AI is intentionally limited. The AI service receives a structured representation of the student's planning situation and returns structured planning insights. It is not given unrestricted authority over scheduling or persistence.

This design is also influenced by research on explainable artificial intelligence. Khosravi et al. (2022) argue that explainability in educational AI presents particular requirements because explanations may affect learners, educators and educational decision-making. Transparency is therefore not simply a technical issue; it also affects whether users can understand and appropriately use AI-supported recommendations.

This consideration supports the hybrid architecture developed in the thesis. Deterministic rules calculate workload priorities and enforce scheduling constraints, while the AI layer provides interpretation. The system can therefore explain both the quantitative planning process and the higher-level recommendation separately.

The development of generative AI has created further opportunities for educational systems. Lo (2023) reviewed early literature concerning ChatGPT in education and identified potential uses in areas such as educational assistance, explanation and personalized support. At the same time, the review highlights concerns regarding inaccurate generated information and the need for appropriate human oversight.

These concerns are directly relevant to the thesis architecture. A generative model may provide useful natural-language interpretation, but an incorrect model response should not be able to create invalid study sessions, ignore availability or bypass ownership rules. Hard constraints therefore remain implemented in application code.

More recent work provides an additional perspective on this division of responsibility. Liang et al. (2026) systematically reviewed empirical research on generative AI in education from a human-AI interaction perspective. Their work considers different relationships between human control and AI automation.

The present system follows a comparatively human-controlled approach. AI assists with interpretation and recommendations, while the student and deterministic application logic retain control over the actual planning state. This division reflects the thesis objective of obtaining value from generative AI without making non-deterministic model output the sole authority for academic scheduling.

AI is therefore considered an additional decision-support layer rather than a replacement for the planner. This distinction is central to the technical and methodological design of the thesis.

## 1.4 Self-Regulated Learning, Time Management and Study Planning

A study-planning system can also be understood through the concept of self-regulated learning. Self-regulated learning describes processes through which students actively manage aspects of their own learning rather than acting only as passive recipients of instruction.

Zimmerman (2002) describes self-regulated learners as active participants in their learning processes. Self-regulation involves processes related to goal setting, strategy selection, monitoring and reflection. These concepts closely correspond to several functions implemented in the study-planning system.

The student first provides goals and academic obligations. The planner then converts these obligations into scheduled activity. The student records progress after study sessions, and this progress influences subsequent planning. This produces a repeating process of planning, action, monitoring and adaptation.

Panadero (2017) reviewed six major models of self-regulated learning and demonstrates that self-regulation incorporates cognitive, metacognitive, behavioural, motivational and emotional processes. The current system does not attempt to model all of these dimensions. Instead, it concentrates on a narrower practical part of self-regulation: the organization and monitoring of academic workload.

Time management is particularly relevant to this function. Broadbent and Poon (2015), in their systematic review of self-regulated learning strategies in online higher education, identified time management among the strategies associated with academic achievement. Their review also discusses metacognition, effort regulation and other self-regulatory strategies.

This supports the assumption that helping students organize when academic work should be completed can be a meaningful form of learning support. However, it does not imply that scheduling software alone produces academic success. Study planning should instead be considered one component of a broader self-regulatory process.

Recent evidence also supports the relevance of time-management behaviour among university students. Liu, Ma and Jia (2026) conducted a systematic review and meta-analysis examining the relationship between time management and college students' learning outcomes. Their analysis reported a positive association between time management and learning outcomes across the included studies.

The findings should be interpreted cautiously because substantial heterogeneity was reported across the studies. The research therefore supports the relevance of time management but does not justify claiming that a study planner will automatically improve grades.

This distinction is important for the thesis evaluation. The current project evaluates whether the software behaves correctly and provides logically consistent planning support. It does not claim to have demonstrated an improvement in students' academic achievement.

The study-planning workflow nevertheless aligns with several self-regulation principles. Students define goals, make their availability explicit, receive a plan, record actual behaviour and revise future plans according to progress. Adaptive replanning is particularly relevant because the system responds to what the student actually completed rather than assuming that the original schedule was followed perfectly.

The theoretical relationship can therefore be summarized as a cycle:

`academic goals and obligations → planning → study activity → progress monitoring → adaptation`

The system provides technical support for this cycle without attempting to replace the student's responsibility for learning.

## 1.5 Analogous Systems and Design Implications

The proposed system is positioned between several existing categories of educational software.

Learning management systems commonly organize courses, learning materials, assessments and administrative information. Educational recommender systems typically focus on recommending resources, activities or learning paths. Intelligent tutoring systems may provide more direct instructional guidance and feedback.

The developed system has a narrower but distinct purpose. Its primary task is to transform academic obligations and personal constraints into scheduled study activity.

This positioning influences the system design in several ways.

First, personalization must be based on meaningful student information. Course priorities, difficulty, deadlines, workload, goals, availability and study preferences are therefore represented explicitly.

Second, recommendations should remain understandable. The deterministic planner produces a rationale for each generated session, while the AI layer returns separate risk and recommendation information.

Third, learner control should be preserved. The student provides the planning constraints and retains visibility into generated sessions and historical plans.

Fourth, adaptation should be based on observed progress rather than assumptions. Completed actual study time is therefore used when recalculating remaining workload.

Finally, artificial intelligence should be applied where it offers a clear additional capability. Natural-language interpretation of competing obligations and planning risks is appropriate for AI, while exact enforcement of availability, duration limits and daily study limits remains deterministic.

These principles distinguish the system from an architecture in which a large language model receives an unrestricted prompt and independently generates the complete timetable.

## 1.6 Identified Gap and Thesis Position

The reviewed literature demonstrates substantial research in personalized learning, educational recommender systems, self-regulated learning, adaptive learning and artificial intelligence in education. However, these areas are often investigated separately.

Personalized-learning research focuses on adapting educational support to learner characteristics. Recommender-system research often concentrates on content, resources or learning activities. Self-regulated-learning research emphasizes learners' own planning, monitoring and adaptation. AI-in-education research increasingly examines automated support, prediction, tutoring and generative assistance.

The thesis brings aspects of these areas together around a practical study-planning problem.

The developed system combines:

- structured academic and learner-planning information;
- deterministic workload prioritization;
- constraint-aware study-session scheduling;
- progress and actual-time recording;
- adaptive remaining-workload calculation;
- historical plan lineage;
- AI-assisted interpretation of priorities and risks.

The key contribution is therefore not the use of artificial intelligence in isolation. The contribution is the design and implementation of a hybrid personalized planning workflow in which deterministic, adaptive and AI-assisted mechanisms perform different responsibilities.

The deterministic component provides reproducibility and hard-constraint enforcement. The adaptive component responds to recorded student behaviour. The AI component adds higher-level interpretation and natural-language guidance.

This results in the following conceptual architecture:

`Structured learner data → deterministic planning → progress-aware adaptation → AI-assisted interpretation`

The literature reviewed in this chapter supports this separation of responsibilities. Personalization provides the basis for using student-specific information, self-regulated-learning research establishes the relevance of planning and monitoring, recommender-system research supports learner-centred recommendation, adaptive-learning research supports responding to changing learner states, and AI/XAI research supports using artificial intelligence in a controlled and explainable manner.

The thesis therefore positions the developed application as a hybrid personalized study-planning system rather than as a conventional learning management system, a content recommender or an unrestricted AI chatbot.
