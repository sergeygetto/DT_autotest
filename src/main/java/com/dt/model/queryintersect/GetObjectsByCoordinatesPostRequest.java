package com.dt.model.queryintersect;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * МОДЕЛЬ КЛАССА GETOBJECTSBYCOORDINATESPOSTREQUEST (ЗАПРОС ПОЛУЧЕНИЯ ОБЪЕКТОВ ПО КООРДИНАТАМ - POST)
 * 
 * ЗАЧЕМ НУЖЕН ЭТОТ КЛАСС:
 * - Представляет данные запроса для получения объектов по координатам через POST
 * - REST Assured автоматически преобразует этот объект в JSON при отправке запроса
 * 
 * ПОЛЯ:
 * - layers - список слоев (обязательное)
 * - coordinates - координаты в формате WKT (например, POINT(...))
 * - radius - радиус поиска (число)
 * - returnCentroid - возвращать ли центроид (boolean)
 * - pageRequest - параметры пагинации (pageNumber, pageSize)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetObjectsByCoordinatesPostRequest {
    
    @JsonProperty("layers")
    private List<Layer> layers;
    
    @JsonProperty("coordinates")
    private String coordinates;
    
    @JsonProperty("radius")
    private Integer radius;
    
    @JsonProperty("returnCentroid")
    private Boolean returnCentroid;
    
    @JsonProperty("pageRequest")
    private PageRequest pageRequest;

    public GetObjectsByCoordinatesPostRequest() {
    }

    public GetObjectsByCoordinatesPostRequest(List<Layer> layers, String coordinates, 
                                             Integer radius, Boolean returnCentroid, PageRequest pageRequest) {
        this.layers = layers;
        this.coordinates = coordinates;
        this.radius = radius;
        this.returnCentroid = returnCentroid;
        this.pageRequest = pageRequest;
    }

    public List<Layer> getLayers() {
        return layers;
    }

    public void setLayers(List<Layer> layers) {
        this.layers = layers;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public Integer getRadius() {
        return radius;
    }

    public void setRadius(Integer radius) {
        this.radius = radius;
    }

    public Boolean getReturnCentroid() {
        return returnCentroid;
    }

    public void setReturnCentroid(Boolean returnCentroid) {
        this.returnCentroid = returnCentroid;
    }

    public PageRequest getPageRequest() {
        return pageRequest;
    }

    public void setPageRequest(PageRequest pageRequest) {
        this.pageRequest = pageRequest;
    }
}
