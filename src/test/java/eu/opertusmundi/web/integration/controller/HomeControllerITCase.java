package eu.opertusmundi.web.integration.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("testing")
@AutoConfigureMockMvc
class HomeControllerITCase {

    @Autowired
    private MockMvc mockMvc;

    // Verifying HTTP Request Matching

    @Test
    @Tag(value = "Controller")
    @DisplayName(value = "When valid URL, method and content-type, return 200")
    void whenValidUrlAndMethodAndContentType_thenReturns200() throws Exception {
        this.mockMvc.perform(get("/")
            .contentType("text/html"))
            .andExpect(status().isOk()
        );
    }

}
