package com.f2pstarhunter.starhook.service;

import com.f2pstarhunter.starhook.exception.InvalidMessageException;
import com.f2pstarhunter.starhook.model.ParsedMessage;
import com.f2pstarhunter.starhook.model.ShootingStar;
import com.f2pstarhunter.starhook.model.WebhookPayload;
import com.f2pstarhunter.starhook.repository.ShootingStarRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class ShootingStarService {

    private static final Logger log = LoggerFactory.getLogger(ShootingStarService.class);
    private final ObjectMapper objectMapper;
    private final ShootingStarRepository repository;
    private final MessageParser messageParser;

    public ShootingStarService(ObjectMapper objectMapper, ShootingStarRepository repository, MessageParser messageParser) {
        this.objectMapper = objectMapper;
        this.repository = repository;
        this.messageParser = messageParser;
    }

    @Transactional
    public void processWebhook(Map<String, String> params) {
        try {
            WebhookPayload payload = extractPayload(params);
            ParsedMessage message = messageParser.parse(payload.message());

            switch (message.type()) {
                case SPOTTED -> handleStarSpotted(message, payload);
                case DEPLETED -> handleStarDepleted(message);
                case DISAPPEARED -> handleStarDisappeared(message);
            }
        } catch (InvalidMessageException e) {
            log.error("Invalid message format: {}", e.getMessage());
            throw new RuntimeException("Invalid message format: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            // Pass through validation errors
            log.error("Validation error: {}", e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to process shooting star webhook", e);
            throw new RuntimeException("Failed to process webhook", e);
        }
    }

    private void handleStarSpotted(ParsedMessage message, WebhookPayload payload) {
        Optional<ShootingStar> existing = repository.findByWorld(message.world());

        if (existing.isPresent()) {
            updateExistingStar(existing.get(), message, payload);
        } else {
            createNewStar(message, payload);
        }
    }

    private void updateExistingStar(ShootingStar star, ParsedMessage message, WebhookPayload payload) {
        log.info("Updating star on world {}: Tier {} -> {}", message.world(), star.getTier(), message.tier());
        star.update(message.tier(), message.location(), payload.type(), payload.source());
        repository.save(star);
    }

    private void createNewStar(ParsedMessage message, WebhookPayload payload) {
        if (message.location() == null || message.location().isEmpty()) {
            throw new IllegalArgumentException("Location is required for new star on world " + message.world());
        }

        log.info("Creating new star on world {}: Tier {}, Location {}", message.world(), message.tier(), message.location());
        ShootingStar star = new ShootingStar(payload.type(), payload.source(), message.tier(), message.world(), message.location());
        repository.save(star);
    }

    private void handleStarDepleted(ParsedMessage message) {
        ShootingStar star = repository.findByWorld(message.world())
                .orElseThrow(() -> new IllegalArgumentException("No star found on world " + message.world()));

        log.info("Star on world {} depleted to dust", message.world());
        repository.delete(star);
    }

    private void handleStarDisappeared(ParsedMessage message) {
        ShootingStar star = repository.findByWorld(message.world())
                .orElseThrow(() -> new IllegalArgumentException("No star found on world " + message.world()));

        log.info("Star on world {} poofed at tier {}", message.world(), star.getTier());
        repository.delete(star);
    }

    private WebhookPayload extractPayload(Map<String, String> params) throws Exception {
        String payloadJson = params.get("payload_json");
        if (payloadJson == null) {
            throw new IllegalArgumentException("Missing payload_json parameter");
        }

        JsonNode root = objectMapper.readTree(payloadJson);
        JsonNode extra = root.get("extra");
        if (extra == null) {
            throw new IllegalArgumentException("Missing 'extra' field in payload");
        }

        return new WebhookPayload(
                extra.get("type").asText(),
                extra.get("source").asText(),
                extra.get("message").asText()
        );
    }
}