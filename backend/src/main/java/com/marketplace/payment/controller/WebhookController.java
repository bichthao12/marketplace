package com.marketplace.payment.controller;

import com.marketplace.common.config.PaymentProperties.StripeSettings;
import com.marketplace.payment.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);
    private static final String PROCESSED_KEY_PREFIX = "stripe:event:";

    private final PaymentService paymentService;
    private final StripeSettings stripeSettings;
    private final StringRedisTemplate redisTemplate;

    public WebhookController(
            PaymentService paymentService,
            StripeSettings stripeSettings,
            StringRedisTemplate redisTemplate
    ) {
        this.paymentService = paymentService;
        this.stripeSettings = stripeSettings;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/stripe")
    public ResponseEntity<Map<String, Boolean>> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String signature
    ) {
        Event event;
        try {
            if (stripeSettings.isConfigured() && signature != null) {
                event = Webhook.constructEvent(payload, signature, stripeSettings.webhookSecret());
            } else {
                event = Event.GSON.fromJson(payload, Event.class);
            }
        } catch (SignatureVerificationException e) {
            log.warn("Invalid Stripe signature");
            return ResponseEntity.badRequest().body(Map.of("received", false));
        }

        String idempotencyKey = PROCESSED_KEY_PREFIX + event.getId();
        Boolean alreadyProcessed = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "1");
        if (alreadyProcessed != null && !alreadyProcessed) {
            return ResponseEntity.ok(Map.of("received", true));
        }
        redisTemplate.expire(idempotencyKey, java.time.Duration.ofDays(7));

        switch (event.getType()) {
            case "payment_intent.succeeded" -> {
                PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (intent != null) {
                    paymentService.markPaid(intent.getId());
                }
            }
            case "payment_intent.payment_failed" -> {
                PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (intent != null) {
                    paymentService.markFailed(intent.getId());
                }
            }
            default -> log.debug("Unhandled Stripe event type: {}", event.getType());
        }

        return ResponseEntity.ok(Map.of("received", true));
    }
}
