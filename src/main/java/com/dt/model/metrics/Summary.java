package com.dt.model.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Модель сводки метрик
 */
public class Summary {
    
    @JsonProperty("startupPageLoadSec")
    private Double startupPageLoadSec;
    
    @JsonProperty("firstDataLoadSec")
    private Double firstDataLoadSec;
    
    @JsonProperty("layerTreeLoadSec")
    private Double layerTreeLoadSec;
    
    @JsonProperty("fpsAvg")
    private Double fpsAvg;
    
    @JsonProperty("fpsAboveNormPercent")
    private Double fpsAboveNormPercent;
    
    @JsonProperty("fpsNormPercent")
    private Double fpsNormPercent;
    
    @JsonProperty("fpsBelowNormPercent")
    private Double fpsBelowNormPercent;
    
    @JsonProperty("drawCallsTotal")
    private Integer drawCallsTotal;
    
    @JsonProperty("downloadSpeedAvgMbps")
    private Double downloadSpeedAvgMbps;
    
    @JsonProperty("requestsTotal")
    private Integer requestsTotal;
    
    @JsonProperty("dataVolumeTotalMb")
    private Double dataVolumeTotalMb;
    
    @JsonProperty("timing")
    private Timing timing;
    
    @JsonProperty("dataComposition")
    private List<DataComposition> dataComposition;
    
    public Summary() {
    }
    
    // Getters and Setters
    public Double getStartupPageLoadSec() {
        return startupPageLoadSec;
    }
    
    public void setStartupPageLoadSec(Double startupPageLoadSec) {
        this.startupPageLoadSec = startupPageLoadSec;
    }
    
    public Double getFirstDataLoadSec() {
        return firstDataLoadSec;
    }
    
    public void setFirstDataLoadSec(Double firstDataLoadSec) {
        this.firstDataLoadSec = firstDataLoadSec;
    }
    
    public Double getLayerTreeLoadSec() {
        return layerTreeLoadSec;
    }
    
    public void setLayerTreeLoadSec(Double layerTreeLoadSec) {
        this.layerTreeLoadSec = layerTreeLoadSec;
    }
    
    public Double getFpsAvg() {
        return fpsAvg;
    }
    
    public void setFpsAvg(Double fpsAvg) {
        this.fpsAvg = fpsAvg;
    }
    
    public Double getFpsAboveNormPercent() {
        return fpsAboveNormPercent;
    }
    
    public void setFpsAboveNormPercent(Double fpsAboveNormPercent) {
        this.fpsAboveNormPercent = fpsAboveNormPercent;
    }
    
    public Double getFpsNormPercent() {
        return fpsNormPercent;
    }
    
    public void setFpsNormPercent(Double fpsNormPercent) {
        this.fpsNormPercent = fpsNormPercent;
    }
    
    public Double getFpsBelowNormPercent() {
        return fpsBelowNormPercent;
    }
    
    public void setFpsBelowNormPercent(Double fpsBelowNormPercent) {
        this.fpsBelowNormPercent = fpsBelowNormPercent;
    }
    
    public Integer getDrawCallsTotal() {
        return drawCallsTotal;
    }
    
    public void setDrawCallsTotal(Integer drawCallsTotal) {
        this.drawCallsTotal = drawCallsTotal;
    }
    
    public Double getDownloadSpeedAvgMbps() {
        return downloadSpeedAvgMbps;
    }
    
    public void setDownloadSpeedAvgMbps(Double downloadSpeedAvgMbps) {
        this.downloadSpeedAvgMbps = downloadSpeedAvgMbps;
    }
    
    public Integer getRequestsTotal() {
        return requestsTotal;
    }
    
    public void setRequestsTotal(Integer requestsTotal) {
        this.requestsTotal = requestsTotal;
    }
    
    public Double getDataVolumeTotalMb() {
        return dataVolumeTotalMb;
    }
    
    public void setDataVolumeTotalMb(Double dataVolumeTotalMb) {
        this.dataVolumeTotalMb = dataVolumeTotalMb;
    }
    
    public Timing getTiming() {
        return timing;
    }
    
    public void setTiming(Timing timing) {
        this.timing = timing;
    }
    
    public List<DataComposition> getDataComposition() {
        return dataComposition;
    }
    
    public void setDataComposition(List<DataComposition> dataComposition) {
        this.dataComposition = dataComposition;
    }
}
