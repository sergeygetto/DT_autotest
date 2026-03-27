package com.dt.model.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Модель для запроса добавления summary к сессии
 * Используется в POST /metrics/session/summary
 */
public class SessionSummaryRequest {
    
    @JsonProperty("sessionId")
    private String sessionId;
    
    @JsonProperty("summary")
    private Summary summary;
    
    @JsonProperty("summaryAlert")
    private Summary summaryAlert;
    
    public SessionSummaryRequest() {
    }
    
    public SessionSummaryRequest(String sessionId, Summary summary, Summary summaryAlert) {
        this.sessionId = sessionId;
        this.summary = summary;
        this.summaryAlert = summaryAlert;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public Summary getSummary() {
        return summary;
    }
    
    public void setSummary(Summary summary) {
        this.summary = summary;
    }
    
    public Summary getSummaryAlert() {
        return summaryAlert;
    }
    
    public void setSummaryAlert(Summary summaryAlert) {
        this.summaryAlert = summaryAlert;
    }
}
