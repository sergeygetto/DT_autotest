package com.dt.tests;

import com.dt.base.BaseTest;
import com.dt.base.Config;
import com.dt.base.SensitiveDataFilter;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;

import java.util.List;
import java.util.Map;

/**
 * Базовый класс для тестов Query-intersect API.
 * Содержит общие константы (URL, эндпоинты, геометрии), setUp и хелперы проверки структуры ответов.
 * От него могут наследоваться классы тестов по отдельным эндпоинтам.
 */
public abstract class QueryIntersectTestBase extends BaseTest {

    // Публичный репозиторий: не храним здесь URL внутренних стендов.
    // Для запуска задайте baseUrl через -DbaseUrl или переменную окружения baseUrl.
    protected static final String DEFAULT_BASE_URL = "https://example.com";
    protected static final String GET_OBJECTS_ENDPOINT = "/da-cm-map-backend-query-intersect-object/geoData/v2/getObjectsByCustomerConfig";
    protected static final String GET_OBJECTS_BY_POLYGON_ENDPOINT = "/da-cm-map-backend-query-intersect-object/geoData/v2/getObjectsByPolygon";
    protected static final String GET_OBJECTS_BY_COORDINATES_ENDPOINT = "/da-cm-map-backend-query-intersect-object/geoData/v2/getObjectsByCoordinates";
    protected static final String GET_AGGREGATED_OBJECTS_BY_COORDINATES_ENDPOINT = "/da-cm-map-backend-query-intersect-object/geoData/v2/getAggregatedObjectsByCoordinates";
    protected static final String CUSTOMER_CODE = "dtwWhatsHere";
    protected static final String METHOD_CONFIGS_ENDPOINT = "/da-cm-map-backend-query-intersect-object/methodConfigs";
    protected static final String METHOD_CONFIGS_MANAGER_ENDPOINT = "/da-cm-map-backend-manager-intersect-object/methodConfigs";
    protected static final String USER_REQUEST_HISTORY_LIST_ENDPOINT = "/da-cm-map-backend-query-intersect-object/userRequestHistory/list";
    protected static final String USER_REQUEST_HISTORY_ENDPOINT = "/da-cm-map-backend-query-intersect-object/userRequestHistory";

    protected static final String POLYGON_GEOMETRY = "POLYGON((37.50735858627024 55.675808571291896, 37.49794969360485 55.670726977282186, 37.5075289686325 55.667737496011796, 37.51608594948658 55.67111132258964, 37.50735858627024 55.675808571291896))";
    protected static final String INVALID_POLYGON_GEOMETRY = "POLYGON((37.50735858627024 55.675808571291896, 37.49794969360485 55.670726977282186, 37.5075289686325 55.667737496011796, 37.51608594948658 55.67111132258964))";
    protected static final String METHOD_TYPE_POLYGON = "OBJECTS_BY_POLYGON";
    protected static final String METHOD_TYPE_COORDINATE = "OBJECTS_BY_COORDINATE";
    protected static final String METHOD_TYPE_AGGREGATED = "AGGREGATED_OBJECTS_BY_COORDINATE";
    protected static final String AGGREGATED_POLYGON_GEOMETRY = "POLYGON((37.50506204454163 55.6775026783655, 37.50707692247116 55.6761392483632, 37.50907154787035 55.67683111423196, 37.50725264289093 55.67844996906348, 37.50506204454163 55.6775026783655))";
    protected static final String POLYGON_COORDINATES = "POLYGON((37.523775383060496 55.68599233364168, 37.532037469347614 55.68142197987103, 37.533259148677814 55.68080184972817, 37.533466548201034 55.68072536958556, 37.53897342969739 55.68433830216799, 37.53004074918445 55.68942717890806, 37.523775383060496 55.68599233364168))";

    /** lastRequestId из теста 67 (userRequestHistory/list), используется в тесте 71 (userRequestHistory/{id}). */
    protected static Integer lastRequestId = null;

    @BeforeClass
    public void setUpQueryIntersect() {
        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        setBaseUrl(baseUrl);
        String cookieValue = Config.getCookie();
        if (cookieValue == null || cookieValue.isEmpty()) {
            logger.error("КРИТИЧЕСКАЯ ОШИБКА: Cookie не установлена! Установите переменную окружения 'cookie' или -Dcookie=value");
            logger.error("Пример: mvn test -Dtest=QueryIntersectApiTest -Dcookie=\"your-cookie-value\"");
            throw new AssertionError("Cookie не установлена. Задайте -Dcookie=... или переменную окружения cookie.");
        }
        setCookie("cookie", cookieValue);
        logger.info("Cookie установлена (маскированная): {}", SensitiveDataFilter.maskForLogging(cookieValue));
        logger.info("QueryIntersectTestBase инициализирован. Base URL: {}", baseUrl);
    }

    @SuppressWarnings("unchecked")
    protected void assertFeatureCollectionStructure(List<Map<String, Object>> features) {
        if (features == null || features.isEmpty()) {
            return;
        }
        for (int i = 0; i < features.size(); i++) {
            Map<String, Object> feature = features.get(i);
            Assert.assertNotNull(feature, "feature[" + i + "] отсутствует");
            Assert.assertNotNull(feature.get("type"), "feature[" + i + "].type отсутствует");
            Assert.assertTrue(feature.get("type") instanceof String, "feature[" + i + "].type должен быть строкой");
            Assert.assertNotNull(feature.get("layerId"), "feature[" + i + "].layerId отсутствует");
            Assert.assertTrue(feature.get("layerId") instanceof String, "feature[" + i + "].layerId должен быть строкой");
            Assert.assertNotNull(feature.get("geometry"), "feature[" + i + "].geometry отсутствует");
            Assert.assertTrue(feature.get("geometry") instanceof Map, "feature[" + i + "].geometry должен быть объектом");
            Map<String, Object> geometry = (Map<String, Object>) feature.get("geometry");
            Assert.assertNotNull(geometry.get("type"), "feature[" + i + "].geometry.type отсутствует");
            Assert.assertTrue(geometry.get("type") instanceof String, "feature[" + i + "].geometry.type должен быть строкой");
            Assert.assertNotNull(geometry.get("coordinates"), "feature[" + i + "].geometry.coordinates отсутствует");
            if ("MultiPolygon".equals(geometry.get("type"))) {
                Assert.assertTrue(geometry.get("coordinates") instanceof List,
                    "coordinates в feature[" + i + "] должны быть массивом для MultiPolygon");
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void assertUserRequestHistoryFeaturesStructure(List<Map<String, Object>> features) {
        if (features == null || features.isEmpty()) {
            return;
        }
        for (int i = 0; i < features.size(); i++) {
            Map<String, Object> feature = features.get(i);
            Assert.assertNotNull(feature, "feature[" + i + "] должен быть объектом");
            Assert.assertTrue(feature.get("layerId") instanceof String,
                "feature[" + i + "].layerId должен быть строкой");
            Assert.assertNotNull(feature.get("geometry"), "feature[" + i + "].geometry должен присутствовать");
            Map<String, Object> geometry = (Map<String, Object>) feature.get("geometry");
            Assert.assertNotNull(geometry.get("type"), "feature[" + i + "].geometry.type должен присутствовать");
            Assert.assertTrue(geometry.get("type") instanceof String,
                "feature[" + i + "].geometry.type должен быть строкой");
            Assert.assertNotNull(geometry.get("coordinates"),
                "feature[" + i + "].geometry.coordinates должен присутствовать");
            if (feature.containsKey("centroid") && feature.get("centroid") != null) {
                Map<String, Object> centroid = (Map<String, Object>) feature.get("centroid");
                Assert.assertEquals(centroid.get("type"), "Point",
                    "feature[" + i + "].centroid.type должен быть Point");
                Assert.assertNotNull(centroid.get("coordinates"),
                    "feature[" + i + "].centroid.coordinates должен присутствовать");
                List<Object> centroidCoords = (List<Object>) centroid.get("coordinates");
                Assert.assertEquals(centroidCoords.size(), 2,
                    "feature[" + i + "].centroid.coordinates должен содержать 2 элемента");
            }
        }
    }

    /**
     * Проверка структуры features для ответа getObjectsByCoordinates с returnCentroid:
     * type, layerId, centroid (Point), distance, geometry (MultiPolygon); при наличии — objectId, properties, correlationId.
     */
    @SuppressWarnings("unchecked")
    protected void assertFeatureWithCentroidAndDistance(List<Map<String, Object>> features) {
        if (features == null || features.isEmpty()) {
            return;
        }
        for (int i = 0; i < features.size(); i++) {
            Map<String, Object> feature = features.get(i);
            Assert.assertNotNull(feature, "feature[" + i + "] отсутствует");
            Assert.assertNotNull(feature.get("type"), "feature[" + i + "].type отсутствует");
            Assert.assertTrue(feature.get("type") instanceof String, "feature[" + i + "].type должен быть строкой");
            Assert.assertNotNull(feature.get("layerId"), "feature[" + i + "].layerId отсутствует");
            Assert.assertTrue(feature.get("layerId") instanceof String, "feature[" + i + "].layerId должен быть строкой");
            Assert.assertNotNull(feature.get("centroid"), "feature[" + i + "].centroid отсутствует");
            Assert.assertTrue(feature.get("centroid") instanceof Map, "feature[" + i + "].centroid должен быть объектом");
            Map<String, Object> centroid = (Map<String, Object>) feature.get("centroid");
            Assert.assertNotNull(centroid.get("type"), "feature[" + i + "].centroid.type отсутствует");
            Assert.assertEquals(centroid.get("type"), "Point", "feature[" + i + "].centroid.type должен быть Point");
            Assert.assertNotNull(centroid.get("coordinates"), "feature[" + i + "].centroid.coordinates отсутствует");
            Assert.assertTrue(centroid.get("coordinates") instanceof List, "feature[" + i + "].centroid.coordinates должен быть массивом");
            List<Object> centroidCoordinates = (List<Object>) centroid.get("coordinates");
            Assert.assertEquals(centroidCoordinates.size(), 2, "feature[" + i + "].centroid.coordinates должен содержать 2 элемента");
            Assert.assertNotNull(feature.get("distance"), "feature[" + i + "].distance отсутствует");
            Assert.assertTrue(feature.get("distance") instanceof Number, "feature[" + i + "].distance должен быть числом");
            Assert.assertNotNull(feature.get("geometry"), "feature[" + i + "].geometry отсутствует");
            Assert.assertTrue(feature.get("geometry") instanceof Map, "feature[" + i + "].geometry должен быть объектом");
            Map<String, Object> geometry = (Map<String, Object>) feature.get("geometry");
            Assert.assertNotNull(geometry.get("type"), "feature[" + i + "].geometry.type отсутствует");
            Assert.assertEquals(geometry.get("type"), "MultiPolygon", "feature[" + i + "].geometry.type должен быть MultiPolygon");
            Assert.assertNotNull(geometry.get("coordinates"), "feature[" + i + "].geometry.coordinates отсутствует");
            Assert.assertTrue(geometry.get("coordinates") instanceof List, "feature[" + i + "].geometry.coordinates должен быть массивом");
            if (feature.containsKey("objectId") && feature.get("objectId") != null) {
                Assert.assertTrue(feature.get("objectId") instanceof String, "feature[" + i + "].objectId должен быть строкой");
            }
            if (feature.containsKey("properties") && feature.get("properties") != null) {
                Assert.assertTrue(feature.get("properties") instanceof Map, "feature[" + i + "].properties должен быть объектом");
                Map<String, Object> properties = (Map<String, Object>) feature.get("properties");
                if (properties.containsKey("id")) {
                    Assert.assertNotNull(properties.get("id"), "feature[" + i + "].properties.id отсутствует");
                    Assert.assertTrue(properties.get("id") instanceof Number, "feature[" + i + "].properties.id должен быть числом");
                }
            }
            if (feature.containsKey("correlationId") && feature.get("correlationId") != null) {
                Assert.assertTrue(feature.get("correlationId") instanceof String, "feature[" + i + "].correlationId должен быть строкой");
            }
        }
    }

    /**
     * Проверка структуры features для ответа getAggregatedObjectsByCoordinates:
     * type=Feature, layerId, centroid (если есть), geometry (MultiPolygon или Polygon), distance (если есть), properties (если есть).
     */
    @SuppressWarnings("unchecked")
    protected void assertAggregatedFeatureStructure(List<Map<String, Object>> features) {
        if (features == null || features.isEmpty()) {
            return;
        }
        for (int i = 0; i < features.size(); i++) {
            Map<String, Object> feature = features.get(i);
            Assert.assertNotNull(feature, "feature[" + i + "] отсутствует");
            Assert.assertEquals(feature.get("type"), "Feature", "feature[" + i + "].type должен быть Feature");
            Assert.assertTrue(feature.get("layerId") instanceof String, "feature[" + i + "].layerId должен быть строкой");
            if (feature.containsKey("centroid") && feature.get("centroid") != null) {
                Map<String, Object> centroid = (Map<String, Object>) feature.get("centroid");
                Assert.assertEquals(centroid.get("type"), "Point", "feature[" + i + "].centroid.type должен быть Point");
                Assert.assertNotNull(centroid.get("coordinates"), "feature[" + i + "].centroid.coordinates отсутствует");
                List<Object> centroidCoords = (List<Object>) centroid.get("coordinates");
                Assert.assertEquals(centroidCoords.size(), 2, "feature[" + i + "].centroid.coordinates должен содержать 2 элемента");
            }
            if (feature.containsKey("distance") && feature.get("distance") != null) {
                Assert.assertTrue(feature.get("distance") instanceof Number, "feature[" + i + "].distance должен быть числом");
            }
            Assert.assertNotNull(feature.get("geometry"), "feature[" + i + "].geometry отсутствует");
            Map<String, Object> geometry = (Map<String, Object>) feature.get("geometry");
            Object geomType = geometry.get("type");
            Assert.assertTrue("Polygon".equals(geomType) || "MultiPolygon".equals(geomType),
                "feature[" + i + "].geometry.type должен быть Polygon или MultiPolygon, получено: " + geomType);
            Assert.assertNotNull(geometry.get("coordinates"), "feature[" + i + "].geometry.coordinates отсутствует");
            List<Object> geometryCoords = (List<Object>) geometry.get("coordinates");
            Assert.assertTrue(geometryCoords.size() > 0, "feature[" + i + "].geometry.coordinates должен быть непустым массивом");
            // Проверка вложенности только для MultiPolygon (у Polygon координаты: [rings][points][x,y])
            if ("MultiPolygon".equals(geomType) && geometryCoords.size() > 0 && geometryCoords.get(0) instanceof List) {
                List<Object> firstLevel = (List<Object>) geometryCoords.get(0);
                if (firstLevel.size() > 0 && firstLevel.get(0) instanceof List) {
                    List<Object> secondLevel = (List<Object>) firstLevel.get(0);
                    if (secondLevel.size() > 0 && secondLevel.get(0) instanceof List) {
                        Assert.assertTrue(secondLevel.get(0) instanceof List, "coordinates[0][0][0] должен быть массивом");
                    }
                }
            }
            if (feature.containsKey("properties") && feature.get("properties") instanceof Map) {
                Map<String, Object> properties = (Map<String, Object>) feature.get("properties");
                if (properties.containsKey("id")) {
                    Assert.assertNotNull(properties.get("id"), "feature[" + i + "].properties.id отсутствует");
                }
            }
        }
    }

    /**
     * Проверка что у агрегированных features properties.id="1" и полигоны замкнуты (первая и последняя точка кольца совпадают).
     */
    @SuppressWarnings("unchecked")
    protected void assertAggregatedFeaturesPropertiesIdAndPolygonsClosed(List<Map<String, Object>> features) {
        if (features == null || features.isEmpty()) {
            return;
        }
        for (int i = 0; i < features.size(); i++) {
            Map<String, Object> feature = features.get(i);
            if (feature.containsKey("properties") && feature.get("properties") instanceof Map) {
                Map<String, Object> properties = (Map<String, Object>) feature.get("properties");
                if (properties.containsKey("id")) {
                    Assert.assertEquals(properties.get("id"), "1",
                        "feature[" + i + "].properties.id должен быть равен \"1\"");
                }
            }
            if (feature.containsKey("geometry") && feature.get("geometry") instanceof Map) {
                Map<String, Object> geometry = (Map<String, Object>) feature.get("geometry");
                if (geometry.containsKey("coordinates") && geometry.get("coordinates") instanceof List) {
                    List<Object> polygons = (List<Object>) geometry.get("coordinates");
                    for (int polygonIndex = 0; polygonIndex < polygons.size(); polygonIndex++) {
                        if (polygons.get(polygonIndex) instanceof List) {
                            List<Object> rings = (List<Object>) polygons.get(polygonIndex);
                            for (int ringIndex = 0; ringIndex < rings.size(); ringIndex++) {
                                if (rings.get(ringIndex) instanceof List) {
                                    List<Object> ring = (List<Object>) rings.get(ringIndex);
                                    if (ring.size() > 0) {
                                        Object firstObj = ring.get(0);
                                        Object lastObj = ring.get(ring.size() - 1);
                                        if (firstObj instanceof List && lastObj instanceof List) {
                                            List<Object> first = (List<Object>) firstObj;
                                            List<Object> last = (List<Object>) lastObj;
                                            if (first.size() >= 2 && last.size() >= 2) {
                                                Assert.assertEquals(first.get(0), last.get(0),
                                                    "Feature " + i + ", polygon " + polygonIndex + ", ring " + ringIndex + ": первая и последняя долгота должны совпадать");
                                                Assert.assertEquals(first.get(1), last.get(1),
                                                    "Feature " + i + ", polygon " + polygonIndex + ", ring " + ringIndex + ": первая и последняя широта должны совпадать");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Проверка что полигоны в features замкнуты (первая и последняя точка кольца совпадают).
     * @param coordCount число координат для сравнения (2 — lon/lat, 8 — как в Postman)
     */
    @SuppressWarnings("unchecked")
    protected void assertPolygonsClosed(List<Map<String, Object>> features, int coordCount) {
        if (features == null || features.isEmpty() || coordCount < 1) {
            return;
        }
        for (int i = 0; i < features.size(); i++) {
            Map<String, Object> feature = features.get(i);
            if (feature.containsKey("geometry") && feature.get("geometry") instanceof Map) {
                Map<String, Object> geometry = (Map<String, Object>) feature.get("geometry");
                if (geometry.containsKey("coordinates") && geometry.get("coordinates") instanceof List) {
                    List<Object> polygons = (List<Object>) geometry.get("coordinates");
                    for (int polygonIndex = 0; polygonIndex < polygons.size(); polygonIndex++) {
                        if (polygons.get(polygonIndex) instanceof List) {
                            List<Object> rings = (List<Object>) polygons.get(polygonIndex);
                            for (int ringIndex = 0; ringIndex < rings.size(); ringIndex++) {
                                if (rings.get(ringIndex) instanceof List) {
                                    List<Object> ring = (List<Object>) rings.get(ringIndex);
                                    if (ring.size() > 0) {
                                        Object firstObj = ring.get(0);
                                        Object lastObj = ring.get(ring.size() - 1);
                                        if (firstObj instanceof List && lastObj instanceof List) {
                                            List<Object> first = (List<Object>) firstObj;
                                            List<Object> last = (List<Object>) lastObj;
                                            for (int c = 0; c < coordCount && c < first.size() && c < last.size(); c++) {
                                                Assert.assertEquals(first.get(c), last.get(c),
                                                    "Feature " + i + ", polygon " + polygonIndex + ", ring " + ringIndex + ": координата[" + c + "] первой и последней точки должны совпадать");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /** Спецификация запроса с cookie_autotest; если не задана — возвращает null. */
    protected RequestSpecification getRequestSpecWithAutotestCookie() {
        String cookieAutotest = System.getProperty("cookie_autotest");
        if (cookieAutotest == null || cookieAutotest.isEmpty()) {
            cookieAutotest = System.getenv("cookie_autotest");
        }
        if (cookieAutotest != null) {
            cookieAutotest = cookieAutotest.trim();
        }
        if (cookieAutotest == null || cookieAutotest.isEmpty()) {
            return null;
        }
        return new RequestSpecBuilder()
                .addFilter(new io.qameta.allure.restassured.AllureRestAssured())
                .addFilter(new com.dt.base.SensitiveDataFilter())
                .addCookie("cookie", cookieAutotest)
                .build();
    }
}
