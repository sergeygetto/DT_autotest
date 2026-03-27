package com.dt.model.queryintersect;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * МОДЕЛЬ КЛАССА GETOBJECTSBYPOLYGONREQUEST (ЗАПРОС ПОЛУЧЕНИЯ ОБЪЕКТОВ ПО ПОЛИГОНУ)
 * 
 * ЗАЧЕМ НУЖЕН ЭТОТ КЛАСС:
 * - Представляет данные запроса для получения объектов по полигону
 * - REST Assured автоматически преобразует этот объект в JSON при отправке запроса
 * 
 * ПОЛЯ:
 * - layers - список слоев (обязательное)
 * - returnCentroid - возвращать ли центроид (boolean)
 * - coordinates - координаты в формате WKT (например, POLYGON(...))
 * - pageRequest - параметры пагинации (pageNumber, pageSize)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetObjectsByPolygonRequest {
    
    @JsonProperty("layers")
    private List<Layer> layers;
    
    @JsonProperty("returnCentroid")
    private Boolean returnCentroid;
    
    @JsonProperty("coordinates")
    private String coordinates;
    
    @JsonProperty("pageRequest")
    private PageRequest pageRequest;

    public GetObjectsByPolygonRequest() {
    }

    public GetObjectsByPolygonRequest(List<Layer> layers, Boolean returnCentroid, 
                                     String coordinates, PageRequest pageRequest) {
        this.layers = layers;
        this.returnCentroid = returnCentroid;
        this.coordinates = coordinates;
        this.pageRequest = pageRequest;
    }

    public List<Layer> getLayers() {
        return layers;
    }

    public void setLayers(List<Layer> layers) {
        this.layers = layers;
    }

    public Boolean getReturnCentroid() {
        return returnCentroid;
    }

    public void setReturnCentroid(Boolean returnCentroid) {
        this.returnCentroid = returnCentroid;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public PageRequest getPageRequest() {
        return pageRequest;
    }

    public void setPageRequest(PageRequest pageRequest) {
        this.pageRequest = pageRequest;
    }
}
