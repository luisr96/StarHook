package com.f2pstarhunter.starhook.service;

import com.f2pstarhunter.starhook.exception.InvalidMessageException;
import com.f2pstarhunter.starhook.model.MessageType;
import com.f2pstarhunter.starhook.model.ParsedMessage;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MessageParser {

    private static final Map<String, String> LOCATION_ALIASES = new HashMap<>();
    private static final Map<String, String> SCOUTING_LOCATION_ALIASES = new HashMap<>();

    static {
        // Star locations
        addLocation("akb", "al kharid bank", "al kharid east bank");
        addLocation("akm", "al kharid mine");
        addLocation("apa", "al kharid duel arena");
        addLocation("ccb", "corsair cove bank");
        addLocation("ccr", "corsair resource area");
        addLocation("cg", "crafting guild");
        addLocation("dray", "draynor village", "draynor");
        addLocation("fb", "falador bank", "falador east bank");
        addLocation("ice", "ice mountain");
        addLocation("lse", "lumbridge southeast mine", "lumbridge southeast");
        addLocation("lsw", "lumbridge southwest mine", "lumbridge southwest");
        addLocation("nc", "north crandor");
        addLocation("sc", "south crandor");
        addLocation("vb", "varrock east bank", "varrock bank");
        addLocation("vse", "varrock southeast mine", "varrock southeast");
        addLocation("vsw", "varrock southwest mine", "varrock southwest");
        addLocation("rim", "rimmington mine", "rimmington");

        // Scouting locations
        addScoutingLocation("cg/rim", "cg/rim", "rim/cg", "cg", "rim");
        addScoutingLocation("akm/apa", "akm/apa", "apa/akm", "akm", "apa", "duel");
        addScoutingLocation("vb", "vb");
        addScoutingLocation("vse", "vse");
        addScoutingLocation("akb", "akb");
        addScoutingLocation("lse", "lse");
        addScoutingLocation("lsw", "lsw");
        addScoutingLocation("ccr/mg", "ccr/mg", "mg/ccr", "ccr");
        addScoutingLocation("sc", "sc");
        addScoutingLocation("nc", "nc");
        addScoutingLocation("ice", "ice");
        addScoutingLocation("fb", "fb", "fally");
        addScoutingLocation("ccb", "ccb");
        addScoutingLocation("dray", "dray");
        addScoutingLocation("uzer", "uzer");
    }

    private static void addLocation(String canonical, String... aliases) {
        LOCATION_ALIASES.put(canonical.toLowerCase(), canonical);
        for (String alias : aliases) {
            LOCATION_ALIASES.put(alias.toLowerCase(), canonical);
        }
    }

    private static void addScoutingLocation(String canonicalId, String... aliases) {
        for (String alias : aliases) {
            SCOUTING_LOCATION_ALIASES.put(alias.toLowerCase(), canonicalId);
        }
    }

    private static final Pattern DUST = Pattern.compile("^w?(\\d+)\\s+dust$", Pattern.CASE_INSENSITIVE);
    private static final Pattern POOF = Pattern.compile("^w?(\\d+)\\s+poof$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SCOUTING = Pattern.compile("^scouting\\s+(.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLEAR = Pattern.compile("^(.+)\\s+clear$", Pattern.CASE_INSENSITIVE);
    private static final Pattern WAVE_END = Pattern.compile("^wave\\s+end$", Pattern.CASE_INSENSITIVE);

    private static final Pattern WORLD_PATTERN = Pattern.compile("w?(\\d{3})\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern TIER_PATTERN = Pattern.compile("t(\\d+)\\b", Pattern.CASE_INSENSITIVE);

    public ParsedMessage parse(String message) throws InvalidMessageException {
        String trimmed = message.trim();

        // Check for wave end
        if (WAVE_END.matcher(trimmed).matches()) {
            return ParsedMessage.forScouting(MessageType.WAVE_END, null);
        }

        // Check for scouting claim
        Matcher scoutingMatcher = SCOUTING.matcher(trimmed);
        if (scoutingMatcher.matches()) {
            String locationStr = scoutingMatcher.group(1).trim();
            String locationId = findScoutingLocation(locationStr);
            if (locationId == null) {
                throw new InvalidMessageException("Unknown scouting location: " + locationStr);
            }
            return ParsedMessage.forScouting(MessageType.SCOUTING_CLAIMED, locationId);
        }

        // Check for scouting completion
        Matcher clearMatcher = CLEAR.matcher(trimmed);
        if (clearMatcher.matches()) {
            String locationStr = clearMatcher.group(1).trim();
            String locationId = findScoutingLocation(locationStr);
            if (locationId == null) {
                throw new InvalidMessageException("Unknown scouting location: " + locationStr);
            }
            return ParsedMessage.forScouting(MessageType.SCOUTING_COMPLETED, locationId);
        }

        // Check for dust/poof
        Matcher m = DUST.matcher(trimmed);
        if (m.matches()) {
            return new ParsedMessage(MessageType.DEPLETED, null, parseWorld(m.group(1)), null);
        }

        m = POOF.matcher(trimmed);
        if (m.matches()) {
            return new ParsedMessage(MessageType.DISAPPEARED, null, parseWorld(m.group(1)), null);
        }

        // Extract world, tier, and location
        Integer world = extractWorld(trimmed);
        Integer tier = extractTier(trimmed);
        String location = findLocation(trimmed);

        if (world == null || tier == null) {
            throw new InvalidMessageException("Invalid format. Expected: 'T{tier} w{world} [location]', 'w{world} dust', or 'w{world} poof'");
        }

        return new ParsedMessage(MessageType.SPOTTED, tier, world, location);
    }

    private String findScoutingLocation(String text) {
        String lowerText = text.toLowerCase();

        return SCOUTING_LOCATION_ALIASES.keySet().stream()
                .sorted((a, b) -> Integer.compare(b.length(), a.length()))
                .filter(alias -> {
                    String pattern = "\\b" + Pattern.quote(alias) + "\\b";
                    return lowerText.matches(".*" + pattern + ".*");
                })
                .map(SCOUTING_LOCATION_ALIASES::get)
                .findFirst()
                .orElse(null);
    }

    private Integer extractWorld(String text) throws InvalidMessageException {
        Matcher m = WORLD_PATTERN.matcher(text);
        if (m.find()) {
            return parseWorld(m.group(1));
        }
        return null;
    }

    private Integer extractTier(String text) throws InvalidMessageException {
        Matcher m = TIER_PATTERN.matcher(text);
        if (m.find()) {
            return parseTier(m.group(1));
        }
        return null;
    }

    private String findLocation(String text) {
        String lowerText = text.toLowerCase();

        return LOCATION_ALIASES.keySet().stream()
                .sorted((a, b) -> Integer.compare(b.length(), a.length()))
                .filter(alias -> {
                    String pattern = "\\b" + Pattern.quote(alias) + "\\b";
                    return lowerText.matches(".*" + pattern + ".*");
                })
                .map(LOCATION_ALIASES::get)
                .findFirst()
                .orElse(null);
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