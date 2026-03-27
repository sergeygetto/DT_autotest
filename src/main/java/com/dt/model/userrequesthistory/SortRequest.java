package com.dt.model.userrequesthistory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * МОДЕЛЬ КЛАССА SORTREQUEST (ПАРАМЕТРЫ СОРТИРОВКИ)
 * 
 * ЗАЧЕМ НУЖЕН ЭТОТ КЛАСС:
 * - Представляет параметры сортировки для запроса истории запросов
 * 
 * ПОЛЯ:
 * - sortColumn - колонка для сортировки (например, "time", "id")
 * - sortDirection - направление сортировки ("ASC", "DESC")
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SortRequest {
    
    @JsonProperty("sortColumn")
    private String sortColumn;
    
    @JsonProperty("sortDirection")
    private String sortDirection;

    public SortRequest() {
    }

    public SortRequest(String sortColumn, String sortDirection) {
        this.sortColumn = sortColumn;
        this.sortDirection = sortDirection;
    }

    public String getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
}
