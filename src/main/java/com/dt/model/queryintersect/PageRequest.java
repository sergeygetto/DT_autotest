package com.dt.model.queryintersect;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * МОДЕЛЬ КЛАССА PAGEREQUEST (ЗАПРОС ПАГИНАЦИИ)
 * 
 * ЗАЧЕМ НУЖЕН ЭТОТ КЛАСС:
 * - Представляет данные пагинации в запросе getObjectsByCustomerConfig
 * - Используется в GetObjectsByCustomerConfigRequest
 * 
 * ПОЛЯ:
 * - pageNumber - номер страницы (начинается с 0)
 * - pageSize - размер страницы (количество элементов на странице)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageRequest {
    
    @JsonProperty("pageNumber")
    private Integer pageNumber;
    
    @JsonProperty("pageSize")
    private Integer pageSize;

    public PageRequest() {
    }

    public PageRequest(Integer pageNumber, Integer pageSize) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
