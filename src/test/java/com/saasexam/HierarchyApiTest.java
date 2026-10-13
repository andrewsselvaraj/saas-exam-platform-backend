package com.saasexam;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HierarchyApiTest {

    @Autowired
    MockMvc mvc;

    @Test
    void seedDataIsLoaded() throws Exception {
        mvc.perform(get("/api/orgs")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(20)));
        mvc.perform(get("/api/orgs/6479e79b21971/classes")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Class 1 ABC"));
        mvc.perform(get("/api/questions/6490999ab704c")).andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Who is the inventor of the computer?"))
                .andExpect(jsonPath("$.answers[0].text").value("Einsteen"))
                .andExpect(jsonPath("$.answers[0].correct").value(false));
        mvc.perform(get("/api/orgs/nope")).andExpect(status().isNotFound());
    }

    @Test
    void fullHierarchyLifecycle() throws Exception {
        String orgId = id(postJson("/api/orgs", "{\"name\":\"Test School\",\"email\":\"t@school.com\"}"));
        String classId = id(postJson("/api/orgs/" + orgId + "/classes", "{\"name\":\"Class 10\"}"));
        String subjectId = id(postJson("/api/classes/" + classId + "/subjects", "{\"name\":\"Maths\"}"));
        String chapterId = id(postJson("/api/subjects/" + subjectId + "/chapters", "{\"name\":\"Algebra\"}"));

        String question = body(postJson("/api/subjects/" + subjectId + "/questions", """
                {"chapterId":"%s","text":"2 + 2 = ?","type":"text","answers":[
                  {"text":"3","correct":false},{"text":"4","correct":true}]}""".formatted(chapterId)));
        String questionId = JsonPath.read(question, "$.id");
        String wrongId = JsonPath.read(question, "$.answers[0].id");
        String rightId = JsonPath.read(question, "$.answers[1].id");

        // a second, unfiled question
        String q2 = body(postJson("/api/subjects/" + subjectId + "/questions", """
                {"text":"Is 7 prime?","type":"text","answers":[{"text":"Yes","correct":true},{"text":"No","correct":false}]}"""));
        String q2Id = JsonPath.read(q2, "$.id");
        String q2WrongId = JsonPath.read(q2, "$.answers[1].id");

        mvc.perform(get("/api/orgs/" + orgId + "/tree")).andExpect(status().isOk())
                .andExpect(jsonPath("$.classes[0].subjects[0].questionCount").value(2))
                .andExpect(jsonPath("$.classes[0].subjects[0].unfiledQuestionCount").value(1))
                .andExpect(jsonPath("$.classes[0].subjects[0].chapters[0].questionCount").value(1));
        mvc.perform(get("/api/chapters/" + chapterId + "/questions")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].answers", hasSize(2)));

        // chapter from another subject is rejected
        json(put("/api/questions/" + questionId),
                "{\"chapterId\":\"688514c8cc3a4\",\"text\":\"x\",\"type\":\"text\"}")
                .andExpect(status().isBadRequest());

        String result = body(postJson("/api/exams", """
                {"subjectId":"%s","takenBy":"student@school.com","choices":[
                  {"questionId":"%s","answerId":"%s"},{"questionId":"%s","answerId":"%s"}]}"""
                .formatted(subjectId, questionId, rightId, q2Id, q2WrongId)));
        String sessionId = JsonPath.read(result, "$.sessionId");
        mvc.perform(get("/api/exams/" + sessionId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.correctCount").value(1))
                .andExpect(jsonPath("$.scorePercent").value(50.0));
        mvc.perform(get("/api/exams").param("subjectId", subjectId)).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // answer from a different question is rejected
        json(post("/api/exams"), """
                {"subjectId":"%s","takenBy":"s","choices":[{"questionId":"%s","answerId":"%s"}]}"""
                .formatted(subjectId, q2Id, wrongId))
                .andExpect(status().isBadRequest());

        // deleting the class cascades down to answers but keeps exam history scoreable
        mvc.perform(delete("/api/classes/" + classId)).andExpect(status().isNoContent());
        mvc.perform(get("/api/subjects/" + subjectId)).andExpect(status().isNotFound());
        mvc.perform(get("/api/answers/" + rightId)).andExpect(status().isNotFound());
        mvc.perform(get("/api/orgs/" + orgId + "/classes")).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/exams/" + sessionId)).andExpect(jsonPath("$.correctCount").value(1));
    }

    private String body(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    }

    private String id(MockHttpServletRequestBuilder request) throws Exception {
        return JsonPath.read(body(request), "$.id");
    }

    private static MockHttpServletRequestBuilder postJson(String url, String json) {
        return post(url).contentType(MediaType.APPLICATION_JSON).content(json);
    }

    private ResultActions json(MockHttpServletRequestBuilder request, String json) throws Exception {
        return mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(json));
    }
}
