package com.dt.model.widget;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * МОДЕЛЬ КЛАССА WIDGET (ВИДЖЕТ)
 * 
 * ЗАЧЕМ НУЖЕН ЭТОТ КЛАСС:
 * - Представляет данные о виджете в виде Java объекта
 * - REST Assured автоматически преобразует этот объект в JSON при отправке запроса
 * - REST Assured автоматически преобразует JSON ответа в этот объект
 * 
 * ПОЛЯ:
 * - customerCode - код клиента (обязательное)
 * - type - тип виджета
 * - widgetCode - код виджета (уникальный)
 * - name - название виджета (обязательное)
 * - jsonData - массив данных виджета
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Widget {
    
    @JsonProperty("customerCode")
    private String customerCode;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("widgetCode")
    private String widgetCode;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("jsonData")
    private List<WidgetData> jsonData;
    
    @JsonProperty("id")
    private Integer id;

    public Widget() {
    }

    public Widget(String customerCode, String type, String widgetCode, String name, List<WidgetData> jsonData) {
        this.customerCode = customerCode;
        this.type = type;
        this.widgetCode = widgetCode;
        this.name = name;
        this.jsonData = jsonData;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWidgetCode() {
        return widgetCode;
    }

    public void setWidgetCode(String widgetCode) {
        this.widgetCode = widgetCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<WidgetData> getJsonData() {
        return jsonData;
    }

    public void setJsonData(List<WidgetData> jsonData) {
        this.jsonData = jsonData;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
