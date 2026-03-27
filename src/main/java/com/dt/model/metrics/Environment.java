package com.dt.model.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Модель окружения для метрик
 */
public class Environment {
    
    @JsonProperty("os")
    private String os;
    
    @JsonProperty("ramGb")
    private Integer ramGb;
    
    @JsonProperty("cpu")
    private String cpu;
    
    @JsonProperty("gpu")
    private String gpu;
    
    @JsonProperty("browser")
    private String browser;
    
    @JsonProperty("browserVersion")
    private String browserVersion;
    
    @JsonProperty("displays")
    private List<Display> displays;
    
    @JsonProperty("viewer")
    private List<Display> viewer;
    
    public Environment() {
    }
    
    // Getters and Setters
    public String getOs() {
        return os;
    }
    
    public void setOs(String os) {
        this.os = os;
    }
    
    public Integer getRamGb() {
        return ramGb;
    }
    
    public void setRamGb(Integer ramGb) {
        this.ramGb = ramGb;
    }
    
    public String getCpu() {
        return cpu;
    }
    
    public void setCpu(String cpu) {
        this.cpu = cpu;
    }
    
    public String getGpu() {
        return gpu;
    }
    
    public void setGpu(String gpu) {
        this.gpu = gpu;
    }
    
    public String getBrowser() {
        return browser;
    }
    
    public void setBrowser(String browser) {
        this.browser = browser;
    }
    
    public String getBrowserVersion() {
        return browserVersion;
    }
    
    public void setBrowserVersion(String browserVersion) {
        this.browserVersion = browserVersion;
    }
    
    public List<Display> getDisplays() {
        return displays;
    }
    
    public void setDisplays(List<Display> displays) {
        this.displays = displays;
    }
    
    public List<Display> getViewer() {
        return viewer;
    }
    
    public void setViewer(List<Display> viewer) {
        this.viewer = viewer;
    }
}
