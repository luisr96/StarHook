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
class MessageParserTest {

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
}