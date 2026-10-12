package com.studentplansystem.studyplangym;

import com.openai.client.OpenAIClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class StudyPlanGymApplicationTests {

    @MockBean
    private OpenAIClient openAIClient;

    @Test
    void contextLoads() {
    }
}
