package com.f2pstarhunter.starhook.model;

public record ParsedMessage(
        MessageType type,
        Integer tier,
        Integer world,
        String location,
        String scoutingLocationId
) {
    public ParsedMessage(MessageType type, Integer tier, Integer world, String location) {
        this(type, tier, world, location, null);
    }

    public static ParsedMessage forScouting(MessageType type, String scoutingLocationId) {
        return new ParsedMessage(type, null, null, null, scoutingLocationId);
    }
}