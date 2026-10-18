package nz.ac.auckland.se325.robolab;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Task 1 — the SQL-injection flaw must be closed. These start RED on the starter and turn
 * GREEN once user input can no longer change the shape of a query. Don't edit this file.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Task 1 — SQL injection is fixed")
class Task1SecurityTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private MockHttpSession login(String user, String pass) throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", user, "password", pass))))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession();
    }

    private String body(Map<String, ?> m) throws Exception {
        return json.writeValueAsString(m);
    }

    @Test
    @DisplayName("login cannot be bypassed by commenting out the password check")
    void loginBypassRejected() throws Exception {
        mvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("username", "admin' --", "password", "whatever"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("login cannot be bypassed with a tautology")
    void tautologyRejected() throws Exception {
        mvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("username", "alice", "password", "' OR '1'='1"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("the search box cannot leak other users' bookings")
    void searchCannotLeakOthers() throws Exception {
        MockHttpSession alice = login("alice", "alice123");
        mvc.perform(get("/api/bookings/mine")
                        .param("q", "') OR 1=1 OR LOWER(r.name) LIKE LOWER('").session(alice))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].username", everyItem(is("alice"))));
    }

    @Test
    @DisplayName("an apostrophe in the search box is treated as text, not SQL")
    void apostropheInSearchIsSafe() throws Exception {
        MockHttpSession alice = login("alice", "alice123");
        mvc.perform(get("/api/bookings/mine").param("q", "O'Brien").session(alice))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("an apostrophe in a booking purpose is stored, not broken")
    void apostropheInPurposeStored() throws Exception {
        MockHttpSession alice = login("alice", "alice123");
        mvc.perform(post("/api/bookings").session(alice).contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("robotId", 2, "date", "2026-10-28",
                                "purpose", "Alice's O'Brien test"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.purpose", is("Alice's O'Brien test")));
    }

    @Test
    @DisplayName("the admin report is not injectable through its date range")
    void adminReportNotInjectable() throws Exception {
        MockHttpSession admin = login("admin", "R0b0t!Adm1n");
        mvc.perform(get("/api/admin/usage").param("from", "O'Brien").param("to", "2026-11-01").session(admin))
                .andExpect(status().isOk());
    }
}
