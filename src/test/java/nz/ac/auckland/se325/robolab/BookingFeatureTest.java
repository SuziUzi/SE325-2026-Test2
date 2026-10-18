package nz.ac.auckland.se325.robolab;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Functional behaviour of the booking system. These must keep passing after your fix. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookingFeatureTest {

    @Autowired
    MockMvc mvc;

    MockHttpSession login(String username, String password) throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession();
    }

    @Test
    void validLoginReturnsUser() throws Exception {
        MockHttpSession session = login("alice", "alice123");
        mvc.perform(get("/api/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"nope\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listsAllRobots() throws Exception {
        mvc.perform(get("/api/robots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void showsTheDaysARobotIsBooked() throws Exception {
        mvc.perform(get("/api/robots/1/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].bookingId", contains(101, 104)))
                .andExpect(jsonPath("$[*].bookedBy", contains("alice", "bob")))
                .andExpect(jsonPath("$[*].date", contains("2026-10-12", "2026-10-15")));
    }

    @Test
    void myBookingsDefaultToDateOrder() throws Exception {
        mvc.perform(get("/api/bookings/mine").session(login("alice", "alice123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", contains(101, 102, 103)));
    }

    @Test
    void myBookingsCanBeSearchedByRobotName() throws Exception {
        mvc.perform(get("/api/bookings/mine").param("q", "arm").session(login("alice", "alice123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", contains(101, 103)));
    }

    @Test
    void myBookingsCanBeSortedByRobotNameDescending() throws Exception {
        mvc.perform(get("/api/bookings/mine").param("sort", "robot_name").param("dir", "desc")
                        .session(login("alice", "alice123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].robotName", contains("UR5e Arm", "Spot Quadruped", "Panda Arm")));
    }

    @Test
    void canCreateABooking() throws Exception {
        MockHttpSession session = login("alice", "alice123");
        mvc.perform(post("/api/bookings").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"robotId\":4,\"date\":\"2026-10-20\",\"purpose\":\"Grasp benchmark\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.robotName").value("Panda Arm"))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.date").value("2026-10-20"))
                .andExpect(jsonPath("$.purpose").value("Grasp benchmark"));
        mvc.perform(get("/api/bookings/mine").session(session)).andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void cannotBookARobotThatIsAlreadyTakenThatDay() throws Exception {
        // robot 1 is booked by alice on 2026-10-12 (seed row 101)
        mvc.perform(post("/api/bookings").session(login("charlie", "charlie123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"robotId\":1,\"date\":\"2026-10-12\",\"purpose\":\"Clashes\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void sameRobotCanBeBookedOnADifferentDay() throws Exception {
        mvc.perform(post("/api/bookings").session(login("charlie", "charlie123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"robotId\":1,\"date\":\"2026-10-19\",\"purpose\":\"Free day\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void mustBeLoggedInToSeeBookings() throws Exception {
        mvc.perform(get("/api/bookings/mine")).andExpect(status().isUnauthorized());
    }

    @Test
    void adminSeesUsageReport() throws Exception {
        mvc.perform(get("/api/admin/usage").param("from", "2026-10-01").param("to", "2026-11-01")
                        .session(login("admin", "R0b0t!Adm1n")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].robotName", contains("Panda Arm", "Spot Quadruped", "UR5e Arm")))
                .andExpect(jsonPath("$[*].bookings", contains(2, 2, 2)));
    }

    @Test
    void studentsCannotSeeUsageReport() throws Exception {
        mvc.perform(get("/api/admin/usage").param("from", "2026-10-01").param("to", "2026-11-01")
                        .session(login("alice", "alice123")))
                .andExpect(status().isForbidden());
    }
}
