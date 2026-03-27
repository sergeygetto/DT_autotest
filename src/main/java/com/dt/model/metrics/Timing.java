package com.dt.model.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Модель таймингов для метрик
 */
public class Timing {
    
    @JsonProperty("totalSeconds")
    private Double totalSeconds;
    
    @JsonProperty("queueingPercent")
    private Double queueingPercent;
    
    @JsonProperty("stalledPercent")
    private Double stalledPercent;
    
    @JsonProperty("waitingTtfbPercent")
    private Double waitingTtfbPercent;
    
    @JsonProperty("receivePercent")
    private Double receivePercent;
    
    public Timing() {
    }
    
    // Getters and Setters
    public Double getTotalSeconds() {
        return totalSeconds;
    }
    
    public void setTotalSeconds(Double totalSeconds) {
        this.totalSeconds = totalSeconds;
    }
    
    public Double getQueueingPercent() {
        return queueingPercent;
    }
    
    public void setQueueingPercent(Double queueingPercent) {
        this.queueingPercent = queueingPercent;
    }
    
    public Double getStalledPercent() {
        return stalledPercent;
    }
    
    public void setStalledPercent(Double stalledPercent) {
        this.stalledPercent = stalledPercent;
    }
    
    public Double getWaitingTtfbPercent() {
        return waitingTtfbPercent;
    }
    
    public void setWaitingTtfbPercent(Double waitingTtfbPercent) {
        this.waitingTtfbPercent = waitingTtfbPercent;
    }
    
    public Double getReceivePercent() {
        return receivePercent;
    }
    
    public void setReceivePercent(Double receivePercent) {
        this.receivePercent = receivePercent;
    }
}
