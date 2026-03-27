package com.dt.model.methodconfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * МОДЕЛЬ КЛАССА METHODCONFIGUPDATEREQUEST (ЗАПРОС ОБНОВЛЕНИЯ КОНФИГУРАЦИИ МЕТОДА)
 * 
 * ЗАЧЕМ НУЖЕН ЭТОТ КЛАСС:
 * - Представляет данные запроса для обновления конфигурации метода через PUT
 * - REST Assured автоматически преобразует этот объект в JSON при отправке запроса
 * 
 * ПОЛЯ:
 * - queryParams - параметры запроса (объект с полями, например radius)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MethodConfigUpdateRequest {
    
    @JsonProperty("queryParams")
    private Map<String, Object> queryParams;

    public MethodConfigUpdateRequest() {
    }

    public MethodConfigUpdateRequest(Map<String, Object> queryParams) {
        this.queryParams = queryParams;
    }

    public Map<String, Object> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map<String, Object> queryParams) {
        this.queryParams = queryParams;
    }
}
