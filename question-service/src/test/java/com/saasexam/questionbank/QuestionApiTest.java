package com.saasexam.questionbank;

import com.jayway.jsonpath.JsonPath;
import com.saasexam.common.NotFoundException;
import com.saasexam.questionbank.client.CatalogClient;
import com.saasexam.questionbank.client.CatalogClient.ChapterRef;
import com.saasexam.questionbank.client.CatalogClient.SubjectRef;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class QuestionApiTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    CatalogClient catalog;

    @Test
    void seedDataIsLoaded() throws Exception {
        mvc.perform(get("/api/questions/6490999ab704c")).andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Who is the inventor of the computer?"))
                .andExpect(jsonPath("$.answers[0].text").value("Einsteen"))
                .andExpect(jsonPath("$.answers[0].correct").value(false));
        mvc.perform(get("/api/questions/nope")).andExpect(status().isNotFound());
    }

    @Test
    void questionLifecycle() throws Exception {
        when(catalog.getSubject("sub1")).thenReturn(new SubjectRef("sub1", "org1", "cls1"));
        when(catalog.getChapter("ch1")).thenReturn(new ChapterRef("ch1", "sub1"));
        when(catalog.getChapter("other")).thenReturn(new ChapterRef("other", "sub2"));
        when(catalog.getSubject("gone")).thenThrow(new NotFoundException("Subject", "gone"));

        String question = body(postJson("/api/subjects/sub1/questions", """
                {"chapterId":"ch1","text":"2 + 2 = ?","type":"text","answers":[
                  {"text":"3","correct":false},{"text":"4","correct":true}]}"""));
        String questionId = JsonPath.read(question, "$.id");
        String rightId = JsonPath.read(question, "$.answers[1].id");
        body(postJson("/api/subjects/sub1/questions", """
                {"text":"Is 7 prime?","type":"text","answers":[{"text":"Yes","correct":true}]}"""));

        mvc.perform(get("/api/chapters/ch1/questions")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].orgId").value("org1"))
                .andExpect(jsonPath("$.content[0].answers", hasSize(2)));
        mvc.perform(get("/internal/question-counts").param("orgId", "org1")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/internal/questions").param("ids", questionId, "missing")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].answers[1].correct").value(true));

        // a chapter from another subject, or a subject the catalog no longer has, is rejected
        json(put("/api/questions/" + questionId), "{\"chapterId\":\"other\",\"text\":\"x\",\"type\":\"text\"}")
                .andExpect(status().isBadRequest());
        json(post("/api/subjects/gone/questions"), "{\"text\":\"x\",\"type\":\"text\"}")
                .andExpect(status().isNotFound());

        // the catalog deleted the class: its questions and answers go too
        json(post("/internal/hierarchy-deletions"), "{\"level\":\"CLASS\",\"id\":\"cls1\"}")
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/questions/" + questionId)).andExpect(status().isNotFound());
        mvc.perform(get("/api/answers/" + rightId)).andExpect(status().isNotFound());
        mvc.perform(get("/internal/question-counts").param("orgId", "org1")).andExpect(jsonPath("$", hasSize(0)));
    }

    private String body(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    }

    private static MockHttpServletRequestBuilder postJson(String url, String json) {
        return post(url).contentType(MediaType.APPLICATION_JSON).content(json);
    }

    private ResultActions json(MockHttpServletRequestBuilder request, String json) throws Exception {
        return mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(json));
    }
}
