package com.dt.model.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Модель для запроса добавления stage к сессии
 * Используется в POST /metrics/session/stage
 */
public class SessionStageRequest {
    
    @JsonProperty("sessionId")
    private String sessionId;
    
    @JsonProperty("stageSummary")
    private Stage stageSummary;
    
    @JsonProperty("stageAlert")
    private Stage stageAlert;
    
    public SessionStageRequest() {
    }
    
    public SessionStageRequest(String sessionId, Stage stageSummary, Stage stageAlert) {
        this.sessionId = sessionId;
        this.stageSummary = stageSummary;
        this.stageAlert = stageAlert;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public Stage getStageSummary() {
        return stageSummary;
    }
    
    public void setStageSummary(Stage stageSummary) {
        this.stageSummary = stageSummary;
    }
    
    public Stage getStageAlert() {
        return stageAlert;
    }
    
    public void setStageAlert(Stage stageAlert) {
        this.stageAlert = stageAlert;
    }
}
