package com.ecommerce.kafka;

import lombok.Data;

@Data
public class OrderCreatedEvent {
    private Long orderId;
    private Long userId;
    private Double total;
    private Long timestamp;
    private String status = "PENDING";

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
