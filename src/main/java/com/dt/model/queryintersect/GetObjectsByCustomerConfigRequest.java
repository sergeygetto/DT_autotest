package com.dt.model.queryintersect;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * МОДЕЛЬ КЛАССА GETOBJECTSBYCUSTOMERCONFIGREQUEST (ЗАПРОС ПОЛУЧЕНИЯ ОБЪЕКТОВ)
 * 
 * ЗАЧЕМ НУЖЕН ЭТОТ КЛАСС:
 * - Представляет данные запроса для получения объектов по конфигурации клиента
 * - REST Assured автоматически преобразует этот объект в JSON при отправке запроса
 * 
 * ПОЛЯ:
 * - customerCode - код клиента (обязательное)
 * - mainLayer - основной слой с layerId и properties
 * - geometry - геометрия в формате WKT (например, POLYGON(...))
 * - methodType - тип метода (например, "OBJECTS_BY_POLYGON")
 * - pageRequest - параметры пагинации (pageNumber, pageSize)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetObjectsByCustomerConfigRequest {
    
    @JsonProperty("customerCode")
    private String customerCode;
    
    @JsonProperty("mainLayer")
    private MainLayer mainLayer;
    
    @JsonProperty("geometry")
    private String geometry;
    
    @JsonProperty("methodType")
    private String methodType;
    
    @JsonProperty("pageRequest")
    private PageRequest pageRequest;

    public GetObjectsByCustomerConfigRequest() {
    }

    public GetObjectsByCustomerConfigRequest(String customerCode, MainLayer mainLayer, 
                                           String geometry, String methodType, PageRequest pageRequest) {
        this.customerCode = customerCode;
        this.mainLayer = mainLayer;
        this.geometry = geometry;
        this.methodType = methodType;
        this.pageRequest = pageRequest;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public MainLayer getMainLayer() {
        return mainLayer;
    }

    public void setMainLayer(MainLayer mainLayer) {
        this.mainLayer = mainLayer;
    }

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }

    public String getMethodType() {
        return methodType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    public PageRequest getPageRequest() {
        return pageRequest;
    }

    public void setPageRequest(PageRequest pageRequest) {
        this.pageRequest = pageRequest;
    }
}
