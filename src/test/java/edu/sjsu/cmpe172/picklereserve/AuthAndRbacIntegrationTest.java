package edu.sjsu.cmpe172.picklereserve;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest @AutoConfigureMockMvc
class AuthAndRbacIntegrationTest {
    @Autowired MockMvc mvc;
    @Test void playerLoginCreatesSessionAndWrongRoleGets403() throws Exception {
        var result=mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"player.demo\",\"password\":\"password\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("PLAYER")).andReturn();
        MockHttpSession session=(MockHttpSession)result.getRequest().getSession(false);
        mvc.perform(get("/api/provider/appointments").session(session)).andExpect(status().isForbidden());
    }
    @Test void unauthenticatedPlayerEndpointGets401() throws Exception { mvc.perform(get("/api/appointments/mine")).andExpect(status().isUnauthorized()); }
}
