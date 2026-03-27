package com.dt.model.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Модель для запроса отправки метрик
 * Используется в POST /metrics
 */
public class MetricsRequest {
    
    @JsonProperty("session")
    private Session session;
    
    public MetricsRequest() {
    }
    
    public MetricsRequest(Session session) {
        this.session = session;
    }
    
    public Session getSession() {
        return session;
    }
    
    public void setSession(Session session) {
        this.session = session;
    }
}
