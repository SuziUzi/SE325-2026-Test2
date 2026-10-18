package nz.ac.auckland.se325.robolab.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import nz.ac.auckland.se325.robolab.model.RobotUsage;
import nz.ac.auckland.se325.robolab.service.AdminReportService;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminReportService reports;

    public AdminController(AdminReportService reports) {
        this.reports = reports;
    }

    /** GET /api/admin/usage?from=2026-10-01&to=2026-11-01 (lab managers only) */
    @GetMapping("/usage")
    public List<RobotUsage> usage(HttpSession session, @RequestParam String from, @RequestParam String to) {
        CurrentUser.requireAdmin(session);
        return reports.usage(from, to);
    }
}
