package com.f2pstarhunter.starhook.service;

import com.f2pstarhunter.starhook.exception.InvalidMessageException;
import com.f2pstarhunter.starhook.model.MessageType;
import com.f2pstarhunter.starhook.model.ParsedMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class MessageParserStarsTest {

    private final MessageParser parser = new MessageParser();

    @ParameterizedTest
    @ValueSource(strings = {
            "T5 W308 akb",
            "T5 w308 akb",
            "T5 308 akb",
            "W308 T5 akb",
            "W308 t5 akb",
            "308 T5 akb",
            "308 t5 akb",
    })
    void testMultipleValidFormats(String message) throws Exception {
        ParsedMessage result = parser.parse(message);

        assertEquals(MessageType.SPOTTED, result.type());
        assertEquals(5, result.tier());
        assertEquals(308, result.world());
        assertEquals("akb", result.location());
    }

    @Test
    void testParseWithoutLocation() throws Exception {
        ParsedMessage result = parser.parse("T4 W301");

        assertEquals(MessageType.SPOTTED, result.type());
        assertEquals(4, result.tier());
        assertEquals(301, result.world());
        assertNull(result.location());
    }

    @Test
    void testParseCaseInsensitive() throws Exception {
        ParsedMessage result = parser.parse("t7 w372 akm");

        assertEquals(MessageType.SPOTTED, result.type());
        assertEquals(7, result.tier());
        assertEquals(372, result.world());
    }

    @Test
    void testParseWithExtraWhitespace() throws Exception {
        ParsedMessage result = parser.parse("  T5   W384   ice  ");

        assertEquals(MessageType.SPOTTED, result.type());
        assertEquals(5, result.tier());
        assertEquals(384, result.world());
        assertEquals("ice", result.location());
    }

    @Test
    void testParseDustEvent() throws Exception {
        ParsedMessage result = parser.parse("W418 dust");

        assertEquals(MessageType.DEPLETED, result.type());
        assertNull(result.tier());
        assertEquals(418, result.world());
        assertNull(result.location());
    }

    @Test
    void testParsePoofEvent() throws Exception {
        ParsedMessage result = parser.parse("419 poof");

        assertEquals(MessageType.DISAPPEARED, result.type());
        assertNull(result.tier());
        assertEquals(419, result.world());
        assertNull(result.location());
    }

    @Test
    void testInvalidTierTooLow() {
        Exception exception = assertThrows(InvalidMessageException.class, () -> {
            parser.parse("T0 W456 ccr");
        });

        assertTrue(exception.getMessage().contains("Tier must be 1-9"));
    }

    @Test
    void testInvalidTierTooHigh() {
        Exception exception = assertThrows(InvalidMessageException.class, () -> {
            parser.parse("T10 W456 dray");
        });

        assertTrue(exception.getMessage().contains("Tier must be 1-9"));
    }

    @Test
    void testInvalidWorldTooLow() {
        Exception exception = assertThrows(InvalidMessageException.class, () -> {
            parser.parse("T5 W300 vse");
        });

        assertTrue(exception.getMessage().contains("World must be 301-638"));
    }

    @Test
    void testInvalidWorldTooHigh() {
        Exception exception = assertThrows(InvalidMessageException.class, () -> {
            parser.parse("T7 W639 vsw");
        });

        assertTrue(exception.getMessage().contains("World must be 301-638"));
    }

    @Test
    void testInvalidFormat() {
        Exception exception = assertThrows(InvalidMessageException.class, () -> {
            parser.parse("invalid message");
        });

        assertTrue(exception.getMessage().contains("Invalid format"));
    }

    @Test
    void testLocationBeforeTier() throws Exception {
        ParsedMessage result = parser.parse("379 lse t5");

        assertEquals(MessageType.SPOTTED, result.type());
        assertEquals(379, result.world());
        assertEquals(5, result.tier());
        assertEquals("lse", result.location());
    }

    @Test
    void testLocationAfterTier() throws Exception {
        ParsedMessage result = parser.parse("379 t5 lse");

        assertEquals(MessageType.SPOTTED, result.type());
        assertEquals(379, result.world());
        assertEquals(5, result.tier());
        assertEquals("lse", result.location());
    }

    @Test
    void testTierBeforeWorld() throws Exception {
        ParsedMessage result = parser.parse("t6 335 vb");

        assertEquals(MessageType.SPOTTED, result.type());
        assertEquals(335, result.world());
        assertEquals(6, result.tier());
        assertEquals("vb", result.location());
    }

    @Test
    void testLocationFirst() throws Exception {
        ParsedMessage result = parser.parse("vsw 445 t6");

        assertEquals(MessageType.SPOTTED, result.type());
        assertEquals(445, result.world());
        assertEquals(6, result.tier());
        assertEquals("vsw", result.location());
    }

    @Test
    void testIgnoreTotalWorlds() throws Exception {
        ParsedMessage result = parser.parse("vsw 445 t6 (500ttl)");

        assertEquals(MessageType.SPOTTED, result.type());
        assertEquals(445, result.world());
        assertEquals(6, result.tier());
        assertEquals("vsw", result.location());
    }

    @Test
    void testLocationNormalizationAKB() throws Exception {
        ParsedMessage result1 = parser.parse("335 t6 akb");
        ParsedMessage result2 = parser.parse("335 t6 al kharid bank");
        ParsedMessage result3 = parser.parse("335 t6 Al Kharid east bank");

        assertEquals("akb", result1.location());
        assertEquals("akb", result2.location());
        assertEquals("akb", result3.location());
    }

    @Test
    void testLocationNormalizationVB() throws Exception {
        ParsedMessage result1 = parser.parse("335 t6 vb");
        ParsedMessage result2 = parser.parse("335 t6 varrock bank");
        ParsedMessage result3 = parser.parse("335 t6 Varrock east bank");

        assertEquals("vb", result1.location());
        assertEquals("vb", result2.location());
        assertEquals("vb", result3.location());
    }

    @Test
    void testLocationNormalizationCaseInsensitive() throws Exception {
        ParsedMessage result1 = parser.parse("419 t3 VSE");
        ParsedMessage result2 = parser.parse("419 t3 vse");
        ParsedMessage result3 = parser.parse("419 t3 VsE");

        assertEquals("vse", result1.location());
        assertEquals("vse", result2.location());
        assertEquals("vse", result3.location());
    }

    @Test
    void testUnknownLocationIgnored() throws Exception {
        ParsedMessage result = parser.parse("335 t6 v");

        assertEquals(MessageType.SPOTTED, result.type());
        assertEquals(335, result.world());
        assertEquals(6, result.tier());
        assertNull(result.location());
    }
}