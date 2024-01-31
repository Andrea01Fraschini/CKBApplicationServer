package BersaniChiappiniFraschini.CKBApplicationServer.user;

import BersaniChiappiniFraschini.CKBApplicationServer.config.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;
    @MockBean
    JwtService jwtService;

    @Test
    void shouldFindEducator() throws Exception {
        mockMvc.perform(
                get("/educator")
                        .param("name", "mar"))
                        .andExpect(status().isOk());
    }

    @Test
    void shouldFindStudent() throws Exception {
        mockMvc.perform(
                        get("/student")
                                .param("name", "mar"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFindGenericUser() throws Exception {
        mockMvc.perform(
                        get("/user")
                                .param("name", "mar"))
                .andExpect(status().isOk());
    }

}