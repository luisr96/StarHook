package com.f2pstarhunter.starhook.model;

public record WebhookPayload(
        String type,
        String source,
        String message
) {
}