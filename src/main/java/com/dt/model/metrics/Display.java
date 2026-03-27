package com.dt.model.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Модель дисплея для метрик
 */
public class Display {
    
    @JsonProperty("widthPx")
    private Integer widthPx;
    
    @JsonProperty("heightPx")
    private Integer heightPx;
    
    public Display() {
    }
    
    public Display(Integer widthPx, Integer heightPx) {
        this.widthPx = widthPx;
        this.heightPx = heightPx;
    }
    
    public Integer getWidthPx() {
        return widthPx;
    }
    
    public void setWidthPx(Integer widthPx) {
        this.widthPx = widthPx;
    }
    
    public Integer getHeightPx() {
        return heightPx;
    }
    
    public void setHeightPx(Integer heightPx) {
        this.heightPx = heightPx;
    }
}
