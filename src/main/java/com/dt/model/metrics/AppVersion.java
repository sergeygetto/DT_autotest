package com.dt.model.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Модель версии приложения для метрик
 */
public class AppVersion {
    
    @JsonProperty("component")
    private String component;
    
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("date")
    private String date;
    
    public AppVersion() {
    }
    
    public AppVersion(String component, String version, String date) {
        this.component = component;
        this.version = version;
        this.date = date;
    }
    
    public String getComponent() {
        return component;
    }
    
    public void setComponent(String component) {
        this.component = component;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
}
