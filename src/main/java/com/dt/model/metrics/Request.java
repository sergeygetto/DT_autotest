package com.dt.model.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Модель запроса для стадии метрик
 */
public class Request {
    
    @JsonProperty("url")
    private String url;
    
    @JsonProperty("ext")
    private String ext;
    
    @JsonProperty("mb")
    private Double mb;
    
    @JsonProperty("time")
    private Double time;
    
    @JsonProperty("mbsec")
    private Double mbsec;
    
    public Request() {
    }
    
    // Getters and Setters
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getExt() {
        return ext;
    }
    
    public void setExt(String ext) {
        this.ext = ext;
    }
    
    public Double getMb() {
        return mb;
    }
    
    public void setMb(Double mb) {
        this.mb = mb;
    }
    
    public Double getTime() {
        return time;
    }
    
    public void setTime(Double time) {
        this.time = time;
    }
    
    public Double getMbsec() {
        return mbsec;
    }
    
    public void setMbsec(Double mbsec) {
        this.mbsec = mbsec;
    }
}
