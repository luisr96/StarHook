package com.f2pstarhunter.starhook.service;

import com.f2pstarhunter.starhook.exception.InvalidMessageException;
import com.f2pstarhunter.starhook.model.MessageType;
import com.f2pstarhunter.starhook.model.ParsedMessage;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MessageParser {

    private static final Pattern TIER_FIRST = Pattern.compile("^T(\\d+)\\s+w?(\\d+)(?:\\s+(.+))?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern WORLD_FIRST = Pattern.compile("^w?(\\d+)\\s+T(\\d+)(?:\\s+(.+))?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DUST = Pattern.compile("^w?(\\d+)\\s+dust$", Pattern.CASE_INSENSITIVE);
    private static final Pattern POOF = Pattern.compile("^w?(\\d+)\\s+poof$", Pattern.CASE_INSENSITIVE);

    public ParsedMessage parse(String message) throws InvalidMessageException {
        String trimmed = message.trim();

        // Check for dust/poof first
        Matcher m = DUST.matcher(trimmed);
        if (m.matches()) {
            return new ParsedMessage(MessageType.DEPLETED, null, parseWorld(m.group(1)), null);
        }

        m = POOF.matcher(trimmed);
        if (m.matches()) {
            return new ParsedMessage(MessageType.DISAPPEARED, null, parseWorld(m.group(1)), null);
        }

        // Check for tier-first message
        m = TIER_FIRST.matcher(trimmed);
        if (m.matches()) {
            int tier = parseTier(m.group(1));
            int world = parseWorld(m.group(2));
            String location = m.group(3) != null ? m.group(3).trim() : null;
            return new ParsedMessage(MessageType.SPOTTED, tier, world, location);
        }

        // Check for world-first message
        m = WORLD_FIRST.matcher(trimmed);
        if (m.matches()) {
            int world = parseWorld(m.group(1));
            int tier = parseTier(m.group(2));
            String location = m.group(3) != null ? m.group(3).trim() : null;
            return new ParsedMessage(MessageType.SPOTTED, tier, world, location);
        }

        throw new InvalidMessageException("Invalid format. Expected: 'T{tier} w{world} [location]', 'w{world} dust', or 'w{world} poof'");
    }

    private int parseTier(String tierStr) throws InvalidMessageException {
        int tier = Integer.parseInt(tierStr);
        if (tier < 1 || tier > 9) {
            throw new InvalidMessageException("Tier must be 1-9, got: " + tier);
        }
        return tier;
    }

    private int parseWorld(String worldStr) throws InvalidMessageException {
        int world = Integer.parseInt(worldStr);
        if (world < 301 || world > 638) {
            throw new InvalidMessageException("World must be 301-638, got: " + world);
        }
        return world;
    }
}