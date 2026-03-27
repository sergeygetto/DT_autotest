package com.dt.model.queryintersect;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * МОДЕЛЬ КЛАССА LAYER (СЛОЙ ДЛЯ getObjectsByPolygon)
 * 
 * ЗАЧЕМ НУЖЕН ЭТОТ КЛАСС:
 * - Представляет данные слоя в запросе getObjectsByPolygon
 * - Используется в GetObjectsByPolygonRequest
 * 
 * ПОЛЯ:
 * - layerId - ID слоя (число)
 * - synced - синхронизирован ли слой (boolean)
 * - properties - список свойств (массив строк)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Layer {
    
    @JsonProperty("layerId")
    private Integer layerId;
    
    @JsonProperty("synced")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean synced;
    
    @JsonProperty("properties")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> properties;
    
    @JsonProperty("lowerOverlapPercent")
    private Integer lowerOverlapPercent;
    
    @JsonProperty("upperOverlapPercent")
    private Integer upperOverlapPercent;

    public Layer() {
    }

    public Layer(Integer layerId, Boolean synced, List<String> properties) {
        this.layerId = layerId;
        this.synced = synced;
        this.properties = properties;
    }

    public Layer(Integer layerId, List<String> properties, Integer lowerOverlapPercent, Integer upperOverlapPercent) {
        this.layerId = layerId;
        this.properties = properties;
        this.lowerOverlapPercent = lowerOverlapPercent;
        this.upperOverlapPercent = upperOverlapPercent;
    }

    public Integer getLayerId() {
        return layerId;
    }

    public void setLayerId(Integer layerId) {
        this.layerId = layerId;
    }

    public Boolean getSynced() {
        return synced;
    }

    public void setSynced(Boolean synced) {
        this.synced = synced;
    }

    public List<String> getProperties() {
        return properties;
    }

    public void setProperties(List<String> properties) {
        this.properties = properties;
    }

    public Integer getLowerOverlapPercent() {
        return lowerOverlapPercent;
    }

    public void setLowerOverlapPercent(Integer lowerOverlapPercent) {
        this.lowerOverlapPercent = lowerOverlapPercent;
    }

    public Integer getUpperOverlapPercent() {
        return upperOverlapPercent;
    }

    public void setUpperOverlapPercent(Integer upperOverlapPercent) {
        this.upperOverlapPercent = upperOverlapPercent;
    }
}
