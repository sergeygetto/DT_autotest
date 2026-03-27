package com.dt.model.userrequesthistory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.dt.model.queryintersect.PageRequest;

/**
 * МОДЕЛЬ КЛАССА USERREQUESTHISTORYLISTREQUEST (ЗАПРОС СПИСКА ИСТОРИИ ЗАПРОСОВ ПОЛЬЗОВАТЕЛЯ)
 * 
 * ЗАЧЕМ НУЖЕН ЭТОТ КЛАСС:
 * - Представляет данные запроса для получения списка истории запросов пользователя
 * - REST Assured автоматически преобразует этот объект в JSON при отправке запроса
 * 
 * ПОЛЯ:
 * - pageNumber, pageSize - параметры пагинации (могут быть в pageRequest или напрямую)
 * - sort - параметры сортировки (sortColumn, sortDirection)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRequestHistoryListRequest {
    
    @JsonProperty("pageNumber")
    private Integer pageNumber;
    
    @JsonProperty("pageSize")
    private Integer pageSize;
    
    @JsonProperty("pageRequest")
    private PageRequest pageRequest;
    
    @JsonProperty("sort")
    private SortRequest sort;

    public UserRequestHistoryListRequest() {
    }

    public UserRequestHistoryListRequest(Integer pageNumber, Integer pageSize, SortRequest sort) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.sort = sort;
    }

    public UserRequestHistoryListRequest(PageRequest pageRequest, SortRequest sort) {
        this.pageRequest = pageRequest;
        this.sort = sort;
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

    public PageRequest getPageRequest() {
        return pageRequest;
    }

    public void setPageRequest(PageRequest pageRequest) {
        this.pageRequest = pageRequest;
    }

    public SortRequest getSort() {
        return sort;
    }

    public void setSort(SortRequest sort) {
        this.sort = sort;
    }
}
