package com.dt.model.widget;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * МОДЕЛЬ ДАННЫХ ВИДЖЕТА (элемент массива jsonData)
 * 
 * ЗАЧЕМ НУЖЕН:
 * - Представляет один элемент массива jsonData в виджете
 * - Содержит данные о статусе (value, status, цвета)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WidgetData {
    
    @JsonProperty("value")
    private String value;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("fontColor")
    private String fontColor;
    
    @JsonProperty("backgroundColor")
    private String backgroundColor;

    public WidgetData() {
    }

    public WidgetData(String value, String status, String fontColor, String backgroundColor) {
        this.value = value;
        this.status = status;
        this.fontColor = fontColor;
        this.backgroundColor = backgroundColor;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}
