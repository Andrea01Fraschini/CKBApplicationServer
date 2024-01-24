package BersaniChiappiniFraschini.CKBApplicationServer.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    @GetMapping("/dashboard")
    public DashboardResponse dashboard(){
        return dashboardService.getDashboard();
    }
}
