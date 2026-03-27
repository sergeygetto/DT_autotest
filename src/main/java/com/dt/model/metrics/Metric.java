package com.dt.model.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Модель метрики для стадии
 */
public class Metric {
    
    @JsonProperty("metricType")
    private String metricType;
    
    @JsonProperty("total")
    private Integer total;
    
    @JsonProperty("glbPercent")
    private Double glbPercent;
    
    @JsonProperty("glbHybridPercent")
    private Double glbHybridPercent;
    
    @JsonProperty("vctrPercent")
    private Double vctrPercent;
    
    @JsonProperty("compositePercent")
    private Double compositePercent;
    
    @JsonProperty("terrainPercent")
    private Double terrainPercent;
    
    @JsonProperty("tiles3dPercent")
    private Double tiles3dPercent;
    
    @JsonProperty("subtreePercent")
    private Double subtreePercent;
    
    @JsonProperty("bimPercent")
    private Double bimPercent;
    
    @JsonProperty("billboardsPercent")
    private Double billboardsPercent;
    
    @JsonProperty("roadLabelsPercent")
    private Double roadLabelsPercent;
    
    @JsonProperty("otherPercent")
    private Double otherPercent;
    
    public Metric() {
    }
    
    // Getters and Setters
    public String getMetricType() {
        return metricType;
    }
    
    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }
    
    public Integer getTotal() {
        return total;
    }
    
    public void setTotal(Integer total) {
        this.total = total;
    }
    
    public Double getGlbPercent() {
        return glbPercent;
    }
    
    public void setGlbPercent(Double glbPercent) {
        this.glbPercent = glbPercent;
    }
    
    public Double getGlbHybridPercent() {
        return glbHybridPercent;
    }
    
    public void setGlbHybridPercent(Double glbHybridPercent) {
        this.glbHybridPercent = glbHybridPercent;
    }
    
    public Double getVctrPercent() {
        return vctrPercent;
    }
    
    public void setVctrPercent(Double vctrPercent) {
        this.vctrPercent = vctrPercent;
    }
    
    public Double getCompositePercent() {
        return compositePercent;
    }
    
    public void setCompositePercent(Double compositePercent) {
        this.compositePercent = compositePercent;
    }
    
    public Double getTerrainPercent() {
        return terrainPercent;
    }
    
    public void setTerrainPercent(Double terrainPercent) {
        this.terrainPercent = terrainPercent;
    }
    
    public Double getTiles3dPercent() {
        return tiles3dPercent;
    }
    
    public void setTiles3dPercent(Double tiles3dPercent) {
        this.tiles3dPercent = tiles3dPercent;
    }
    
    public Double getSubtreePercent() {
        return subtreePercent;
    }
    
    public void setSubtreePercent(Double subtreePercent) {
        this.subtreePercent = subtreePercent;
    }
    
    public Double getBimPercent() {
        return bimPercent;
    }
    
    public void setBimPercent(Double bimPercent) {
        this.bimPercent = bimPercent;
    }
    
    public Double getBillboardsPercent() {
        return billboardsPercent;
    }
    
    public void setBillboardsPercent(Double billboardsPercent) {
        this.billboardsPercent = billboardsPercent;
    }
    
    public Double getRoadLabelsPercent() {
        return roadLabelsPercent;
    }
    
    public void setRoadLabelsPercent(Double roadLabelsPercent) {
        this.roadLabelsPercent = roadLabelsPercent;
    }
    
    public Double getOtherPercent() {
        return otherPercent;
    }
    
    public void setOtherPercent(Double otherPercent) {
        this.otherPercent = otherPercent;
    }
}
