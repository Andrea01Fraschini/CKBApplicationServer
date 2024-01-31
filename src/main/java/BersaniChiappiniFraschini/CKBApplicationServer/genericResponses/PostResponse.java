package BersaniChiappiniFraschini.CKBApplicationServer.genericResponses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic error response for POST requests
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {
    private String error_msg;
}
