package com.dt.model.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Модель скорости загрузки для метрик
 */
public class DownloadSpeed {
    
    @JsonProperty("mbps")
    private Double mbps;
    
    public DownloadSpeed() {
    }
    
    public Double getMbps() {
        return mbps;
    }
    
    public void setMbps(Double mbps) {
        this.mbps = mbps;
    }
}
