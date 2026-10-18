package nz.ac.auckland.se325.robolab;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
 * Task 5 — cancel a booking. A student may cancel only their own; an admin may cancel anyone's.
 * These start RED (the endpoint doesn't exist yet) and turn GREEN once DELETE /api/bookings/{id}
 * is implemented with that rule. Don't edit this file.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Task 5 — cancel a booking")
class Task5CancellationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private MockHttpSession login(String user, String pass) throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", user, "password", pass))))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession();
    }

    @Test
    @DisplayName("a student can cancel their own booking")
    void studentCancelsOwnBooking() throws Exception {
        MockHttpSession alice = login("alice", "alice123");
        // Booking 101 belongs to alice.
        mvc.perform(delete("/api/bookings/101").session(alice)).andExpect(status().isNoContent());
        mvc.perform(get("/api/bookings/mine").session(alice))
                .andExpect(jsonPath("$[*].id", not(hasItem(101))));
    }

    @Test
    @DisplayName("a student cannot cancel someone else's booking")
    void studentCannotCancelOthers() throws Exception {
        // Booking 104 belongs to bob.
        mvc.perform(delete("/api/bookings/104").session(login("alice", "alice123")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("an admin can cancel anyone's booking")
    void adminCancelsAnyonesBooking() throws Exception {
        // Booking 104 belongs to bob; admin may still cancel it.
        mvc.perform(delete("/api/bookings/104").session(login("admin", "R0b0t!Adm1n")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("cancelling a booking that does not exist is a 404")
    void cancelMissingBooking() throws Exception {
        mvc.perform(delete("/api/bookings/9999").session(login("alice", "alice123")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("cancelling requires logging in")
    void cancelRequiresLogin() throws Exception {
        mvc.perform(delete("/api/bookings/101")).andExpect(status().isUnauthorized());
    }
}
