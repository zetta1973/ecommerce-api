package com.ecommerce.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);
    private final ObjectMapper objectMapper;

    public KafkaConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order.created", groupId = "ecommerce-group", autoStartup = "false")
    public void handleOrderCreated(String eventJson) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(eventJson, OrderCreatedEvent.class);
            
            log.info("Pedido recibido: {}", event.getOrderId());
            log.info("Usuario: {}", event.getUserId());
            log.info("Total: {}", event.getTotal());
            log.info("Timestamp: {}", event.getTimestamp());
            
            // Simular actualización de inventario
            updateInventory(event);
            
            // Simular envío de notificación
            sendNotification(event);
            
        } catch (Exception e) {
            log.error("Error processing order event: {}", eventJson, e);
        }
    }

    private void updateInventory(OrderCreatedEvent event) {
        log.info("📦 Actualizando inventario para pedido: {}", event.getOrderId());
    }

    private void sendNotification(OrderCreatedEvent event) {
        log.info("📧 Enviando notificación para pedido: {}", event.getOrderId());
    }
}
