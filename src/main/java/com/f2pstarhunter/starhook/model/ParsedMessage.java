package com.f2pstarhunter.starhook.model;

public record ParsedMessage(
        MessageType type,
        Integer tier,
        Integer world,
        String location
) {

}