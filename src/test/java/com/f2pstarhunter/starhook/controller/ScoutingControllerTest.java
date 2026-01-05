package com.f2pstarhunter.starhook.controller;

import com.f2pstarhunter.starhook.model.ScoutingLocation;
import com.f2pstarhunter.starhook.repository.ScoutingLocationRepository;
import com.f2pstarhunter.starhook.repository.ScoutingStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ScoutingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScoutingLocationRepository locationRepository;

    @Autowired
    private ScoutingStatusRepository statusRepository;

    @BeforeEach
    void setUp() {
        statusRepository.deleteAll();
        locationRepository.deleteAll();

        // Add test locations
        locationRepository.save(new ScoutingLocation("cg/rim", "CG / Rim hopping location", 2958, 3265, 0, 32));
        locationRepository.save(new ScoutingLocation("akm/apa", "AKM / APA hopping location", 3323, 3289, 0, 32));
    }

    @Test
    void testGetAllLocations() throws Exception {
        mockMvc.perform(get("/api/scouting/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("cg/rim")))
                .andExpect(jsonPath("$[0].status", is("not_scouted")))
                .andExpect(jsonPath("$[0].scoutedBy").doesNotExist())
                .andExpect(jsonPath("$[1].id", is("akm/apa")));
    }

    @Test
    void testGetLocationsAfterClaiming() throws Exception {
        String payload = "{\"extra\":{\"type\":\"test\",\"source\":\"TestUser\",\"message\":\"scouting cg/rim\"}}";

        // Claim a location via webhook
        mockMvc.perform(post("/webhooks/stars")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("payload_json", payload))
                .andExpect(status().isOk());

        // Check status
        mockMvc.perform(get("/api/scouting/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='cg/rim')].status", contains("being_scouted")))
                .andExpect(jsonPath("$[?(@.id=='cg/rim')].scoutedBy", contains("TestUser")));
    }

    @Test
    void testCompleteScoutingFlow() throws Exception {
        String claimPayload = "{\"extra\":{\"type\":\"test\",\"source\":\"TestUser\",\"message\":\"scouting cg/rim\"}}";
        String completePayload = "{\"extra\":{\"type\":\"test\",\"source\":\"TestUser\",\"message\":\"cg/rim clear\"}}";

        // Claim
        mockMvc.perform(post("/webhooks/stars")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("payload_json", claimPayload))
                .andExpect(status().isOk());

        // Complete
        mockMvc.perform(post("/webhooks/stars")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("payload_json", completePayload))
                .andExpect(status().isOk());

        // Check status
        mockMvc.perform(get("/api/scouting/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='cg/rim')].status", contains("scouted")))
                .andExpect(jsonPath("$[?(@.id=='cg/rim')].scoutedBy", contains("TestUser")));
    }

    @Test
    void testWaveEndResetsAll() throws Exception {
        String claimPayload = "{\"extra\":{\"type\":\"test\",\"source\":\"TestUser\",\"message\":\"scouting cg/rim\"}}";
        String waveEndPayload = "{\"extra\":{\"type\":\"test\",\"source\":\"TestUser\",\"message\":\"wave end\"}}";

        // Claim a location
        mockMvc.perform(post("/webhooks/stars")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("payload_json", claimPayload))
                .andExpect(status().isOk());

        // Wave end
        mockMvc.perform(post("/webhooks/stars")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("payload_json", waveEndPayload))
                .andExpect(status().isOk());

        // Check all reset
        mockMvc.perform(get("/api/scouting/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].status", everyItem(is("not_scouted"))))
                .andExpect(jsonPath("$[*].scoutedBy", everyItem(nullValue())));
    }
}