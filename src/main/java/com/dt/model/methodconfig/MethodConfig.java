package com.dt.model.methodconfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * МОДЕЛЬ КЛАССА METHODCONFIG (КОНФИГУРАЦИЯ МЕТОДА)
 * 
 * ЗАЧЕМ НУЖЕН ЭТОТ КЛАСС:
 * - Представляет данные о конфигурации метода в виде Java объекта
 * - REST Assured автоматически преобразует этот объект в JSON при отправке запроса
 * - REST Assured автоматически преобразует JSON ответа в этот объект
 * 
 * ПОЛЯ:
 * - customerCode - код клиента (обязательное)
 * - methodType - тип метода (обязательное)
 * - name - название конфигурации (обязательное)
 * - queryParams - параметры запроса (объект с полями, например radius)
 * - id - идентификатор (приходит в ответе)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MethodConfig {
    
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("customerCode")
    private String customerCode;
    
    @JsonProperty("methodType")
    private String methodType;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("queryParams")
    private Map<String, Object> queryParams;

    public MethodConfig() {
    }

    public MethodConfig(String customerCode, String methodType, String name, Map<String, Object> queryParams) {
        this.customerCode = customerCode;
        this.methodType = methodType;
        this.name = name;
        this.queryParams = queryParams;
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

    public String getMethodType() {
        return methodType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map<String, Object> queryParams) {
        this.queryParams = queryParams;
    }
}
