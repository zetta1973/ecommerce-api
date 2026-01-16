package com.ecommerce.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private KafkaProducer kafkaProducer;

    private OrderCreatedEvent event;

    @BeforeEach
    void setUp() {
        event = new OrderCreatedEvent();
        event.setOrderId(1L);
        event.setUserId(100L);
        event.setTotal(100.50);
        event.setTimestamp(System.currentTimeMillis());
        event.setStatus("PENDING");
    }

    @Test
    void shouldPublishOrderCreated() throws JsonProcessingException {
        String eventJson = "{\"orderId\":1,\"userId\":100,\"total\":100.5,\"status\":\"PENDING\"}";
        when(objectMapper.writeValueAsString(event)).thenReturn(eventJson);

        kafkaProducer.publishOrderCreated(event);

        verify(kafkaTemplate).send("order.created", eventJson);
    }

    @Test
    void shouldBeAnnotatedWithService() {
        assertThat(KafkaProducer.class.isAnnotationPresent(org.springframework.stereotype.Service.class)).isTrue();
    }

    @Test
    void shouldHaveConstructorWithKafkaTemplateAndObjectMapper() {
        KafkaProducer producer = new KafkaProducer(kafkaTemplate, objectMapper);
        assertThat(producer).isNotNull();
    }

    @Test
    void shouldHandleJsonProcessingException() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(event)).thenThrow(new JsonProcessingException("Error"){});

        try {
            kafkaProducer.publishOrderCreated(event);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Error serializing event");
        }

        verify(kafkaTemplate, never()).send(any(), any());
    }

    @Test
    void shouldPublishEventWithCorrectTopic() throws JsonProcessingException {
        String eventJson = "{\"orderId\":1}";
        when(objectMapper.writeValueAsString(event)).thenReturn(eventJson);

        kafkaProducer.publishOrderCreated(event);

        verify(kafkaTemplate).send(eq("order.created"), eq(eventJson));
    }

    @Test
    void shouldHandleNullEvent() throws Exception {
        when(objectMapper.writeValueAsString(null)).thenReturn("null");

        kafkaProducer.publishOrderCreated((OrderCreatedEvent) null);

        verify(kafkaTemplate).send("order.created", "null");
    }
}
