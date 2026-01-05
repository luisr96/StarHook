package com.f2pstarhunter.starhook.service;

import com.f2pstarhunter.starhook.exception.InvalidMessageException;
import com.f2pstarhunter.starhook.model.MessageType;
import com.f2pstarhunter.starhook.model.ParsedMessage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class MessageParserScoutingTest {

    private final MessageParser parser = new MessageParser();

    @Test
    void testScoutingClaim() throws Exception {
        ParsedMessage result = parser.parse("scouting cg/rim");

        assertEquals(MessageType.SCOUTING_CLAIMED, result.type());
        assertEquals("cg/rim", result.scoutingLocationId());
        assertNull(result.tier());
        assertNull(result.world());
    }

    @Test
    void testScoutingClaimReversedOrder() throws Exception {
        ParsedMessage result = parser.parse("scouting rim/cg");

        assertEquals(MessageType.SCOUTING_CLAIMED, result.type());
        assertEquals("cg/rim", result.scoutingLocationId());
    }

    @Test
    void testScoutingClaimDifferentLocation() throws Exception {
        ParsedMessage result = parser.parse("scouting akm/apa");

        assertEquals(MessageType.SCOUTING_CLAIMED, result.type());
        assertEquals("akm/apa", result.scoutingLocationId());
    }

    @Test
    void testScoutingClaimSingleLocation() throws Exception {
        ParsedMessage result = parser.parse("scouting vb");

        assertEquals(MessageType.SCOUTING_CLAIMED, result.type());
        assertEquals("vb", result.scoutingLocationId());
    }

    @Test
    void testScoutingClaimCaseInsensitive() throws Exception {
        ParsedMessage result = parser.parse("SCOUTING CG/RIM");

        assertEquals(MessageType.SCOUTING_CLAIMED, result.type());
        assertEquals("cg/rim", result.scoutingLocationId());
    }

    @Test
    void testScoutingClaimWithAlias() throws Exception {
        ParsedMessage result = parser.parse("scouting fally");

        assertEquals(MessageType.SCOUTING_CLAIMED, result.type());
        assertEquals("fb", result.scoutingLocationId());
    }

    @Test
    void testScoutingComplete() throws Exception {
        ParsedMessage result = parser.parse("cg/rim clear");

        assertEquals(MessageType.SCOUTING_COMPLETED, result.type());
        assertEquals("cg/rim", result.scoutingLocationId());
    }

    @Test
    void testScoutingCompleteReversedOrder() throws Exception {
        ParsedMessage result = parser.parse("rim/cg clear");

        assertEquals(MessageType.SCOUTING_COMPLETED, result.type());
        assertEquals("cg/rim", result.scoutingLocationId());
    }

    @Test
    void testScoutingCompleteSingleLocation() throws Exception {
        ParsedMessage result = parser.parse("vb clear");

        assertEquals(MessageType.SCOUTING_COMPLETED, result.type());
        assertEquals("vb", result.scoutingLocationId());
    }

    @Test
    void testScoutingCompleteCaseInsensitive() throws Exception {
        ParsedMessage result = parser.parse("AKM/APA CLEAR");

        assertEquals(MessageType.SCOUTING_COMPLETED, result.type());
        assertEquals("akm/apa", result.scoutingLocationId());
    }

    @Test
    void testWaveEnd() throws Exception {
        ParsedMessage result = parser.parse("wave end");

        assertEquals(MessageType.WAVE_END, result.type());
        assertNull(result.scoutingLocationId());
    }

    @Test
    void testWaveEndCaseInsensitive() throws Exception {
        ParsedMessage result = parser.parse("WAVE END");

        assertEquals(MessageType.WAVE_END, result.type());
    }

    @Test
    void testInvalidScoutingLocation() {
        Exception exception = assertThrows(InvalidMessageException.class, () -> {
            parser.parse("scouting invalidlocation");
        });

        assertTrue(exception.getMessage().contains("Unknown scouting location"));
    }

    @Test
    void testInvalidClearLocation() {
        Exception exception = assertThrows(InvalidMessageException.class, () -> {
            parser.parse("invalidlocation clear");
        });

        assertTrue(exception.getMessage().contains("Unknown scouting location"));
    }

    @Test
    void testClearOnlyMatchesAtEnd() throws Exception {
        ParsedMessage result = parser.parse("clear skies passengers, and also t5 w308 ice");

        assertEquals(MessageType.SPOTTED, result.type());
        assertEquals(5, result.tier());
        assertEquals(308, result.world());
        assertEquals("ice", result.location());
    }
}