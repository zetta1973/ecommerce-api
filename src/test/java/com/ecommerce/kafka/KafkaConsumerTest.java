package com.ecommerce.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerTest {

    @Mock
    private ObjectMapper objectMapper;

    private KafkaConsumer kafkaConsumer;

    @BeforeEach
    void setUp() {
        kafkaConsumer = new KafkaConsumer(objectMapper);
    }

    @Test
    void shouldBeAnnotatedWithComponent() {
        assertThat(KafkaConsumer.class.isAnnotationPresent(org.springframework.stereotype.Component.class)).isTrue();
    }

    @Test
    void shouldHaveKafkaListenerAnnotation() throws Exception {
        java.lang.reflect.Method method = KafkaConsumer.class.getMethod("handleOrderCreated", String.class);
        assertThat(method.isAnnotationPresent(org.springframework.kafka.annotation.KafkaListener.class)).isTrue();
    }

    @Test
    void shouldHaveCorrectTopicConfiguration() throws Exception {
        java.lang.reflect.Method method = KafkaConsumer.class.getMethod("handleOrderCreated", String.class);
        org.springframework.kafka.annotation.KafkaListener annotation = method.getAnnotation(org.springframework.kafka.annotation.KafkaListener.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.topics()).contains("order.created");
        assertThat(annotation.groupId()).isEqualTo("ecommerce-group");
    }

    @Test
    void shouldHaveConstructorWithObjectMapper() {
        KafkaConsumer consumer = new KafkaConsumer(objectMapper);
        assertThat(consumer).isNotNull();
    }

    @Test
    void shouldHandleOrderCreatedEvent() throws Exception {
        String eventJson = "{\"orderId\":123,\"userId\":456,\"total\":99.99,\"timestamp\":1234567890,\"status\":\"PENDING\"}";
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId(123L);
        event.setUserId(456L);
        event.setTotal(99.99);
        event.setTimestamp(1234567890L);
        event.setStatus("PENDING");

        when(objectMapper.readValue(eventJson, OrderCreatedEvent.class)).thenReturn(event);

        kafkaConsumer.handleOrderCreated(eventJson);

        verify(objectMapper).readValue(eventJson, OrderCreatedEvent.class);
    }

    @Test
    void shouldHandleExceptionWhenProcessingFails() throws Exception {
        String eventJson = "invalid json";
        when(objectMapper.readValue(eventJson, OrderCreatedEvent.class)).thenThrow(new RuntimeException("Parse error"));

        kafkaConsumer.handleOrderCreated(eventJson);

        verify(objectMapper).readValue(eventJson, OrderCreatedEvent.class);
    }
}
