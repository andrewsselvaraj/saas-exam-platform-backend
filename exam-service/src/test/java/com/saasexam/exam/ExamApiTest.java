package com.saasexam.exam;

import com.jayway.jsonpath.JsonPath;
import com.saasexam.exam.client.CatalogClient;
import com.saasexam.exam.client.CatalogClient.SubjectRef;
import com.saasexam.exam.client.QuestionBankClient;
import com.saasexam.exam.client.QuestionBankClient.AnswerRef;
import com.saasexam.exam.client.QuestionBankClient.QuestionRef;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExamApiTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    CatalogClient catalog;

    @MockitoBean
    QuestionBankClient questionBank;

    @Test
    void seedAttemptIsScored() throws Exception {
        mvc.perform(get("/api/exams/66c9933e02e56")).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuestions").value(6))
                .andExpect(jsonPath("$.correctCount").value(1));
        mvc.perform(get("/api/exams/nope")).andExpect(status().isNotFound());
    }

    @Test
    void submitAndReadBack() throws Exception {
        when(catalog.getSubject("sub1")).thenReturn(new SubjectRef("sub1", "org1", "cls1"));
        when(questionBank.findLive(any())).thenReturn(List.of(
                new QuestionRef("q1", "sub1", List.of(new AnswerRef("a1", "q1", false), new AnswerRef("a2", "q1", true))),
                new QuestionRef("q2", "sub1", List.of(new AnswerRef("b1", "q2", true), new AnswerRef("b2", "q2", false))),
                new QuestionRef("q3", "sub2", List.of(new AnswerRef("c1", "q3", true)))));

        String result = submit("""
                {"subjectId":"sub1","takenBy":"student@school.com","choices":[
                  {"questionId":"q1","answerId":"a2"},{"questionId":"q2","answerId":"b2"}]}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orgId").value("org1"))
                .andReturn().getResponse().getContentAsString();
        String sessionId = JsonPath.read(result, "$.sessionId");

        // the stored outcome no longer depends on the question bank
        when(questionBank.findLive(any())).thenReturn(List.of());
        mvc.perform(get("/api/exams/" + sessionId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.correctCount").value(1))
                .andExpect(jsonPath("$.scorePercent").value(50.0));
        mvc.perform(get("/api/exams").param("subjectId", "sub1")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void invalidChoicesAreRejected() throws Exception {
        when(catalog.getSubject("sub1")).thenReturn(new SubjectRef("sub1", "org1", "cls1"));
        when(questionBank.findLive(any())).thenReturn(List.of(
                new QuestionRef("q1", "sub1", List.of(new AnswerRef("a1", "q1", true))),
                new QuestionRef("q3", "sub2", List.of(new AnswerRef("c1", "q3", true)))));

        submit("""
                {"subjectId":"sub1","takenBy":"s","choices":[{"questionId":"q1","answerId":"c1"}]}""")
                .andExpect(status().isBadRequest());
        submit("""
                {"subjectId":"sub1","takenBy":"s","choices":[{"questionId":"q3","answerId":"c1"}]}""")
                .andExpect(status().isBadRequest());
        submit("""
                {"subjectId":"sub1","takenBy":"s","choices":[{"questionId":"missing","answerId":"x"}]}""")
                .andExpect(status().isNotFound());
    }

    private ResultActions submit(String json) throws Exception {
        return mvc.perform(post("/api/exams").contentType(MediaType.APPLICATION_JSON).content(json));
    }
}
