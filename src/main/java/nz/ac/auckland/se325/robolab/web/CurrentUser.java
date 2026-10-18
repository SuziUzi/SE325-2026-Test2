package nz.ac.auckland.se325.robolab.web;

import org.springframework.http.HttpStatus;

import nz.ac.auckland.se325.robolab.model.User;
import jakarta.servlet.http.HttpSession;

/** Tiny session helper. The logged-in user is stored in the HTTP session by AuthController. */
public final class CurrentUser {

    static final String SESSION_KEY = "robolab.user";

    private CurrentUser() {
    }

    public static User require(HttpSession session) {
        User user = (User) session.getAttribute(SESSION_KEY);
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Please log in");
        }
        return user;
    }

    public static User requireAdmin(HttpSession session) {
        User user = require(session);
        if (!user.isAdmin()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Lab managers only");
        }
        return user;
    }
}
