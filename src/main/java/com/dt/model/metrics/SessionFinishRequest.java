package com.dt.model.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Модель для запроса завершения сессии
 * Используется в POST /metrics/session/finish
 */
public class SessionFinishRequest {
    
    @JsonProperty("sessionId")
    private String sessionId;
    
    public SessionFinishRequest() {
    }
    
    public SessionFinishRequest(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
