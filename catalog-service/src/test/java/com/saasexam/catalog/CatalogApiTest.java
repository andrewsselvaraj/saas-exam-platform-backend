package com.saasexam.catalog;

import com.jayway.jsonpath.JsonPath;
import com.saasexam.catalog.client.QuestionBankClient;
import com.saasexam.catalog.client.QuestionBankClient.QuestionCount;
import com.saasexam.common.HierarchyLevel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogApiTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    QuestionBankClient questionBank;

    @Test
    void seedDataIsLoaded() throws Exception {
        mvc.perform(get("/api/orgs")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '6479e79b21971')].name").value("ABC School"));
        mvc.perform(get("/api/orgs/6479e79b21971/classes")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Class 1 ABC"));
        mvc.perform(get("/api/orgs/nope")).andExpect(status().isNotFound());
    }

    @Test
    void hierarchyLifecycle() throws Exception {
        String orgId = id(postJson("/api/orgs", "{\"name\":\"Test School\",\"email\":\"t@school.com\"}"));
        String classId = id(postJson("/api/orgs/" + orgId + "/classes", "{\"name\":\"Class 10\"}"));
        String subjectId = id(postJson("/api/classes/" + classId + "/subjects", "{\"name\":\"Maths\"}"));
        String chapterId = id(postJson("/api/subjects/" + subjectId + "/chapters", "{\"name\":\"Algebra\"}"));

        mvc.perform(get("/api/subjects/" + subjectId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.orgId").value(orgId))
                .andExpect(jsonPath("$.classId").value(classId));

        when(questionBank.countByOrg(orgId)).thenReturn(List.of(
                new QuestionCount(subjectId, chapterId, 1), new QuestionCount(subjectId, null, 1)));
        mvc.perform(get("/api/orgs/" + orgId + "/tree")).andExpect(status().isOk())
                .andExpect(jsonPath("$.classes[0].subjects[0].questionCount").value(2))
                .andExpect(jsonPath("$.classes[0].subjects[0].unfiledQuestionCount").value(1))
                .andExpect(jsonPath("$.classes[0].subjects[0].chapters[0].questionCount").value(1));

        // deleting the class cascades down the catalog and tells the question service
        mvc.perform(delete("/api/classes/" + classId)).andExpect(status().isNoContent());
        verify(questionBank).deleteBelow(HierarchyLevel.CLASS, classId);
        mvc.perform(get("/api/subjects/" + subjectId)).andExpect(status().isNotFound());
        mvc.perform(get("/api/chapters/" + chapterId)).andExpect(status().isNotFound());
        mvc.perform(get("/api/orgs/" + orgId + "/classes")).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deleteIsRolledBackWhenQuestionServiceFails() throws Exception {
        String orgId = id(postJson("/api/orgs", "{\"name\":\"Rollback School\",\"email\":\"r@school.com\"}"));
        String classId = id(postJson("/api/orgs/" + orgId + "/classes", "{\"name\":\"Class 1\"}"));
        doThrow(new ResourceAccessException("question service down"))
                .when(questionBank).deleteBelow(eq(HierarchyLevel.CLASS), any());

        mvc.perform(delete("/api/classes/" + classId)).andExpect(status().isBadGateway());
        mvc.perform(get("/api/classes/" + classId)).andExpect(status().isOk());
    }

    private String id(MockHttpServletRequestBuilder request) throws Exception {
        String body = mvc.perform(request).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private static MockHttpServletRequestBuilder postJson(String url, String json) {
        return post(url).contentType(MediaType.APPLICATION_JSON).content(json);
    }
}
