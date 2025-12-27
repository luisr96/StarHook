package com.f2pstarhunter.starhook.controller;

import com.f2pstarhunter.starhook.repository.ShootingStarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ShootingStarControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShootingStarRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/webhooks/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Webhook endpoint is running"));
    }

    @Test
    void testReceiveWebhook() throws Exception {
        String payload = "{\"extra\":{\"type\":\"TradeFor200k\",\"source\":\"FRIENDSCHAT\",\"message\":\"T6 W418 ice\"}}";

        mockMvc.perform(post("/webhooks/stars")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("payload_json", payload))
                .andExpect(status().isOk());

        assertEquals(1, repository.count());
        assertTrue(repository.findByWorld(418).isPresent());
    }

    @Test
    void testReceiveMultipleWebhooks() throws Exception {
        String payload1 = "{\"extra\":{\"type\":\"TradeFor200k\",\"source\":\"FRIENDSCHAT\",\"message\":\"T8 W469 vb\"}}";
        String payload2 = "{\"extra\":{\"type\":\"TradeFor200k\",\"source\":\"FRIENDSCHAT\",\"message\":\"T9 W555 lse\"}}";

        mockMvc.perform(post("/webhooks/stars")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("payload_json", payload1))
                .andExpect(status().isOk());

        mockMvc.perform(post("/webhooks/stars")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("payload_json", payload2))
                .andExpect(status().isOk());

        assertEquals(2, repository.count());
    }
}