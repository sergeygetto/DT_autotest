package com.dt.model.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Модель FPS для метрик
 */
public class Fps {
    
    @JsonProperty("avgFps")
    private Double avgFps;
    
    @JsonProperty("belowNormPercent")
    private Double belowNormPercent;
    
    @JsonProperty("normPercent")
    private Double normPercent;
    
    @JsonProperty("aboveNormPercent")
    private Double aboveNormPercent;
    
    public Fps() {
    }
    
    public Double getAvgFps() {
        return avgFps;
    }
    
    public void setAvgFps(Double avgFps) {
        this.avgFps = avgFps;
    }
    
    public Double getBelowNormPercent() {
        return belowNormPercent;
    }
    
    public void setBelowNormPercent(Double belowNormPercent) {
        this.belowNormPercent = belowNormPercent;
    }
    
    public Double getNormPercent() {
        return normPercent;
    }
    
    public void setNormPercent(Double normPercent) {
        this.normPercent = normPercent;
    }
    
    public Double getAboveNormPercent() {
        return aboveNormPercent;
    }
    
    public void setAboveNormPercent(Double aboveNormPercent) {
        this.aboveNormPercent = aboveNormPercent;
    }
}
