package com.dt.model.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Модель для запроса настройки окружения сессии
 * Используется в POST /metrics/session/env
 */
public class SessionEnvRequest {
    
    @JsonProperty("sessionId")
    private String sessionId;
    
    @JsonProperty("layers")
    private List<String> layers;
    
    @JsonProperty("appVersions")
    private List<AppVersion> appVersions;
    
    public SessionEnvRequest() {
    }
    
    public SessionEnvRequest(String sessionId, List<String> layers, List<AppVersion> appVersions) {
        this.sessionId = sessionId;
        this.layers = layers;
        this.appVersions = appVersions;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
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
}
