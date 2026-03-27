package com.dt.model.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Модель стадии для метрик
 * Используется в stagesSummary и stagesAlert
 */
public class Stage {
    
    @JsonProperty("stageNumber")
    private Integer stageNumber;
    
    @JsonProperty("stageName")
    private String stageName;
    
    @JsonProperty("fps")
    private Fps fps;
    
    @JsonProperty("timing")
    private Timing timing;
    
    @JsonProperty("cameraPosition")
    private CameraPosition cameraPosition;
    
    @JsonProperty("downloadSpeed")
    private DownloadSpeed downloadSpeed;
    
    @JsonProperty("metrics")
    private List<Metric> metrics;
    
    @JsonProperty("requests")
    private List<Request> requests;
    
    public Stage() {
    }
    
    // Getters and Setters
    public Integer getStageNumber() {
        return stageNumber;
    }
    
    public void setStageNumber(Integer stageNumber) {
        this.stageNumber = stageNumber;
    }
    
    public String getStageName() {
        return stageName;
    }
    
    public void setStageName(String stageName) {
        this.stageName = stageName;
    }
    
    public Fps getFps() {
        return fps;
    }
    
    public void setFps(Fps fps) {
        this.fps = fps;
    }
    
    public Timing getTiming() {
        return timing;
    }
    
    public void setTiming(Timing timing) {
        this.timing = timing;
    }
    
    public CameraPosition getCameraPosition() {
        return cameraPosition;
    }
    
    public void setCameraPosition(CameraPosition cameraPosition) {
        this.cameraPosition = cameraPosition;
    }
    
    public DownloadSpeed getDownloadSpeed() {
        return downloadSpeed;
    }
    
    public void setDownloadSpeed(DownloadSpeed downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }
    
    public List<Metric> getMetrics() {
        return metrics;
    }
    
    public void setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
    }
    
    public List<Request> getRequests() {
        return requests;
    }
    
    public void setRequests(List<Request> requests) {
        this.requests = requests;
    }
}
