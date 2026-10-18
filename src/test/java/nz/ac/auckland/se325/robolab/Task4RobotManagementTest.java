package nz.ac.auckland.se325.robolab;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
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
 * Task 4 — admins can add and remove robots. These start RED (the endpoints don't exist yet) and
 * turn GREEN once POST /api/robots and DELETE /api/robots/{id} are implemented. Don't edit this file.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Task 4 — manage robots (admin)")
class Task4RobotManagementTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private MockHttpSession login(String user, String pass) throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", user, "password", pass))))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession();
    }

    private String robot(String name, String model, String location) throws Exception {
        return json.writeValueAsString(Map.of("name", name, "model", model, "location", location));
    }

    // ---- add ----

    @Test
    @DisplayName("an admin can add a robot, and it then appears in the list")
    void adminCanAddRobot() throws Exception {
        MockHttpSession admin = login("admin", "R0b0t!Adm1n");
        mvc.perform(post("/api/robots").session(admin).contentType(MediaType.APPLICATION_JSON)
                        .content(robot("Laser Cutter", "Epilog Fusion", "Bay D")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Laser Cutter")))
                .andExpect(jsonPath("$.location", is("Bay D")));

        mvc.perform(get("/api/robots").session(admin))
                .andExpect(jsonPath("$[*].name", hasItem("Laser Cutter")));
    }

    @Test
    @DisplayName("adding a robot with a blank field is rejected")
    void addRobotValidatesFields() throws Exception {
        MockHttpSession admin = login("admin", "R0b0t!Adm1n");
        mvc.perform(post("/api/robots").session(admin).contentType(MediaType.APPLICATION_JSON)
                        .content(robot("", "Epilog Fusion", "Bay D")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("a student cannot add a robot")
    void studentCannotAddRobot() throws Exception {
        mvc.perform(post("/api/robots").session(login("alice", "alice123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(robot("Laser Cutter", "Epilog Fusion", "Bay D")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("adding a robot requires logging in")
    void addRobotRequiresLogin() throws Exception {
        mvc.perform(post("/api/robots").contentType(MediaType.APPLICATION_JSON)
                        .content(robot("Laser Cutter", "Epilog Fusion", "Bay D")))
                .andExpect(status().isUnauthorized());
    }

    // ---- remove ----

    @Test
    @DisplayName("an admin can remove a robot that has no bookings")
    void adminCanRemoveUnbookedRobot() throws Exception {
        MockHttpSession admin = login("admin", "R0b0t!Adm1n");
        // Robot 3 (TurtleBot 4) has no bookings in the seed data.
        mvc.perform(delete("/api/robots/3").session(admin)).andExpect(status().isNoContent());
        mvc.perform(get("/api/robots").session(admin))
                .andExpect(jsonPath("$[*].id", not(hasItem(3))));
    }

    @Test
    @DisplayName("a robot that still has bookings cannot be removed")
    void cannotRemoveRobotWithBookings() throws Exception {
        // Robot 1 has bookings in the seed data.
        mvc.perform(delete("/api/robots/1").session(login("admin", "R0b0t!Adm1n")))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("removing a robot that does not exist is a 404")
    void removeMissingRobot() throws Exception {
        mvc.perform(delete("/api/robots/9999").session(login("admin", "R0b0t!Adm1n")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("a student cannot remove a robot")
    void studentCannotRemoveRobot() throws Exception {
        mvc.perform(delete("/api/robots/3").session(login("alice", "alice123")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("removing a robot requires logging in")
    void removeRobotRequiresLogin() throws Exception {
        mvc.perform(delete("/api/robots/3")).andExpect(status().isUnauthorized());
    }
}
