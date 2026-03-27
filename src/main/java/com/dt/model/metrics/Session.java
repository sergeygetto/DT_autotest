package com.dt.model.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Модель сессии для метрик
 */
public class Session {
    
    @JsonProperty("sessionId")
    private String sessionId;
    
    @JsonProperty("userName")
    private String userName;
    
    @JsonProperty("presetName")
    private String presetName;
    
    @JsonProperty("ended")
    private Boolean ended;
    
    @JsonProperty("startedAt")
    private String startedAt;
    
    @JsonProperty("environment")
    private Environment environment;
    
    @JsonProperty("layers")
    private List<String> layers;
    
    @JsonProperty("appVersions")
    private List<AppVersion> appVersions;
    
    @JsonProperty("summary")
    private Summary summary;
    
    @JsonProperty("summaryAlert")
    private Summary summaryAlert;
    
    @JsonProperty("stagesSummary")
    private List<Stage> stagesSummary;
    
    @JsonProperty("stagesAlert")
    private List<Stage> stagesAlert;
    
    public Session() {
    }
    
    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getPresetName() {
        return presetName;
    }
    
    public void setPresetName(String presetName) {
        this.presetName = presetName;
    }
    
    public Boolean getEnded() {
        return ended;
    }
    
    public void setEnded(Boolean ended) {
        this.ended = ended;
    }
    
    public String getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }
    
    public Environment getEnvironment() {
        return environment;
    }
    
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
    
    public List<String> getLayers() {
        return layers;
    }
    
    public void setLayers(List<String> layers) {
        this.layers = layers;
    }
    
    public List<AppVersion> getAppVersions() {
        return appVersions;
    }
    
    public void setAppVersions(List<AppVersion> appVersions) {
        this.appVersions = appVersions;
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
    
    public List<Stage> getStagesSummary() {
        return stagesSummary;
    }
    
    public void setStagesSummary(List<Stage> stagesSummary) {
        this.stagesSummary = stagesSummary;
    }
    
    public List<Stage> getStagesAlert() {
        return stagesAlert;
    }
    
    public void setStagesAlert(List<Stage> stagesAlert) {
        this.stagesAlert = stagesAlert;
    }
}
