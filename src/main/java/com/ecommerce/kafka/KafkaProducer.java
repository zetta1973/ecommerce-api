package com.ecommerce.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishOrderCreated(OrderCreatedEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("order.created", eventJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing event", e);
        }
    }

    // Método para enviar solo el ID (si lo necesitas)
    public void publishOrderCreated(Long orderId) {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId(orderId);
        event.setTimestamp(System.currentTimeMillis());
        publishOrderCreated(event);
    }
}
