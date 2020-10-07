package eu.opertusmundi.web.integration.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("testing")
@AutoConfigureMockMvc
class HomeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    // Verifying HTTP Request Matching

    @Test
    void whenValidUrlAndMethodAndContentType_thenReturns200() throws Exception {
        this.mockMvc.perform(get("/")
            .contentType("text/html"))
            .andExpect(status().isOk()
        );
    }

}
