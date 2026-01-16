package com.ecommerce.kafka;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderCreatedEventTest {

    @Test
    void shouldCreateEvent() {
        OrderCreatedEvent event = new OrderCreatedEvent();

        event.setOrderId(1L);
        event.setUserId(100L);
        event.setTotal(100.50);
        event.setTimestamp(System.currentTimeMillis());
        event.setStatus("PENDING");

        assertThat(event.getOrderId()).isEqualTo(1L);
        assertThat(event.getUserId()).isEqualTo(100L);
        assertThat(event.getTotal()).isEqualTo(100.50);
        assertThat(event.getStatus()).isEqualTo("PENDING");
        assertThat(event.getTimestamp()).isPositive();
    }

    @Test
    void shouldCreateEventWithDefaults() {
        OrderCreatedEvent event = new OrderCreatedEvent();

        assertThat(event.getOrderId()).isNull();
        assertThat(event.getUserId()).isNull();
        assertThat(event.getTotal()).isNull();
        assertThat(event.getStatus()).isEqualTo("PENDING");
        assertThat(event.getTimestamp()).isNull();
    }
}
