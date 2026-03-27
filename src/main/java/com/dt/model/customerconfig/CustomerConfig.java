package com.dt.model.customerconfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * МОДЕЛЬ КЛАССА CUSTOMERCONFIG (КОНФИГУРАЦИЯ КЛИЕНТА)
 * 
 * ЗАЧЕМ НУЖЕН ЭТОТ КЛАСС:
 * - Представляет данные о конфигурации клиента в виде Java объекта
 * - REST Assured автоматически преобразует этот объект в JSON при отправке запроса
 * - REST Assured автоматически преобразует JSON ответа в этот объект
 * 
 * ПОЛЯ:
 * - customerCode - код клиента (обязательное)
 * - layerId - ID слоя (обязательное)
 * - alias - псевдоним
 * - properties - список свойств (массив строк)
 * - treeLayerId - UUID слоя дерева
 * - order - порядок
 * - lowerOverlapPercent - процент нижнего перекрытия
 * - upperOverlapPercent - процент верхнего перекрытия
 * - id - идентификатор (приходит в ответе)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerConfig {
    
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("customerCode")
    private String customerCode;
    
    @JsonProperty("layerId")
    private Integer layerId;
    
    @JsonProperty("alias")
    private String alias;
    
    @JsonProperty("properties")
    private List<String> properties;
    
    @JsonProperty("treeLayerId")
    private String treeLayerId;
    
    @JsonProperty("order")
    private Integer order;
    
    @JsonProperty("lowerOverlapPercent")
    private Integer lowerOverlapPercent;
    
    @JsonProperty("upperOverlapPercent")
    private Integer upperOverlapPercent;

    public CustomerConfig() {
    }

    public CustomerConfig(String customerCode, Integer layerId, String alias, List<String> properties, 
                         String treeLayerId, Integer order, Integer lowerOverlapPercent, Integer upperOverlapPercent) {
        this.customerCode = customerCode;
        this.layerId = layerId;
        this.alias = alias;
        this.properties = properties;
        this.treeLayerId = treeLayerId;
        this.order = order;
        this.lowerOverlapPercent = lowerOverlapPercent;
        this.upperOverlapPercent = upperOverlapPercent;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public Integer getLayerId() {
        return layerId;
    }

    public void setLayerId(Integer layerId) {
        this.layerId = layerId;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public List<String> getProperties() {
        return properties;
    }

    public void setProperties(List<String> properties) {
        this.properties = properties;
    }

    public String getTreeLayerId() {
        return treeLayerId;
    }

    public void setTreeLayerId(String treeLayerId) {
        this.treeLayerId = treeLayerId;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
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
