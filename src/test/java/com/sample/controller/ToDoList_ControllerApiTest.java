package com.sample.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.sample.entity.ToDoList;
import com.sample.service.ToDoList_Service;

/**
 * API tests for the /todo endpoints.
 *
 * Isolation: the service keeps tasks in an in-memory map shared by the Spring
 * context, so every test starts and ends with an empty store (see clearStore()).
 * Each test creates its own data and never relies on another test.
 */
@SpringBootTest
class ToDoList_ControllerApiTest {

    private static final String BASE = "/todo";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ToDoList_Service service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        clearStore();
    }

    // Runs even when an assertion in the test failed.
    @AfterEach
    void tearDown() {
        clearStore();
    }

    private void clearStore() {
        for (ToDoList t : new ArrayList<>(service.getAll())) {
            service.deleteTask(t.gettId());
        }
        assertTrue(service.getAll().isEmpty(), "store must be empty");
    }

    private ResultActions postJson(String json) throws Exception {
        return mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private ResultActions putJson(int id, String json) throws Exception {
        return mockMvc.perform(put(BASE + "/update/" + id).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    /** Creates a task through the API and returns its generated id. */
    private int createTask(String title, String dueDateOrNull) throws Exception {
        String due = dueDateOrNull == null ? "" : ",\"dueDate\":\"" + dueDateOrNull + "\"";
        postJson("{\"title\":\"" + title + "\",\"completed\":false" + due + "}")
                .andExpect(status().isCreated());
        return service.getAll().stream()
                .filter(t -> t.gettitle().equals(title))
                .findFirst().orElseThrow().gettId();
    }

    private void assertStandardError(ResultActions result, int status, String error, String path) throws Exception {
        result.andExpect(status().is(status))
                .andExpect(jsonPath("$.error").value(error))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value(path))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
        String body = result.andReturn().getResponse().getContentAsString();
        Set<String> fields = new TreeSet<>(
                com.jayway.jsonpath.JsonPath.<java.util.Map<String, Object>>read(body, "$").keySet());
        assertEquals(Set.of("error", "message", "path", "timestamp"), fields,
                "error response must contain exactly error, message, path, timestamp");
    }

    // ---------- POST /todo ----------

    @Test
    void createTask_withValidTitleAndDueDate_returns201AndAllFields() throws Exception {
        postJson("{\"title\":\"Write report\",\"completed\":false,\"dueDate\":\"2030-05-17\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tId").isNumber())
                .andExpect(jsonPath("$.title").value("Write report"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.dueDate").value("2030-05-17"));
    }

    @Test
    void createTask_withoutDueDate_returns201AndNullDueDate() throws Exception {
        postJson("{\"title\":\"No due date\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tId").isNumber())
                .andExpect(jsonPath("$.title").value("No due date"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.dueDate").isEmpty());
    }

    @Test
    void createTask_assignsDistinctIds() throws Exception {
        int first = createTask("First", null);
        int second = createTask("Second", null);
        assertTrue(first != second, "ids must be unique");
    }

    @Test
    void createTask_withBlankTitle_returns400WithStandardError() throws Exception {
        assertStandardError(postJson("{\"title\":\"   \",\"completed\":false}"), 400, "Bad Request", BASE);
        assertTrue(service.getAll().isEmpty(), "invalid task must not be stored");
    }

    @Test
    void createTask_withMissingTitle_returns400WithStandardError() throws Exception {
        assertStandardError(postJson("{\"completed\":false}"), 400, "Bad Request", BASE);
    }

    @Test
    void createTask_withMalformedDueDate_returns400WithStandardError() throws Exception {
        assertStandardError(postJson("{\"title\":\"Bad date\",\"dueDate\":\"13/45/2030\"}"),
                400, "Bad Request", BASE);
        assertTrue(service.getAll().isEmpty(), "invalid task must not be stored");
    }

    @Test
    void createTask_withImpossibleCalendarDate_returns400() throws Exception {
        assertStandardError(postJson("{\"title\":\"Bad date\",\"dueDate\":\"2030-02-31\"}"),
                400, "Bad Request", BASE);
    }

    @Test
    void createTask_withMalformedJson_returns400WithStandardError() throws Exception {
        assertStandardError(postJson("{not json"), 400, "Bad Request", BASE);
    }

    // ---------- GET ----------

    @Test
    void readAll_whenNoTasks_returnsEmptyArray() throws Exception {
        mockMvc.perform(get(BASE + "/readall"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void readAll_returnsTasksCreatedByTheTest() throws Exception {
        createTask("Alpha", "2030-01-01");
        createTask("Beta", null);

        mockMvc.perform(get(BASE + "/readall"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.title=='Alpha')].dueDate").value("2030-01-01"))
                .andExpect(jsonPath("$[?(@.title=='Beta')]").exists());
    }

    @Test
    void readById_withExistingId_returns200AndTask() throws Exception {
        int id = createTask("Readable", "2031-12-31");

        mockMvc.perform(get(BASE + "/read/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tId").value(id))
                .andExpect(jsonPath("$.title").value("Readable"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.dueDate").value("2031-12-31"));
    }

    @Test
    void readById_withMissingId_returns404WithStandardError() throws Exception {
        assertStandardError(mockMvc.perform(get(BASE + "/read/9999")), 404, "Not Found", BASE + "/read/9999");
    }

    // ---------- PUT ----------

    @Test
    void updateTask_withExistingId_updatesAllFields() throws Exception {
        int id = createTask("Before", "2030-01-01");

        putJson(id, "{\"tId\":" + id + ",\"title\":\"After\",\"completed\":true,\"dueDate\":\"2032-06-30\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tId").value(id))
                .andExpect(jsonPath("$.title").value("After"))
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.dueDate").value("2032-06-30"));

        mockMvc.perform(get(BASE + "/read/" + id))
                .andExpect(jsonPath("$.title").value("After"))
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.dueDate").value("2032-06-30"));
    }

    @Test
    void updateTask_canClearDueDate() throws Exception {
        int id = createTask("Has date", "2030-01-01");

        putJson(id, "{\"title\":\"Has date\",\"completed\":false,\"dueDate\":null}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dueDate").isEmpty());
    }

    @Test
    void updateTask_withMissingId_returns404WithStandardError() throws Exception {
        assertStandardError(putJson(9999, "{\"title\":\"Ghost\",\"completed\":false}"),
                404, "Not Found", BASE + "/update/9999");
    }

    @Test
    void updateTask_withBlankTitle_returns400AndKeepsOriginal() throws Exception {
        int id = createTask("Original", null);

        assertStandardError(putJson(id, "{\"title\":\"\",\"completed\":false}"),
                400, "Bad Request", BASE + "/update/" + id);

        mockMvc.perform(get(BASE + "/read/" + id))
                .andExpect(jsonPath("$.title").value("Original"));
    }

    @Test
    void updateTask_withInvalidDate_returns400AndKeepsOriginal() throws Exception {
        int id = createTask("Original", "2030-01-01");

        assertStandardError(putJson(id, "{\"title\":\"Original\",\"dueDate\":\"not-a-date\"}"),
                400, "Bad Request", BASE + "/update/" + id);

        mockMvc.perform(get(BASE + "/read/" + id))
                .andExpect(jsonPath("$.dueDate").value("2030-01-01"));
    }

    // ---------- DELETE ----------

    @Test
    void deleteTask_withExistingId_returns204WithNoBodyAndRemovesTask() throws Exception {
        int id = createTask("Disposable", null);

        mockMvc.perform(delete(BASE + "/delete/" + id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get(BASE + "/read/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void deleteTask_withMissingId_returns404WithStandardError() throws Exception {
        assertStandardError(mockMvc.perform(delete(BASE + "/delete/9999")),
                404, "Not Found", BASE + "/delete/9999");
    }

    @Test
    void deleteTask_twice_secondCallReturns404() throws Exception {
        int id = createTask("Once", null);

        mockMvc.perform(delete(BASE + "/delete/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(delete(BASE + "/delete/" + id)).andExpect(status().isNotFound());
    }

    // ---------- Isolation guard ----------

    @Test
    void store_isEmptyAtStartOfEachTest_evenAfterOtherTestsCreatedData() throws Exception {
        assertTrue(service.getAll().isEmpty());
        createTask("Leftover candidate", null);
        assertEquals(1, service.getAll().size());
    }
}
