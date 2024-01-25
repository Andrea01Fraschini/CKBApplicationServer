package BersaniChiappiniFraschini.CKBApplicationServer.dashboard;

import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponse {
    private String account_type;
    private List<NotificationDetails> notifications;
    private List<CardInfo> cards;
}
