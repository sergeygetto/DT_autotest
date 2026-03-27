package com.dt.model.queryintersect;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

    /**
     * МОДЕЛЬ КЛАССА MAINLAYER (ОСНОВНОЙ СЛОЙ)
     * 
     * ЗАЧЕМ НУЖЕН ЭТОТ КЛАСС:
     * - Представляет данные об основном слое в запросе getObjectsByCustomerConfig
     * - Используется в GetObjectsByCustomerConfigRequest
     * 
     * ПОЛЯ:
     * - layerId - ID слоя (может быть строкой "", числом Integer, или null)
     * - properties - список свойств (массив строк)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class MainLayer {
        
        @JsonProperty("layerId")
        private Object layerId;
        
        @JsonProperty("properties")
        private List<String> properties;

        public MainLayer() {
        }

        public MainLayer(Object layerId, List<String> properties) {
            this.layerId = layerId;
            this.properties = properties;
        }

        public Object getLayerId() {
            return layerId;
        }

        public void setLayerId(Object layerId) {
            this.layerId = layerId;
        }

    public List<String> getProperties() {
        return properties;
    }

    public void setProperties(List<String> properties) {
        this.properties = properties;
    }
}
