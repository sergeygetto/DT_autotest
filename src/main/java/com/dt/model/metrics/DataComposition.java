package com.dt.model.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Модель композиции данных для метрик
 */
public class DataComposition {
    
    @JsonProperty("component")
    private String component;
    
    @JsonProperty("drawCallsPercent")
    private Double drawCallsPercent;
    
    @JsonProperty("queueingPercent")
    private Double queueingPercent;
    
    @JsonProperty("stalledPercent")
    private Double stalledPercent;
    
    @JsonProperty("waitingTtfbPercent")
    private Double waitingTtfbPercent;
    
    @JsonProperty("receivePercent")
    private Double receivePercent;
    
    @JsonProperty("requestsPercent")
    private Double requestsPercent;
    
    @JsonProperty("dataVolumePercent")
    private Double dataVolumePercent;
    
    public DataComposition() {
    }
    
    // Getters and Setters
    public String getComponent() {
        return component;
    }
    
    public void setComponent(String component) {
        this.component = component;
    }
    
    public Double getDrawCallsPercent() {
        return drawCallsPercent;
    }
    
    public void setDrawCallsPercent(Double drawCallsPercent) {
        this.drawCallsPercent = drawCallsPercent;
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
    
    public Double getRequestsPercent() {
        return requestsPercent;
    }
    
    public void setRequestsPercent(Double requestsPercent) {
        this.requestsPercent = requestsPercent;
    }
    
    public Double getDataVolumePercent() {
        return dataVolumePercent;
    }
    
    public void setDataVolumePercent(Double dataVolumePercent) {
        this.dataVolumePercent = dataVolumePercent;
    }
}
