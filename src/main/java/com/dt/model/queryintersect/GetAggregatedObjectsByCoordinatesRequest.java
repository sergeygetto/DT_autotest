package com.dt.model.queryintersect;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * МОДЕЛЬ КЛАССА GETAGGREGATEDOBJECTSBYCOORDINATESREQUEST (ЗАПРОС ПОЛУЧЕНИЯ АГРЕГИРОВАННЫХ ОБЪЕКТОВ ПО КООРДИНАТАМ)
 * 
 * ЗАЧЕМ НУЖЕН ЭТОТ КЛАСС:
 * - Представляет данные запроса для получения агрегированных объектов по координатам
 * - REST Assured автоматически преобразует этот объект в JSON при отправке запроса
 * 
 * ПОЛЯ:
 * - mainLayer - основной слой (обязательное)
 * - linkedLayers - связанные слои (массив)
 * - coordinates - координаты в формате WKT (например, POINT(...))
 * - returnCentroid - возвращать ли центроид (boolean)
 * - pageRequest - параметры пагинации (pageNumber, pageSize)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetAggregatedObjectsByCoordinatesRequest {
    
    @JsonProperty("mainLayer")
    private MainLayer mainLayer;
    
    @JsonProperty("linkedLayers")
    private List<MainLayer> linkedLayers;
    
    @JsonProperty("coordinates")
    private String coordinates;
    
    @JsonProperty("geomAggregation")
    private String geomAggregation;
    
    @JsonProperty("returnCentroid")
    private Boolean returnCentroid;
    
    @JsonProperty("distance")
    private Integer distance;
    
    @JsonProperty("pageRequest")
    private PageRequest pageRequest;

    public GetAggregatedObjectsByCoordinatesRequest() {
    }

    public GetAggregatedObjectsByCoordinatesRequest(MainLayer mainLayer, List<MainLayer> linkedLayers,
                                                    String coordinates, String geomAggregation, Boolean returnCentroid, PageRequest pageRequest) {
        this.mainLayer = mainLayer;
        this.linkedLayers = linkedLayers;
        this.coordinates = coordinates;
        this.geomAggregation = geomAggregation;
        this.returnCentroid = returnCentroid;
        this.distance = null;
        this.pageRequest = pageRequest;
    }

    public GetAggregatedObjectsByCoordinatesRequest(MainLayer mainLayer, List<MainLayer> linkedLayers,
                                                    String coordinates, String geomAggregation, Boolean returnCentroid, Integer distance, PageRequest pageRequest) {
        this.mainLayer = mainLayer;
        this.linkedLayers = linkedLayers;
        this.coordinates = coordinates;
        this.geomAggregation = geomAggregation;
        this.returnCentroid = returnCentroid;
        this.distance = distance;
        this.pageRequest = pageRequest;
    }

    public MainLayer getMainLayer() {
        return mainLayer;
    }

    public void setMainLayer(MainLayer mainLayer) {
        this.mainLayer = mainLayer;
    }

    public List<MainLayer> getLinkedLayers() {
        return linkedLayers;
    }

    public void setLinkedLayers(List<MainLayer> linkedLayers) {
        this.linkedLayers = linkedLayers;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public String getGeomAggregation() {
        return geomAggregation;
    }

    public void setGeomAggregation(String geomAggregation) {
        this.geomAggregation = geomAggregation;
    }

    public Boolean getReturnCentroid() {
        return returnCentroid;
    }

    public void setReturnCentroid(Boolean returnCentroid) {
        this.returnCentroid = returnCentroid;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public PageRequest getPageRequest() {
        return pageRequest;
    }

    public void setPageRequest(PageRequest pageRequest) {
        this.pageRequest = pageRequest;
    }
}
