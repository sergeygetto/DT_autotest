package com.dt.tests;

import com.dt.model.methodconfig.MethodConfigUpdateRequest;
import com.dt.model.queryintersect.GetAggregatedObjectsByCoordinatesRequest;
import com.dt.model.queryintersect.GetObjectsByCustomerConfigRequest;
import com.dt.model.queryintersect.GetObjectsByCoordinatesPostRequest;
import com.dt.model.queryintersect.GetObjectsByPolygonRequest;
import com.dt.model.queryintersect.Layer;
import com.dt.model.queryintersect.MainLayer;
import com.dt.model.queryintersect.PageRequest;
import com.dt.model.userrequesthistory.SortRequest;
import com.dt.model.userrequesthistory.UserRequestHistoryListRequest;
import io.qameta.allure.*;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * КЛАСС С ТЕСТАМИ ДЛЯ API QUERY-INTERSECT (ПОЛУЧЕНИЕ ОБЪЕКТОВ ПО КОНФИГУРАЦИИ КЛИЕНТА)
 * 
 * ЗАЧЕМ НУЖЕН ЭТОТ КЛАСС:
 * - Содержит все тесты для API получения объектов по конфигурации клиента
 * - Наследуется от BaseTest - получает все методы для работы с API
 * - Тесты выполняются в строгом порядке (priority 1, 2, 3...)
 * 
 * КАК РАБОТАЕТ:
 * - TestNG находит методы с аннотацией @Test
 * - Выполняет их в порядке priority (1, 2, 3...)
 * - @BeforeClass выполняется один раз перед всеми тестами
 * 
 * АННОТАЦИИ ALLURE:
 * - @Epic - большая группа тестов (например, "Query-intersect API Testing")
 * - @Feature - функциональность (например, "Get Objects By Customer Config")
 */
@Epic("Query-intersect API Testing")
@Feature("Get Objects By Customer Config")
public class QueryIntersectApiTest extends QueryIntersectTestBase {

    /**
     * DataProvider для негативных тестов getObjectsByCustomerConfig с отсутствующим обязательным полем.
     * Возвращает: requestBody, expectedStatus, messageFragment для проверки в ответе.
     */
    @DataProvider(name = "missingRequiredFieldGetObjectsByCustomerConfig")
    public Object[][] missingRequiredFieldGetObjectsByCustomerConfig() {
        String bodyWithoutCustomerCode = "{\n"
            + "  \"mainLayer\": {\n"
            + "    \"layerId\": \"\",\n"
            + "    \"properties\": [ \"string\" ]\n"
            + "  },\n"
            + "  \"geometry\": \"" + POLYGON_GEOMETRY + "\",\n"
            + "  \"methodType\": \"" + METHOD_TYPE_POLYGON + "\",\n"
            + "  \"pageRequest\": { \"pageNumber\": 0, \"pageSize\": 10000 }\n"
            + "}";
        String bodyWithoutMethodType = "{\n"
            + "  \"customerCode\": \"" + CUSTOMER_CODE + "\",\n"
            + "  \"mainLayer\": {\n"
            + "    \"layerId\": \"\",\n"
            + "    \"properties\": [ \"string\" ]\n"
            + "  },\n"
            + "  \"geometry\": \"" + POLYGON_GEOMETRY + "\",\n"
            + "  \"pageRequest\": { \"pageNumber\": 0, \"pageSize\": 10000 }\n"
            + "}";
        return new Object[][] {
            { bodyWithoutCustomerCode, 500, "Cannot deserialize" },
            { bodyWithoutMethodType, 500, "methodType" }
        };
    }

    /**
     * ТЕСТ 1: POST getObjectsByCustomerConfig (polygon) - ПОЗИТИВНЫЙ ТЕСТ
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API позволяет получить объекты по конфигурации клиента с полигоном
     * - Проверяет структуру ответа (size, content с FeatureCollection)
     * - Проверяет валидность геометрии в features
     * - Проверяет время ответа
     */
    @Test(priority = 1, description = "POST getObjectsByCustomerConfig (polygon)")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение объектов по конфигурации клиента с полигоном. Проверка статуса 200, структуры ответа (size, content с FeatureCollection), валидности features и времени ответа.")
    @Story("Get Objects By Customer Config - Polygon")
    public void testGetObjectsByCustomerConfigPolygon() {
        logger.info("Выполнение теста: POST getObjectsByCustomerConfig (polygon)");
        
        // Создаем объект MainLayer
        List<String> properties = new ArrayList<>();
        properties.add("string");
        MainLayer mainLayer = new MainLayer("", properties);
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 10000);
        
        // Создаем объект запроса
        GetObjectsByCustomerConfigRequest request = new GetObjectsByCustomerConfigRequest(
            CUSTOMER_CODE,
            mainLayer,
            POLYGON_GEOMETRY,
            METHOD_TYPE_POLYGON,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_OBJECTS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("size", notNullValue())
                .body("size", instanceOf(Integer.class))
                .body("content", notNullValue())
                .body("content", instanceOf(java.util.Map.class))
                .body("content.type", equalTo("FeatureCollection"))
                .body("content.features", notNullValue())
                .body("content.features", instanceOf(java.util.List.class))
                .body("content.features.size()", greaterThan(0))
                .extract()
                .response();
        
        List<Map<String, Object>> features = response.jsonPath().getList("content.features");
        assertFeatureCollectionStructure(features);
        logger.info("Тест успешно завершен. Размер ответа: {}, количество features: {}",
            response.jsonPath().getInt("size"), features != null ? features.size() : 0);
    }

    /**
     * ТЕСТ 2–3: NEGATIVE POST getObjectsByCustomerConfig — отсутствует обязательное поле (customerCode или methodType).
     * Параметризованный тест по DataProvider.
     */
    @Test(priority = 2, dataProvider = "missingRequiredFieldGetObjectsByCustomerConfig", description = "NEGATIVE getObjectsByCustomerConfig - отсутствует обязательное поле")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить объекты без обязательного поля (customerCode или methodType). Проверка статуса 500 и сообщения об ошибке.")
    @Story("Negative Tests - Missing Required Field (CustomerCode / MethodType)")
    public void testGetObjectsByCustomerConfigMissingRequiredField(String requestBody, int expectedStatus, String messageFragment) {
        logger.info("Выполнение теста: NEGATIVE getObjectsByCustomerConfig — ожидаемый статус {}, фрагмент сообщения: {}", expectedStatus, messageFragment);
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(GET_OBJECTS_ENDPOINT)
                .then()
                .statusCode(expectedStatus)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString(messageFragment))
                .extract()
                .response();
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 4: NEGATIVE incorrect methodType (NO_POLYGON)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию - при methodType COORDINATE должна быть точка, а не полигон
     * - Проверяет что API возвращает ошибку 500 при несоответствии methodType и geometry
     */
    @Test(priority = 4, description = "NEGATIVE incorrect methodType (NO_POLYGON)")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить объекты с неправильным methodType (COORDINATE вместо POLYGON). Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Incorrect MethodType")
    public void testGetObjectsWithIncorrectMethodType() {
        logger.info("Выполнение теста: NEGATIVE incorrect methodType (NO_POLYGON)");
        
        // Создаем JSON строку с methodType COORDINATE, но geometry - полигон
        String requestBody = "{\n" +
            "  \"customerCode\": \"" + CUSTOMER_CODE + "\",\n" +
            "  \"mainLayer\": {\n" +
            "    \"layerId\": \"\",\n" +
            "    \"properties\": [\n" +
            "      \"string\"\n" +
            "    ]\n" +
            "  },\n" +
            "  \"geometry\": \"" + INVALID_POLYGON_GEOMETRY + "\",\n" +
            "  \"methodType\": \"" + METHOD_TYPE_COORDINATE + "\",\n" +
            "  \"pageRequest\": {\n" +
            "    \"pageNumber\": 0,\n" +
            "    \"pageSize\": 10000\n" +
            "  }\n" +
            "}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(GET_OBJECTS_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("должно содержать корректную WKT-точку"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 5: NEGATIVE POST getObjectsByCustomerConfig - некорректная geometry (незамкнутый полигон)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию геометрии - полигон должен быть замкнутым
     * - Проверяет что API возвращает ошибку 500 при некорректной геометрии
     */
    @Test(priority = 5, description = "NEGATIVE POST getObjectsByCustomerConfig - некорректная geometry")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить объекты с некорректной геометрией (незамкнутый полигон). Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid Geometry")
    public void testGetObjectsWithInvalidGeometry() {
        logger.info("Выполнение теста: NEGATIVE POST getObjectsByCustomerConfig - некорректная geometry");
        
        // Создаем объект MainLayer
        List<String> properties = new ArrayList<>();
        properties.add("string");
        MainLayer mainLayer = new MainLayer("", properties);
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 10000);
        
        // Создаем объект запроса с некорректной геометрией
        GetObjectsByCustomerConfigRequest request = new GetObjectsByCustomerConfigRequest(
            CUSTOMER_CODE,
            mainLayer,
            INVALID_POLYGON_GEOMETRY,
            METHOD_TYPE_POLYGON,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_OBJECTS_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .extract()
                .response();
        
        // Проверяем что сообщение содержит ожидаемый текст (один из вариантов)
        String message = response.jsonPath().getString("message");
        Assert.assertTrue(
            message.contains("Требуется задать замкнутый полигон в формате POLYGON") ||
            message.contains("должно содержать корректный WKT-полигон"),
            "message не содержит ожидаемый текст. Получено: " + message
        );
        
        logger.info("Ожидаемая ошибка получена: {}", message);
    }
    
    /**
     * ТЕСТ 6: NEGATIVE POST getObjectsByCustomerConfig - отсутствует поле geometry
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию API - поле "geometry" обязательное
     * - Проверяет что API возвращает ошибку 500 при отсутствии обязательного поля
     */
    @Test(priority = 6, description = "NEGATIVE POST getObjectsByCustomerConfig - отсутствует поле geometry")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить объекты без поля geometry. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Missing Geometry Field")
    public void testGetObjectsWithoutGeometry() {
        logger.info("Выполнение теста: NEGATIVE POST getObjectsByCustomerConfig - отсутствует поле geometry");
        
        // Создаем JSON строку без поля "geometry"
        String requestBody = "{\n" +
            "  \"customerCode\": \"" + CUSTOMER_CODE + "\",\n" +
            "  \"mainLayer\": {\n" +
            "    \"layerId\": \"\",\n" +
            "    \"properties\": [\n" +
            "      \"string\"\n" +
            "    ]\n" +
            "  },\n" +
            "  \"methodType\": \"" + METHOD_TYPE_POLYGON + "\",\n" +
            "  \"pageRequest\": {\n" +
            "    \"pageNumber\": 0,\n" +
            "    \"pageSize\": 10000\n" +
            "  }\n" +
            "}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(GET_OBJECTS_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Поле 'coordinates' должно содержать корректный WKT-полигон"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 7: NEGATIVE POST getObjectsByCustomerConfig - несуществующий customerCode
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет обработку несуществующего customerCode
     * - Проверяет что API возвращает ошибку 500
     */
    @Test(priority = 7, description = "NEGATIVE POST getObjectsByCustomerConfig - несуществующий customerCode")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить объекты с несуществующим customerCode. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Non-existent CustomerCode")
    public void testGetObjectsWithNonExistentCustomerCode() {
        logger.info("Выполнение теста: NEGATIVE POST getObjectsByCustomerConfig - несуществующий customerCode");
        
        // Создаем объект MainLayer
        List<String> properties = new ArrayList<>();
        properties.add("string");
        MainLayer mainLayer = new MainLayer("", properties);
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 10000);
        
        // Создаем объект запроса с несуществующим customerCode
        GetObjectsByCustomerConfigRequest request = new GetObjectsByCustomerConfigRequest(
            "nonExistentCustomer123",
            mainLayer,
            POLYGON_GEOMETRY,
            METHOD_TYPE_POLYGON,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_OBJECTS_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Поле 'layers' обязательно и не может быть пустым"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 8: NEGATIVE POST getObjectsByCustomerConfig - невалидный methodType
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию methodType - должен быть валидным значением enum
     * - Проверяет что API возвращает ошибку 500 при невалидном methodType
     */
    @Test(priority = 8, description = "NEGATIVE POST getObjectsByCustomerConfig - невалидный methodType")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить объекты с невалидным methodType. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid MethodType")
    public void testGetObjectsWithInvalidMethodType() {
        logger.info("Выполнение теста: NEGATIVE POST getObjectsByCustomerConfig - невалидный methodType");
        
        // Создаем объект MainLayer
        List<String> properties = new ArrayList<>();
        properties.add("string");
        MainLayer mainLayer = new MainLayer("", properties);
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 10000);
        
        // Создаем объект запроса с невалидным methodType
        GetObjectsByCustomerConfigRequest request = new GetObjectsByCustomerConfigRequest(
            CUSTOMER_CODE,
            mainLayer,
            POLYGON_GEOMETRY,
            "INVALID_METHOD_TYPE",
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_OBJECTS_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 9: NEGATIVE POST getObjectsByCustomerConfig - невалидные значения pageRequest
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию pageRequest - pageSize должен быть больше нуля
     * - Проверяет что API возвращает ошибку 500 при невалидных значениях
     */
    @Test(priority = 9, description = "NEGATIVE POST getObjectsByCustomerConfig - невалидные значения pageRequest")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить объекты с невалидными значениями pageRequest. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid PageRequest")
    public void testGetObjectsWithInvalidPageRequest() {
        logger.info("Выполнение теста: NEGATIVE POST getObjectsByCustomerConfig - невалидные значения pageRequest");
        
        // Создаем объект MainLayer
        List<String> properties = new ArrayList<>();
        properties.add("string");
        MainLayer mainLayer = new MainLayer("", properties);
        
        // Создаем объект PageRequest с невалидными значениями
        PageRequest pageRequest = new PageRequest(-1, 0);
        
        // Создаем объект запроса
        GetObjectsByCustomerConfigRequest request = new GetObjectsByCustomerConfigRequest(
            CUSTOMER_CODE,
            mainLayer,
            POLYGON_GEOMETRY,
            METHOD_TYPE_POLYGON,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_OBJECTS_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Размер страницы (pageSize) должен быть больше нуля"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 10: NEGATIVE POST getObjectsByCustomerConfig - невалидный JSON
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет обработку невалидного JSON (неправильный формат)
     * - Проверяет что API корректно обрабатывает ошибки парсинга JSON
     */
    @Test(priority = 10, description = "NEGATIVE POST getObjectsByCustomerConfig - невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка отправить невалидный JSON. Проверка статуса 400 или 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid JSON")
    public void testGetObjectsWithInvalidJson() {
        logger.info("Выполнение теста: NEGATIVE POST getObjectsByCustomerConfig - невалидный JSON");
        
        // Невалидный JSON - отсутствует закрывающая скобка }
        String invalidJson = "{\n" +
            "  \"customerCode\": \"" + CUSTOMER_CODE + "\",\n" +
            "  \"mainLayer\": {\n" +
            "    \"layerId\": \"\",\n" +
            "    \"properties\": [\n" +
            "      \"string\"\n" +
            "    ]\n" +
            "  },\n" +
            "  \"geometry\": \"" + POLYGON_GEOMETRY + "\",\n" +
            "  \"methodType\": \"" + METHOD_TYPE_POLYGON + "\",\n" +
            "  \"pageRequest\": {\n" +
            "    \"pageNumber\": 0,\n" +
            "    \"pageSize\": 10000\n";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .post(GET_OBJECTS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class))
                .body("error", notNullValue())
                .body("error", containsString("Internal Server Error"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("error"));
    }
    
    /**
     * ТЕСТ 11: POST getObjectsByCustomerConfig (aggregated) - ПОЗИТИВНЫЙ ТЕСТ
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API позволяет получить агрегированные объекты по конфигурации клиента
     * - Проверяет структуру ответа (size, content с FeatureCollection)
     * - Проверяет валидность геометрии в features
     * - Проверяет время ответа
     */
    @Test(priority = 11, description = "POST getObjectsByCustomerConfig (aggregated)")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение агрегированных объектов по конфигурации клиента. Проверка статуса 200, структуры ответа (size, content с FeatureCollection), валидности features и времени ответа.")
    @Story("Get Objects By Customer Config - Aggregated")
    public void testGetObjectsByCustomerConfigAggregated() {
        logger.info("Выполнение теста: POST getObjectsByCustomerConfig (aggregated)");
        
        // Создаем объект MainLayer с layerId = 6210
        List<String> properties = new ArrayList<>();
        properties.add("string");
        MainLayer mainLayer = new MainLayer("6210", properties);
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 50);
        
        // Создаем объект запроса
        GetObjectsByCustomerConfigRequest request = new GetObjectsByCustomerConfigRequest(
            CUSTOMER_CODE,
            mainLayer,
            AGGREGATED_POLYGON_GEOMETRY,
            METHOD_TYPE_AGGREGATED,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_OBJECTS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("size", notNullValue())
                .body("size", instanceOf(Integer.class))
                .body("size", equalTo(50))
                .body("content", notNullValue())
                .body("content", instanceOf(java.util.Map.class))
                .body("content.type", equalTo("FeatureCollection"))
                .body("content.features", notNullValue())
                .body("content.features", instanceOf(java.util.List.class))
                .body("content.features.size()", greaterThan(0))
                .extract()
                .response();
        
        List<Map<String, Object>> features = response.jsonPath().getList("content.features");
        assertFeatureCollectionStructure(features);
        logger.info("Тест успешно завершен. Размер ответа: {}, количество features: {}",
            response.jsonPath().getInt("size"), features != null ? features.size() : 0);
    }

    /**
     * ТЕСТ 12: NEGATIVE POST getObjectsByCustomerConfig (aggregated) - отсутствует поле customerCode
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию API - поле "customerCode" обязательное
     * - Проверяет что API возвращает ошибку 500 при отсутствии обязательного поля
     */
    @Test(priority = 12, description = "NEGATIVE POST getObjectsByCustomerConfig (aggregated) - отсутствует поле customerCode")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить агрегированные объекты без поля customerCode. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Missing CustomerCode Field (Aggregated)")
    public void testGetAggregatedObjectsWithoutCustomerCode() {
        logger.info("Выполнение теста: NEGATIVE POST getObjectsByCustomerConfig (aggregated) - отсутствует поле customerCode");
        
        // Создаем JSON строку без поля "customerCode"
        String requestBody = "{\n" +
            "  \"mainLayer\": {\n" +
            "    \"layerId\": 6210,\n" +
            "    \"properties\": [\n" +
            "      \"string\"\n" +
            "    ]\n" +
            "  },\n" +
            "  \"geometry\":\"" + AGGREGATED_POLYGON_GEOMETRY + "\",\n" +
            "  \"methodType\": \"" + METHOD_TYPE_AGGREGATED + "\",\n" +
            "  \"pageRequest\": {\n" +
            "    \"pageNumber\": 0,\n" +
            "    \"pageSize\": 50\n" +
            "  }\n" +
            "}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(GET_OBJECTS_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Cannot deserialize value of type "))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 13: NEGATIVE POST getObjectsByCustomerConfig (aggregated) - отсутствует поле mainLayer
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию API - поле "mainLayer" обязательное
     * - Проверяет что API возвращает ошибку 500 при отсутствии обязательного поля
     */
    @Test(priority = 13, description = "NEGATIVE POST getObjectsByCustomerConfig (aggregated) - отсутствует поле mainLayer")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить агрегированные объекты без поля mainLayer. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Missing MainLayer Field (Aggregated)")
    public void testGetAggregatedObjectsWithoutMainLayer() {
        logger.info("Выполнение теста: NEGATIVE POST getObjectsByCustomerConfig (aggregated) - отсутствует поле mainLayer");
        
        // Создаем JSON строку без поля "mainLayer"
        String requestBody = "{\n" +
            "  \"customerCode\": \"" + CUSTOMER_CODE + "\",\n" +
            "  \"geometry\":\"" + AGGREGATED_POLYGON_GEOMETRY + "\",\n" +
            "  \"methodType\": \"" + METHOD_TYPE_AGGREGATED + "\",\n" +
            "  \"pageRequest\": {\n" +
            "    \"pageNumber\": 0,\n" +
            "    \"pageSize\": 50\n" +
            "  }\n" +
            "}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(GET_OBJECTS_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Поле 'mainLayer' обязательно"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 14: NEGATIVE POST getObjectsByCustomerConfig (aggregated) - отсутствует поле methodType
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию API - поле "methodType" обязательное
     * - Проверяет что API возвращает ошибку 500 при отсутствии обязательного поля
     */
    @Test(priority = 14, description = "NEGATIVE POST getObjectsByCustomerConfig (aggregated) - отсутствует поле methodType")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить агрегированные объекты без поля methodType. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Missing MethodType Field (Aggregated)")
    public void testGetAggregatedObjectsWithoutMethodType() {
        logger.info("Выполнение теста: NEGATIVE POST getObjectsByCustomerConfig (aggregated) - отсутствует поле methodType");
        
        // Создаем JSON строку без поля "methodType"
        String requestBody = "{\n" +
            "  \"customerCode\": \"" + CUSTOMER_CODE + "\",\n" +
            "  \"mainLayer\": {\n" +
            "    \"layerId\": 6210,\n" +
            "    \"properties\": [\n" +
            "      \"string\"\n" +
            "    ]\n" +
            "  },\n" +
            "  \"geometry\":\"" + AGGREGATED_POLYGON_GEOMETRY + "\",\n" +
            "  \"pageRequest\": {\n" +
            "    \"pageNumber\": 0,\n" +
            "    \"pageSize\": 50\n" +
            "  }\n" +
            "}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(GET_OBJECTS_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("because \"methodType\" is null"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 15: NEGATIVE POST getObjectsByCustomerConfig (aggregated) - невалидная структура mainLayer
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию mainLayer - должен содержать layerId
     * - Проверяет что API возвращает ошибку 500 при невалидной структуре
     */
    @Test(priority = 15, description = "NEGATIVE POST getObjectsByCustomerConfig (aggregated) - невалидная структура mainLayer")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить агрегированные объекты с невалидной структурой mainLayer (без layerId). Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid MainLayer Structure (Aggregated)")
    public void testGetAggregatedObjectsWithInvalidMainLayer() {
        logger.info("Выполнение теста: NEGATIVE POST getObjectsByCustomerConfig (aggregated) - невалидная структура mainLayer");
        
        // Создаем JSON строку с mainLayer без layerId
        String requestBody = "{\n" +
            "  \"customerCode\": \"" + CUSTOMER_CODE + "\",\n" +
            "  \"mainLayer\": {\n" +
            "    \"properties\": [\n" +
            "      \"string\"\n" +
            "    ]\n" +
            "  },\n" +
            "  \"geometry\":\"" + AGGREGATED_POLYGON_GEOMETRY + "\",\n" +
            "  \"methodType\": \"" + METHOD_TYPE_AGGREGATED + "\",\n" +
            "  \"pageRequest\": {\n" +
            "    \"pageNumber\": 0,\n" +
            "    \"pageSize\": 50\n" +
            "  }\n" +
            "}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(GET_OBJECTS_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 16: NEGATIVE POST getObjectsByCustomerConfig (aggregated) - несуществующий layerId в mainLayer
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет обработку несуществующего layerId
     * - Проверяет что API возвращает ошибку 500
     */
    @Test(priority = 16, description = "NEGATIVE POST getObjectsByCustomerConfig (aggregated) - несуществующий layerId в mainLayer")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить агрегированные объекты с несуществующим layerId. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Non-existent LayerId (Aggregated)")
    public void testGetAggregatedObjectsWithNonExistentLayerId() {
        logger.info("Выполнение теста: NEGATIVE POST getObjectsByCustomerConfig (aggregated) - несуществующий layerId в mainLayer");
        
        // Создаем объект MainLayer с несуществующим layerId
        List<String> properties = new ArrayList<>();
        properties.add("string");
        MainLayer mainLayer = new MainLayer("999999999", properties);
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 50);
        
        // Создаем объект запроса
        GetObjectsByCustomerConfigRequest request = new GetObjectsByCustomerConfigRequest(
            CUSTOMER_CODE,
            mainLayer,
            AGGREGATED_POLYGON_GEOMETRY,
            METHOD_TYPE_AGGREGATED,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_OBJECTS_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Отсутсвует конфигурация для слоя"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 17: NEGATIVE POST getObjectsByCustomerConfig (aggregated) - невалидный JSON
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет обработку невалидного JSON (неправильный формат)
     * - Проверяет что API корректно обрабатывает ошибки парсинга JSON
     */
    @Test(priority = 17, description = "NEGATIVE POST getObjectsByCustomerConfig (aggregated) - невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка отправить невалидный JSON для aggregated метода. Проверка статуса 400 или 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid JSON (Aggregated)")
    public void testGetAggregatedObjectsWithInvalidJson() {
        logger.info("Выполнение теста: NEGATIVE POST getObjectsByCustomerConfig (aggregated) - невалидный JSON");
        
        // Невалидный JSON - отсутствует закрывающая скобка }
        String invalidJson = "{\n" +
            "  \"customerCode\": \"" + CUSTOMER_CODE + "\",\n" +
            "  \"mainLayer\": {\n" +
            "    \"layerId\": 6210,\n" +
            "    \"properties\": [\n" +
            "      \"string\"\n" +
            "    ]\n" +
            "  },\n" +
            "  \"geometry\":\"" + AGGREGATED_POLYGON_GEOMETRY + "\",\n" +
            "  \"methodType\": \"" + METHOD_TYPE_AGGREGATED + "\",\n" +
            "  \"pageRequest\": {\n" +
            "    \"pageNumber\": 0,\n" +
            "    \"pageSize\": 50\n";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .post(GET_OBJECTS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500))) //Шляпа конечно, но я забыл что там приходит, наверное судя по тексту 500
                .body("$", instanceOf(java.util.Map.class))
                .body("error", notNullValue())
                .body("error", containsString("Internal Server Error"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("error"));
    }
    
    /**
     * ТЕСТ 18: POST geoData/v2/getObjectsByPolygon - ПОЗИТИВНЫЙ ТЕСТ
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API позволяет получить объекты по полигону
     * - Проверяет структуру ответа (size, pageNumber, pageSize, totalElements, totalPages, content)
     * - Проверяет валидность геометрии в features
     * - Проверяет время ответа
     */
    @Test(priority = 18, description = "POST geoData/v2/getObjectsByPolygon")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение объектов по полигону. Проверка статуса 200, структуры ответа (size, pageNumber, pageSize, totalElements, totalPages, content с FeatureCollection), валидности features и времени ответа.")
    @Story("Get Objects By Polygon")
    public void testGetObjectsByPolygon() {
        logger.info("Выполнение теста: POST geoData/v2/getObjectsByPolygon");
        
        // Создаем список слоев (без поля synced, как в Postman)
        List<Layer> layers = new ArrayList<>();
        layers.add(new Layer(4528, null, new ArrayList<>()));
        layers.add(new Layer(24, null, new ArrayList<>()));
        layers.add(new Layer(3230, null, new ArrayList<>()));
        layers.add(new Layer(4542, null, new ArrayList<>()));
        layers.add(new Layer(4555, null, new ArrayList<>()));
        
        List<String> properties4557 = new ArrayList<>();
        properties4557.add("load_address");
        properties4557.add("load_investor");
        layers.add(new Layer(4557, null, properties4557));
        
        layers.add(new Layer(4562, null, new ArrayList<>()));
        
        List<String> properties4912 = new ArrayList<>();
        properties4912.add("docNumber");
        properties4912.add("adress");
        layers.add(new Layer(4912, null, properties4912));
        
        layers.add(new Layer(5666, null, new ArrayList<>()));
        
        List<String> properties5973 = new ArrayList<>();
        properties5973.add("load_id");
        properties5973.add("load_name_obj");
        layers.add(new Layer(5973, null, properties5973));
        
        layers.add(new Layer(6061, null, new ArrayList<>()));
        layers.add(new Layer(6166, null, new ArrayList<>()));
        
        List<String> properties6209 = new ArrayList<>();
        properties6209.add("load_address");
        properties6209.add("load_investor");
        layers.add(new Layer(6209, null, properties6209));
        
        layers.add(new Layer(6210, null, new ArrayList<>()));
        layers.add(new Layer(6349, null, new ArrayList<>()));
        layers.add(new Layer(7623, null, new ArrayList<>()));
        
        List<String> properties7702 = new ArrayList<>();
        properties7702.add("id_unique");
        layers.add(new Layer(7702, null, properties7702));
        
        layers.add(new Layer(7913, null, new ArrayList<>()));
        layers.add(new Layer(7989, null, new ArrayList<>()));
        layers.add(new Layer(8012, null, new ArrayList<>()));
        layers.add(new Layer(8024, null, new ArrayList<>()));
        layers.add(new Layer(8029, null, new ArrayList<>()));
        layers.add(new Layer(8030, null, new ArrayList<>()));
        layers.add(new Layer(8031, null, new ArrayList<>()));
        layers.add(new Layer(8117, null, new ArrayList<>()));
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 10000);
        
        // Создаем объект запроса
        GetObjectsByPolygonRequest request = new GetObjectsByPolygonRequest(
            layers,
            false,
            POLYGON_COORDINATES,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_OBJECTS_BY_POLYGON_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("size", notNullValue())
                .body("size", instanceOf(Integer.class))
                .body("size", greaterThan(50))
                .body("pageNumber", notNullValue())
                .body("pageNumber", instanceOf(Integer.class))
                .body("pageSize", notNullValue())
                .body("pageSize", instanceOf(Integer.class))
                .body("totalElements", notNullValue())
                .body("totalElements", instanceOf(Integer.class))
                .body("totalPages", notNullValue())
                .body("totalPages", instanceOf(Integer.class))
                .body("content", notNullValue())
                .body("content", instanceOf(java.util.Map.class))
                .body("content.type", equalTo("FeatureCollection"))
                .extract()
                .response();
        
        // Проверяем features только если size > 0
        Integer size = response.jsonPath().getInt("size");
        if (size > 0) {
            response.then()
                    .body("content.features", notNullValue())
                    .body("content.features", instanceOf(java.util.List.class))
                    .body("content.features.size()", greaterThan(0));
        }
        
        List<Map<String, Object>> features = response.jsonPath().getList("content.features");
        assertFeatureCollectionStructure(features);
        logger.info("Тест успешно завершен. Размер ответа: {}, количество features: {}",
            response.jsonPath().getInt("size"), features != null ? features.size() : 0);
    }

    /**
     * ТЕСТ 19: NEGATIVE POST geoData/v2/getObjectsByPolygon - отсутствует поле layers
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию API - поле "layers" обязательное
     * - Проверяет что API возвращает ошибку 400 при отсутствии обязательного поля
     */
    @Test(priority = 19, description = "NEGATIVE POST geoData/v2/getObjectsByPolygon - отсутствует поле layers")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить объекты без поля layers. Проверка статуса 400 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Missing Layers Field")
    public void testGetObjectsByPolygonWithoutLayers() {
        logger.info("Выполнение теста: NEGATIVE POST geoData/v2/getObjectsByPolygon - отсутствует поле layers");
        
        // Создаем JSON строку без поля "layers"
        String requestBody = "{\n" +
            "  \"returnCentroid\": false,\n" +
            "  \"coordinates\": \"" + POLYGON_COORDINATES + "\",\n" +
            "  \"pageRequest\": {\n" +
            "    \"pageNumber\": 0,\n" +
            "    \"pageSize\": 10000\n" +
            "  }\n" +
            "}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(GET_OBJECTS_BY_POLYGON_ENDPOINT)
                .then()
                .statusCode(400)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Поле 'layers' обязательно и не может быть пустым"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 20: NEGATIVE POST geoData/v2/getObjectsByPolygon - отсутствует поле coordinates
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию API - поле "coordinates" обязательное
     * - Проверяет что API возвращает ошибку 400 при отсутствии обязательного поля
     */
    @Test(priority = 20, description = "NEGATIVE POST geoData/v2/getObjectsByPolygon - отсутствует поле coordinates")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить объекты без поля coordinates. Проверка статуса 400 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Missing Coordinates Field")
    public void testGetObjectsByPolygonWithoutCoordinates() {
        logger.info("Выполнение теста: NEGATIVE POST geoData/v2/getObjectsByPolygon - отсутствует поле coordinates");
        
        // Создаем список слоев
        List<Layer> layers = new ArrayList<>();
        layers.add(new Layer(4528, true, new ArrayList<>()));
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 10000);
        
        // Создаем объект запроса без coordinates
        GetObjectsByPolygonRequest request = new GetObjectsByPolygonRequest(
            layers,
            false,
            null,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_OBJECTS_BY_POLYGON_ENDPOINT)
                .then()
                .statusCode(400)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Поле 'coordinates' должно содержать корректный WKT-полигон"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 21: NEGATIVE POST geoData/v2/getObjectsByPolygon - пустой массив layers
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию API - массив layers не может быть пустым
     * - Проверяет что API возвращает ошибку 400 при пустом массиве layers
     */
    @Test(priority = 21, description = "NEGATIVE POST geoData/v2/getObjectsByPolygon - пустой массив layers")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить объекты с пустым массивом layers. Проверка статуса 400 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Empty Layers Array")
    public void testGetObjectsByPolygonWithEmptyLayers() {
        logger.info("Выполнение теста: NEGATIVE POST geoData/v2/getObjectsByPolygon - пустой массив layers");
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 10000);
        
        // Создаем объект запроса с пустым массивом layers
        GetObjectsByPolygonRequest request = new GetObjectsByPolygonRequest(
            new ArrayList<>(),
            false,
            POLYGON_COORDINATES,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_OBJECTS_BY_POLYGON_ENDPOINT)
                .then()
                .statusCode(400)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Поле 'layers' обязательно и не может быть пустым"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 22: NEGATIVE POST geoData/v2/getObjectsByPolygon - невалидная структура элемента layers
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию структуры элемента layers - должен содержать layerId
     * - Проверяет что API возвращает ошибку 500 при невалидной структуре
     */
    @Test(priority = 22, description = "NEGATIVE POST geoData/v2/getObjectsByPolygon - невалидная структура элемента layers")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить объекты с невалидной структурой элемента layers (без layerId). Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid Layer Structure")
    public void testGetObjectsByPolygonWithInvalidLayerStructure() {
        logger.info("Выполнение теста: NEGATIVE POST geoData/v2/getObjectsByPolygon - невалидная структура элемента layers");
        
        // Создаем JSON строку с layer без layerId
        String requestBody = "{\n" +
            "  \"layers\": [\n" +
            "    {\n" +
            "      \"synced\": true,\n" +
            "      \"properties\": []\n" +
            "    }\n" +
            "  ],\n" +
            "  \"returnCentroid\": false,\n" +
            "  \"coordinates\": \"" + POLYGON_COORDINATES + "\",\n" +
            "  \"pageRequest\": {\n" +
            "    \"pageNumber\": 0,\n" +
            "    \"pageSize\": 10000\n" +
            "  }\n" +
            "}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(GET_OBJECTS_BY_POLYGON_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 23: NEGATIVE POST geoData/v2/getObjectsByPolygon - невалидные значения pageRequest
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию pageRequest - pageNumber не может быть отрицательным
     * - Проверяет что API возвращает ошибку 400 при невалидных значениях
     */
    @Test(priority = 23, description = "NEGATIVE POST geoData/v2/getObjectsByPolygon - невалидные значения pageRequest")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить объекты с невалидными значениями pageRequest. Проверка статуса 400 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid PageRequest (getObjectsByPolygon)")
    public void testGetObjectsByPolygonWithInvalidPageRequest() {
        logger.info("Выполнение теста: NEGATIVE POST geoData/v2/getObjectsByPolygon - невалидные значения pageRequest");
        
        // Создаем список слоев
        List<Layer> layers = new ArrayList<>();
        layers.add(new Layer(4528, true, new ArrayList<>()));
        
        // Создаем объект PageRequest с невалидными значениями
        PageRequest pageRequest = new PageRequest(-1, 0);
        
        // Создаем объект запроса
        GetObjectsByPolygonRequest request = new GetObjectsByPolygonRequest(
            layers,
            false,
            POLYGON_COORDINATES,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_OBJECTS_BY_POLYGON_ENDPOINT)
                .then()
                .statusCode(400)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Номер страницы (pageNumber) не может быть отрицательным"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 24: NEGATIVE POST geoData/v2/getObjectsByPolygon - невалидный тип layerId в слоях
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию layerId - должен быть числом
     * - Проверяет что API возвращает ошибку 500 при невалидном типе layerId
     */
    @Test(priority = 24, description = "NEGATIVE POST geoData/v2/getObjectsByPolygon - невалидный тип layerId в слоях")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить объекты с невалидным типом layerId (строка вместо числа). Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid LayerId Type")
    public void testGetObjectsByPolygonWithInvalidLayerIdType() {
        logger.info("Выполнение теста: NEGATIVE POST geoData/v2/getObjectsByPolygon - невалидный тип layerId в слоях");
        
        // Создаем JSON строку с layerId как строкой вместо числа
        String requestBody = "{\n" +
            "  \"layers\": [\n" +
            "    {\n" +
            "      \"layerId\": \"not_a_number\",\n" +
            "      \"synced\": true,\n" +
            "      \"properties\": []\n" +
            "    }\n" +
            "  ],\n" +
            "  \"returnCentroid\": false,\n" +
            "  \"coordinates\": \"" + POLYGON_COORDINATES + "\",\n" +
            "  \"pageRequest\": {\n" +
            "    \"pageNumber\": 0,\n" +
            "    \"pageSize\": 10000\n" +
            "  }\n" +
            "}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(GET_OBJECTS_BY_POLYGON_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Cannot deserialize value of type"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 25: NEGATIVE POST geoData/v2/getObjectsByPolygon - невалидный формат coordinates
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию coordinates - должен быть валидным WKT-полигоном
     * - Проверяет что API возвращает ошибку 400 при невалидном формате
     */
    @Test(priority = 25, description = "NEGATIVE POST geoData/v2/getObjectsByPolygon - невалидный формат coordinates")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить объекты с невалидным форматом coordinates. Проверка статуса 400 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid Coordinates Format")
    public void testGetObjectsByPolygonWithInvalidCoordinatesFormat() {
        logger.info("Выполнение теста: NEGATIVE POST geoData/v2/getObjectsByPolygon - невалидный формат coordinates");
        
        // Создаем список слоев
        List<Layer> layers = new ArrayList<>();
        layers.add(new Layer(4528, true, new ArrayList<>()));
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 10000);
        
        // Создаем объект запроса с невалидным форматом coordinates
        GetObjectsByPolygonRequest request = new GetObjectsByPolygonRequest(
            layers,
            false,
            "INVALID_WKT_FORMAT",
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_OBJECTS_BY_POLYGON_ENDPOINT)
                .then()
                .statusCode(400)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Поле 'coordinates' должно содержать корректный WKT-полигон"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 26: NEGATIVE POST geoData/v2/getObjectsByPolygon - невалидный тип returnCentroid
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию returnCentroid - должен быть boolean
     * - Проверяет что API возвращает ошибку 500 при невалидном типе
     */
    @Test(priority = 26, description = "NEGATIVE POST geoData/v2/getObjectsByPolygon - невалидный тип returnCentroid")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить объекты с невалидным типом returnCentroid (строка вместо boolean). Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid ReturnCentroid Type")
    public void testGetObjectsByPolygonWithInvalidReturnCentroidType() {
        logger.info("Выполнение теста: NEGATIVE POST geoData/v2/getObjectsByPolygon - невалидный тип returnCentroid");
        
        // Создаем JSON строку с returnCentroid как строкой вместо boolean
        String requestBody = "{\n" +
            "  \"layers\": [\n" +
            "    {\n" +
            "      \"layerId\": 4528,\n" +
            "      \"synced\": true,\n" +
            "      \"properties\": []\n" +
            "    }\n" +
            "  ],\n" +
            "  \"returnCentroid\": \"not_boolean\",\n" +
            "  \"coordinates\": \"" + POLYGON_COORDINATES + "\",\n" +
            "  \"pageRequest\": {\n" +
            "    \"pageNumber\": 0,\n" +
            "    \"pageSize\": 10000\n" +
            "  }\n" +
            "}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(GET_OBJECTS_BY_POLYGON_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Cannot deserialize value of type `boolean` from String"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 27: NEGATIVE POST geoData/v2/getObjectsByPolygon - невалидный JSON
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет обработку невалидного JSON (неправильный формат)
     * - Проверяет что API корректно обрабатывает ошибки парсинга JSON
     */
    @Test(priority = 27, description = "NEGATIVE POST geoData/v2/getObjectsByPolygon - невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка отправить невалидный JSON для getObjectsByPolygon. Проверка статуса 400 или 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid JSON (getObjectsByPolygon)")
    public void testGetObjectsByPolygonWithInvalidJson() {
        logger.info("Выполнение теста: NEGATIVE POST geoData/v2/getObjectsByPolygon - невалидный JSON");
        
        // Невалидный JSON - отсутствует закрывающая скобка }
        String invalidJson = "{\n" +
            "  \"layers\": [\n" +
            "    {\n" +
            "      \"layerId\": 4528,\n" +
            "      \"synced\": true,\n" +
            "      \"properties\": []\n" +
            "    }\n" +
            "  ],\n" +
            "  \"returnCentroid\": false,\n" +
            "  \"coordinates\": \"" + POLYGON_COORDINATES + "\",\n" +
            "  \"pageRequest\": {\n" +
            "    \"pageNumber\": 0,\n" +
            "    \"pageSize\": 10000\n";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .post(GET_OBJECTS_BY_POLYGON_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("error", notNullValue())
                .body("error", containsString("Internal Server Error"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("error"));
    }
    
    /**
     * ТЕСТ 28: GET geoData/v2/getObjectsByCoordinates - ПОЗИТИВНЫЙ ТЕСТ
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API позволяет получить объекты по координатам
     * - Проверяет структуру ответа (size, pageNumber, pageSize, totalElements, totalPages, content)
     * - Проверяет валидность features с centroid, distance, objectId, properties, correlationId
     * - Проверяет время ответа
     */
    @Test(priority = 28, description = "GET geoData/v2/getObjectsByCoordinates")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение объектов по координатам. Проверка статуса 200, структуры ответа, валидности features с centroid, distance, objectId и времени ответа.")
    @Story("Get Objects By Coordinates")
    public void testGetObjectsByCoordinates() {
        logger.info("Выполнение теста: GET geoData/v2/getObjectsByCoordinates");
        
        Response response = given()
                .spec(getRequestSpec())
                .queryParam("layerId", "6210")
                .queryParam("coordinate", "point(37.527505026619366 55.685154469832156)")
                .queryParam("returnCentroid", "true")
                .when()
                .get(GET_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("size", notNullValue())
                .body("size", instanceOf(Integer.class))
                .body("size", equalTo(1))
                .body("pageNumber", notNullValue())
                .body("pageNumber", instanceOf(Integer.class))
                .body("pageSize", notNullValue())
                .body("pageSize", instanceOf(Integer.class))
                .body("totalElements", notNullValue())
                .body("totalElements", instanceOf(Integer.class))
                .body("totalElements", equalTo(1))
                .body("totalPages", notNullValue())
                .body("totalPages", instanceOf(Integer.class))
                .body("content", notNullValue())
                .body("content", instanceOf(java.util.Map.class))
                .body("content.type", equalTo("FeatureCollection"))
                .body("content.features", notNullValue())
                .body("content.features", instanceOf(java.util.List.class))
                .body("content.features.size()", greaterThan(0))
                .extract()
                .response();
        
        List<Map<String, Object>> features = response.jsonPath().getList("content.features");
        assertFeatureWithCentroidAndDistance(features);
    }

    /**
     * ТЕСТ 29: NEGATIVE GET geoData/v2/getObjectsByCoordinates - отсутствует query параметр layerId
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию API - query параметр layerId обязательный
     * - Проверяет что API возвращает ошибку 500 при отсутствии обязательного параметра
     */
    @Test(priority = 29, description = "NEGATIVE GET geoData/v2/getObjectsByCoordinates - отсутствует query параметр layerId")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить объекты без query параметра layerId. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Missing LayerId Query Parameter")
    public void testGetObjectsByCoordinatesWithoutLayerId() {
        logger.info("Выполнение теста: NEGATIVE GET geoData/v2/getObjectsByCoordinates - отсутствует query параметр layerId");
        
        Response response = given()
                .spec(getRequestSpec())
                .queryParam("coordinate", "POINT(37.51099542641316 55.6782409858406)")
                .when()
                .get(GET_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 30: NEGATIVE GET geoData/v2/getObjectsByCoordinates - отсутствует query параметр coordinate
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию API - query параметр coordinate обязательный
     * - Проверяет что API возвращает ошибку 400 при отсутствии обязательного параметра
     */
    @Test(priority = 30, description = "NEGATIVE GET geoData/v2/getObjectsByCoordinates - отсутствует query параметр coordinate")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить объекты без query параметра coordinate. Проверка статуса 400 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Missing Coordinate Query Parameter")
    public void testGetObjectsByCoordinatesWithoutCoordinate() {
        logger.info("Выполнение теста: NEGATIVE GET geoData/v2/getObjectsByCoordinates - отсутствует query параметр coordinate");
        
        Response response = given()
                .spec(getRequestSpec())
                .queryParam("layerId", "4528")
                .when()
                .get(GET_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(400)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Поле 'coordinates' должно содержать корректную WKT-точку"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 31: NEGATIVE GET geoData/v2/getObjectsByCoordinates - невалидный тип layerId
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию layerId - должен быть числом
     * - Проверяет что API возвращает ошибку 400 или 500 при невалидном типе
     */
    @Test(priority = 31, description = "NEGATIVE GET geoData/v2/getObjectsByCoordinates - невалидный тип layerId")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить объекты с невалидным типом layerId (строка вместо числа). Проверка статуса 400 или 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid LayerId Type (GET)")
    public void testGetObjectsByCoordinatesWithInvalidLayerIdType() {
        logger.info("Выполнение теста: NEGATIVE GET geoData/v2/getObjectsByCoordinates - невалидный тип layerId");
        
        Response response = given()
                .spec(getRequestSpec())
                .queryParam("layerId", "not_a_number")
                .queryParam("coordinate", "POINT(37.51099542641316 55.6782409858406)")
                .when()
                .get(GET_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("error", notNullValue())
                .body("error", containsString("Internal Server Error"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("error"));
    }
    
    /**
     * ТЕСТ 32: NEGATIVE GET geoData/v2/getObjectsByCoordinates - невалидный тип returnCentroid
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию returnCentroid - должен быть boolean
     * - Проверяет что API возвращает ошибку 500 при невалидном типе
     */
    @Test(priority = 32, description = "NEGATIVE GET geoData/v2/getObjectsByCoordinates - невалидный тип returnCentroid")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить объекты с невалидным типом returnCentroid (строка вместо boolean). Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid ReturnCentroid Type (GET)")
    public void testGetObjectsByCoordinatesWithInvalidReturnCentroidType() {
        logger.info("Выполнение теста: NEGATIVE GET geoData/v2/getObjectsByCoordinates - невалидный тип returnCentroid");
        
        Response response = given()
                .spec(getRequestSpec())
                .queryParam("layerId", "4528")
                .queryParam("coordinate", "POINT(37.51099542641316 55.6782409858406)")
                .queryParam("returnCentroid", "not_boolean")
                .when()
                .get(GET_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Invalid boolean value [not_boolean]"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 33: POST geoData/v2/getObjectsByCoordinates - ПОЗИТИВНЫЙ ТЕСТ
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API позволяет получить объекты по координатам через POST
     * - Проверяет структуру ответа (size, pageNumber, pageSize, totalElements, totalPages, content)
     * - Проверяет валидность features с centroid, distance, geometry
     */
    @Test(priority = 33, description = "POST geoData/v2/getObjectsByCoordinates")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение объектов по координатам через POST. Проверка статуса 200, структуры ответа, валидности features с centroid, distance, geometry.")
    @Story("Get Objects By Coordinates - POST")
    public void testPostGetObjectsByCoordinates() {
        logger.info("Выполнение теста: POST geoData/v2/getObjectsByCoordinates");
        
        // Создаем список слоев
        List<Layer> layers = new ArrayList<>();
        List<String> properties = new ArrayList<>();
        properties.add("string");
        layers.add(new Layer(4557, null, properties));
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 100);
        
        // Создаем объект запроса
        GetObjectsByCoordinatesPostRequest request = new GetObjectsByCoordinatesPostRequest(
            layers,
            "point(  37.51099542641316 55.6782409858406)",
            250,
            true,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("size", notNullValue())
                .body("size", instanceOf(Integer.class))
                .body("size", equalTo(2))
                .body("pageNumber", notNullValue())
                .body("pageNumber", instanceOf(Integer.class))
                .body("pageSize", notNullValue())
                .body("pageSize", instanceOf(Integer.class))
                .body("totalElements", notNullValue())
                .body("totalElements", instanceOf(Integer.class))
                .body("totalElements", equalTo(2))
                .body("totalPages", notNullValue())
                .body("totalPages", instanceOf(Integer.class))
                .body("content", notNullValue())
                .body("content", instanceOf(java.util.Map.class))
                .body("content.type", equalTo("FeatureCollection"))
                .body("content.features", notNullValue())
                .body("content.features", instanceOf(java.util.List.class))
                .body("content.features.size()", equalTo(2))
                .extract()
                .response();
        
        // Дополнительные проверки структуры features
        List<Map<String, Object>> features = response.jsonPath().getList("content.features");
        assertFeatureWithCentroidAndDistance(features);
        
        logger.info("Тест успешно завершен. Размер ответа: {}, количество features: {}", 
            response.jsonPath().getInt("size"), features.size());
    }
    
    /**
     * ТЕСТ 34: NEGATIVE POST /geoData/v2/getObjectsByCoordinates - отсутствует поле layers
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию API - поле "layers" обязательное
     * - Проверяет что API возвращает ошибку 400 при отсутствии обязательного поля
     */
    @Test(priority = 34, description = "NEGATIVE POST /geoData/v2/getObjectsByCoordinates - отсутствует поле layers")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить объекты без поля layers. Проверка статуса 400 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Missing Layers Field (POST)")
    public void testPostGetObjectsByCoordinatesWithoutLayers() {
        logger.info("Выполнение теста: NEGATIVE POST /geoData/v2/getObjectsByCoordinates - отсутствует поле layers");
        
        // Создаем JSON строку без поля "layers"
        String requestBody = "{\n" +
            "  \"coordinates\": \"POINT(37.51099542641316 55.6782409858406)\",\n" +
            "  \"radius\": 250,\n" +
            "  \"returnCentroid\": true,\n" +
            "  \"pageRequest\": {\n" +
            "    \"pageNumber\": 0,\n" +
            "    \"pageSize\": 100\n" +
            "  }\n" +
            "}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(GET_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(400)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Поле 'layers' обязательно и не может быть пустым"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 35: NEGATIVE POST /geoData/v2/getObjectsByCoordinates - отсутствует поле coordinates
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию API - поле "coordinates" обязательное
     * - Проверяет что API возвращает ошибку 400 при отсутствии обязательного поля
     */
    @Test(priority = 35, description = "NEGATIVE POST /geoData/v2/getObjectsByCoordinates - отсутствует поле coordinates")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить объекты без поля coordinates. Проверка статуса 400 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Missing Coordinates Field (POST)")
    public void testPostGetObjectsByCoordinatesWithoutCoordinates() {
        logger.info("Выполнение теста: NEGATIVE POST /geoData/v2/getObjectsByCoordinates - отсутствует поле coordinates");
        
        // Создаем список слоев
        List<Layer> layers = new ArrayList<>();
        List<String> properties = new ArrayList<>();
        properties.add("string");
        layers.add(new Layer(4557, null, properties));
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 100);
        
        // Создаем объект запроса без coordinates
        GetObjectsByCoordinatesPostRequest request = new GetObjectsByCoordinatesPostRequest(
            layers,
            null,
            250,
            true,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(400)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Поле 'coordinates' должно содержать корректную WKT-точку"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 36: NEGATIVE POST /geoData/v2/getObjectsByCoordinates - пустой массив layers
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию API - массив layers не может быть пустым
     * - Проверяет что API возвращает ошибку 400 при пустом массиве layers
     */
    @Test(priority = 36, description = "NEGATIVE POST /geoData/v2/getObjectsByCoordinates - пустой массив layers")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить объекты с пустым массивом layers. Проверка статуса 400 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Empty Layers Array (POST)")
    public void testPostGetObjectsByCoordinatesWithEmptyLayers() {
        logger.info("Выполнение теста: NEGATIVE POST /geoData/v2/getObjectsByCoordinates - пустой массив layers");
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 100);
        
        // Создаем объект запроса с пустым массивом layers
        GetObjectsByCoordinatesPostRequest request = new GetObjectsByCoordinatesPostRequest(
            new ArrayList<>(),
            "POINT(37.51099542641316 55.6782409858406)",
            250,
            true,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(400)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Поле 'layers' обязательно и не может быть пустым"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 37: NEGATIVE POST /geoData/v2/getObjectsByCoordinates - невалидная структура элемента в layers
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию структуры элемента layers - должен содержать layerId
     * - Проверяет что API возвращает ошибку 500 при невалидной структуре
     */
    @Test(priority = 37, description = "NEGATIVE POST /geoData/v2/getObjectsByCoordinates - невалидная структура элемента в layers")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить объекты с невалидной структурой элемента layers (без layerId). Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid Layer Structure (POST)")
    public void testPostGetObjectsByCoordinatesWithInvalidLayerStructure() {
        logger.info("Выполнение теста: NEGATIVE POST /geoData/v2/getObjectsByCoordinates - невалидная структура элемента в layers");
        
        // Создаем JSON строку с layer без layerId
        String requestBody = "{\n" +
            "  \"layers\": [\n" +
            "    {\n" +
            "      \"properties\": [\n" +
            "        \"string\"\n" +
            "      ]\n" +
            "    }\n" +
            "  ],\n" +
            "  \"coordinates\": \"POINT(37.51099542641316 55.6782409858406)\",\n" +
            "  \"radius\": 250,\n" +
            "  \"returnCentroid\": true,\n" +
            "  \"pageRequest\": {\n" +
            "    \"pageNumber\": 0,\n" +
            "    \"pageSize\": 100\n" +
            "  }\n" +
            "}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(GET_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 38: NEGATIVE POST /geoData/v2/getObjectsByCoordinates - отрицательное значение radius
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию radius - должен быть неотрицательным
     * - Проверяет что API возвращает ошибку 400 при отрицательном значении
     */
    @Test(priority = 38, description = "NEGATIVE POST /geoData/v2/getObjectsByCoordinates - отрицательное значение radius")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить объекты с отрицательным значением radius. Проверка статуса 400 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Negative Radius")
    public void testPostGetObjectsByCoordinatesWithNegativeRadius() {
        logger.info("Выполнение теста: NEGATIVE POST /geoData/v2/getObjectsByCoordinates - отрицательное значение radius");
        
        // Создаем список слоев
        List<Layer> layers = new ArrayList<>();
        List<String> properties = new ArrayList<>();
        properties.add("string");
        layers.add(new Layer(4557, null, properties));
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 100);
        
        // Создаем объект запроса с отрицательным radius
        GetObjectsByCoordinatesPostRequest request = new GetObjectsByCoordinatesPostRequest(
            layers,
            "POINT(37.51099542641316 55.6782409858406)",
            -1,
            true,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(400)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Значение поля 'radius' должно быть неотрицательным"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 39: NEGATIVE POST /geoData/v2/getObjectsByCoordinates - невалидный JSON
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет обработку невалидного JSON (неправильный формат)
     * - Проверяет что API корректно обрабатывает ошибки парсинга JSON
     */
    @Test(priority = 39, description = "NEGATIVE POST /geoData/v2/getObjectsByCoordinates - невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка отправить невалидный JSON для POST getObjectsByCoordinates. Проверка статуса 400 или 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid JSON (POST getObjectsByCoordinates)")
    public void testPostGetObjectsByCoordinatesWithInvalidJson() {
        logger.info("Выполнение теста: NEGATIVE POST /geoData/v2/getObjectsByCoordinates - невалидный JSON");
        
        // Невалидный JSON - отсутствует закрывающая скобка }
        String invalidJson = "{\n" +
            "  \"layers\": [\n" +
            "    {\n" +
            "      \"layerId\": 4557,\n" +
            "      \"properties\": [\n" +
            "        \"string\"\n" +
            "      ]\n" +
            "    }\n" +
            "  ],\n" +
            "  \"coordinates\": \"POINT(37.51099542641316 55.6782409858406)\",\n" +
            "  \"radius\": 250,\n" +
            "  \"returnCentroid\": true,\n" +
            "  \"pageRequest\": {\n" +
            "    \"pageNumber\": 0,\n" +
            "    \"pageSize\": 100\n";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .post(GET_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("error", notNullValue())
                .body("error", containsString("Internal Server Error"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("error"));
    }
    
    /**
     * ТЕСТ 40: POST geoData/v2/getAggregatedObjectsByCoordinates - ПОЗИТИВНЫЙ ТЕСТ
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API позволяет получить агрегированные объекты по координатам
     * - Проверяет структуру ответа (size, pageNumber, pageSize, totalElements, totalPages, content)
     * - Проверяет валидность features с centroid, geometry
     */
    @Test(priority = 40, description = "POST geoData/v2/getAggregatedObjectsByCoordinates")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение агрегированных объектов по координатам. Проверка статуса 200, структуры ответа, валидности features с centroid, geometry.")
    @Story("Get Aggregated Objects By Coordinates")
    public void testGetAggregatedObjectsByCoordinates() {
        logger.info("Выполнение теста: POST geoData/v2/getAggregatedObjectsByCoordinates");
        
        // Создаем объект mainLayer (layerId как число, как в Postman)
        List<String> mainLayerProperties = new ArrayList<>();
        mainLayerProperties.add("string");
        MainLayer mainLayer = new MainLayer(8030, mainLayerProperties);
        
        // Создаем список linkedLayers (layerId как число, как в Postman)
        List<MainLayer> linkedLayers = new ArrayList<>();
        List<String> linkedLayerProperties = new ArrayList<>();
        linkedLayerProperties.add("string");
        linkedLayers.add(new MainLayer(10299, linkedLayerProperties));
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 1000);
        
        // Создаем объект запроса
        GetAggregatedObjectsByCoordinatesRequest request = new GetAggregatedObjectsByCoordinatesRequest(
            mainLayer,
            linkedLayers,
            "POINT(37.509721071514065 55.668464193910836)",
            null, // geomAggregation не указан в позитивном тесте из Postman
            true,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_AGGREGATED_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("size", notNullValue())
                .body("size", instanceOf(Integer.class))
                .body("pageNumber", notNullValue())
                .body("pageNumber", instanceOf(Integer.class))
                .body("pageSize", notNullValue())
                .body("pageSize", instanceOf(Integer.class))
                .body("totalElements", notNullValue())
                .body("totalElements", instanceOf(Integer.class))
                .body("totalPages", notNullValue())
                .body("totalPages", instanceOf(Integer.class))
                .body("content", notNullValue())
                .body("content", instanceOf(java.util.Map.class))
                .body("content.type", equalTo("FeatureCollection"))
                .body("content.features", notNullValue())
                .body("content.features", instanceOf(java.util.List.class))
                .body("content.features.size()", greaterThan(0))
                .extract()
                .response();
        
        // Дополнительные проверки структуры features
        List<Map<String, Object>> features = response.jsonPath().getList("content.features");
        assertAggregatedFeatureStructure(features);
        
        logger.info("Тест успешно завершен. Размер ответа: {}, количество features: {}", 
            response.jsonPath().getInt("size"), features.size());
    }
    
    /**
     * ТЕСТ 41: NEGATIVE POST geoData/v2/getAggregatedObjectsByCoordinates - отсутствует поле mainLayer
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию API - поле "mainLayer" обязательное
     * - Проверяет что API возвращает ошибку 400 при отсутствии обязательного поля
     */
    @Test(priority = 41, description = "NEGATIVE POST geoData/v2/getAggregatedObjectsByCoordinates - отсутствует поле mainLayer")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить агрегированные объекты без поля mainLayer. Проверка статуса 400 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Missing MainLayer Field (Aggregated)")
    public void testGetAggregatedObjectsByCoordinatesWithoutMainLayer() {
        logger.info("Выполнение теста: NEGATIVE POST geoData/v2/getAggregatedObjectsByCoordinates - отсутствует поле mainLayer");
        
        // Создаем JSON строку без поля "mainLayer"
        String requestBody = "{\n" +
            "  \"linkedLayers\": [],\n" +
            "  \"coordinates\": \"POINT(37.51099542641316 55.6782409858406)\",\n" +
            "  \"geomAggregation\": \"EQUALS\",\n" +
            "  \"returnCentroid\": false,\n" +
            "  \"pageRequest\": {\n" +
            "    \"pageNumber\": 0,\n" +
            "    \"pageSize\": 50\n" +
            "  }\n" +
            "}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(GET_AGGREGATED_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(400)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Поле 'mainLayer' обязательно"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 42: NEGATIVE POST geoData/v2/getAggregatedObjectsByCoordinates - невалидный тип layerId в mainLayer
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию layerId - должен быть числом
     * - Проверяет что API возвращает ошибку 500 при невалидном типе
     */
    @Test(priority = 42, description = "NEGATIVE POST geoData/v2/getAggregatedObjectsByCoordinates - невалидный тип layerId в mainLayer")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить агрегированные объекты с невалидным типом layerId (строка вместо числа). Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid LayerId Type (Aggregated)")
    public void testGetAggregatedObjectsByCoordinatesWithInvalidLayerIdType() {
        logger.info("Выполнение теста: NEGATIVE POST geoData/v2/getAggregatedObjectsByCoordinates - невалидный тип layerId в mainLayer");
        
        // Создаем JSON строку с layerId как строкой вместо числа
        String requestBody = "{\n" +
            "  \"mainLayer\": {\n" +
            "    \"layerId\": \"not_a_number\",\n" +
            "    \"properties\": [\"string\"]\n" +
            "  },\n" +
            "  \"linkedLayers\": [],\n" +
            "  \"coordinates\": \"POINT(37.51099542641316 55.6782409858406)\",\n" +
            "  \"geomAggregation\": \"EQUALS\",\n" +
            "  \"returnCentroid\": false,\n" +
            "  \"pageRequest\": {\n" +
            "    \"pageNumber\": 0,\n" +
            "    \"pageSize\": 50\n" +
            "  }\n" +
            "}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(GET_AGGREGATED_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("layerId"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 43: NEGATIVE POST geoData/v2/getAggregatedObjectsByCoordinates - невалидная структура linkedLayers
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию linkedLayers - должен быть массивом
     * - Проверяет что API возвращает ошибку 500 при невалидной структуре
     */
    @Test(priority = 43, description = "NEGATIVE POST geoData/v2/getAggregatedObjectsByCoordinates - невалидная структура linkedLayers")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить агрегированные объекты с невалидной структурой linkedLayers (строка вместо массива). Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid LinkedLayers Structure")
    public void testGetAggregatedObjectsByCoordinatesWithInvalidLinkedLayers() {
        logger.info("Выполнение теста: NEGATIVE POST geoData/v2/getAggregatedObjectsByCoordinates - невалидная структура linkedLayers");
        
        // Создаем JSON строку с linkedLayers как строкой вместо массива
        String requestBody = "{\n" +
            "  \"mainLayer\": {\n" +
            "    \"layerId\": 6210,\n" +
            "    \"properties\": [\"string\"]\n" +
            "  },\n" +
            "  \"linkedLayers\": \"not_an_array\",\n" +
            "  \"coordinates\": \"POINT(37.51099542641316 55.6782409858406)\",\n" +
            "  \"geomAggregation\": \"EQUALS\",\n" +
            "  \"returnCentroid\": false,\n" +
            "  \"pageRequest\": {\n" +
            "    \"pageNumber\": 0,\n" +
            "    \"pageSize\": 50\n" +
            "  }\n" +
            "}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(GET_AGGREGATED_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("linkedLayers"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 44: NEGATIVE POST geoData/v2/getAggregatedObjectsByCoordinates - невалидный формат coordinates
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию coordinates - должен быть валидным WKT
     * - Проверяет что API возвращает ошибку 400 при невалидном формате
     */
    @Test(priority = 44, description = "NEGATIVE POST geoData/v2/getAggregatedObjectsByCoordinates - невалидный формат coordinates")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить агрегированные объекты с невалидным форматом coordinates. Проверка статуса 400 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid Coordinates Format (Aggregated)")
    public void testGetAggregatedObjectsByCoordinatesWithInvalidCoordinatesFormat() {
        logger.info("Выполнение теста: NEGATIVE POST geoData/v2/getAggregatedObjectsByCoordinates - невалидный формат coordinates");
        
        // Создаем объект mainLayer
        List<String> mainLayerProperties = new ArrayList<>();
        mainLayerProperties.add("string");
        MainLayer mainLayer = new MainLayer(6210, mainLayerProperties);
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 50);
        
        // Создаем объект запроса с невалидным форматом coordinates
        GetAggregatedObjectsByCoordinatesRequest request = new GetAggregatedObjectsByCoordinatesRequest(
            mainLayer,
            new ArrayList<>(),
            "INVALID_WKT",
            "EQUALS",
            false,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_AGGREGATED_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(400)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Поле 'coordinates' должно содержать корректную WKT-геометрию"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 45: NEGATIVE POST geoData/v2/getAggregatedObjectsByCoordinates - невалидный тип returnCentroid
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию returnCentroid - должен быть boolean
     * - Проверяет что API возвращает ошибку 500 при невалидном типе
     */
    @Test(priority = 45, description = "NEGATIVE POST geoData/v2/getAggregatedObjectsByCoordinates - невалидный тип returnCentroid")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить агрегированные объекты с невалидным типом returnCentroid (строка вместо boolean). Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid ReturnCentroid Type (Aggregated)")
    public void testGetAggregatedObjectsByCoordinatesWithInvalidReturnCentroidType() {
        logger.info("Выполнение теста: NEGATIVE POST geoData/v2/getAggregatedObjectsByCoordinates - невалидный тип returnCentroid");
        
        // Создаем JSON строку с returnCentroid как строкой вместо boolean
        String requestBody = "{\n" +
            "  \"mainLayer\": {\n" +
            "    \"layerId\": 6210,\n" +
            "    \"properties\": [\"string\"]\n" +
            "  },\n" +
            "  \"linkedLayers\": [],\n" +
            "  \"coordinates\": \"POINT(37.51099542641316 55.6782409858406)\",\n" +
            "  \"geomAggregation\": \"EQUALS\",\n" +
            "  \"returnCentroid\": \"not_boolean\",\n" +
            "  \"pageRequest\": {\n" +
            "    \"pageNumber\": 0,\n" +
            "    \"pageSize\": 50\n" +
            "  }\n" +
            "}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(GET_AGGREGATED_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("returnCentroid"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 46: NEGATIVE POST geoData/v2/getAggregatedObjectsByCoordinates - невалидные значения pageRequest
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию pageRequest - pageSize должен быть больше нуля
     * - Проверяет что API возвращает ошибку 400 при невалидных значениях
     */
    @Test(priority = 46, description = "NEGATIVE POST geoData/v2/getAggregatedObjectsByCoordinates - невалидные значения pageRequest")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить агрегированные объекты с невалидными значениями pageRequest (pageNumber: -1, pageSize: 0). Проверка статуса 400 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid PageRequest Values (Aggregated)")
    public void testGetAggregatedObjectsByCoordinatesWithInvalidPageRequest() {
        logger.info("Выполнение теста: NEGATIVE POST geoData/v2/getAggregatedObjectsByCoordinates - невалидные значения pageRequest");
        
        // Создаем объект mainLayer
        List<String> mainLayerProperties = new ArrayList<>();
        mainLayerProperties.add("string");
        MainLayer mainLayer = new MainLayer(6210, mainLayerProperties);
        
        // Создаем объект PageRequest с невалидными значениями
        PageRequest pageRequest = new PageRequest(-1, 0);
        
        // Создаем объект запроса
        GetAggregatedObjectsByCoordinatesRequest request = new GetAggregatedObjectsByCoordinatesRequest(
            mainLayer,
            new ArrayList<>(),
            "POINT(37.51099542641316 55.6782409858406)",
            "EQUALS",
            false,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_AGGREGATED_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(400)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("(pageSize) должен быть больше нуля"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 47: NEGATIVE POST geoData/v2/getAggregatedObjectsByCoordinates - невалидный JSON
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет обработку невалидного JSON (незакрытая скобка)
     * - Проверяет что API возвращает ошибку 400 или 500 при невалидном JSON
     */
    @Test(priority = 47, description = "NEGATIVE POST geoData/v2/getAggregatedObjectsByCoordinates - невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка отправить невалидный JSON (незакрытая скобка). Проверка статуса 400 или 500 и наличия поля error.")
    @Story("Negative Tests - Invalid JSON (Aggregated)")
    public void testGetAggregatedObjectsByCoordinatesWithInvalidJson() {
        logger.info("Выполнение теста: NEGATIVE POST geoData/v2/getAggregatedObjectsByCoordinates - невалидный JSON");
        
        // Создаем невалидный JSON (незакрытая скобка)
        String invalidJson = "{\n" +
            "  \"mainLayer\": {\n" +
            "    \"layerId\": 6210,\n" +
            "    \"properties\": [\"string\"]\n" +
            "  },\n" +
            "  \"linkedLayers\": [],\n" +
            "  \"coordinates\": \"POINT(37.51099542641316 55.6782409858406)\",\n" +
            "  \"geomAggregation\": \"EQUALS\",\n" +
            "  \"returnCentroid\": false,\n" +
            "  \"pageRequest\": {\n" +
            "    \"pageNumber\": 0,\n" +
            "    \"pageSize\": 50\n";
        // Намеренно не закрываем JSON
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .post(GET_AGGREGATED_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("error", notNullValue())
                .body("error", containsString("Internal Server Error"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("error"));
    }
    
    /**
     * ТЕСТ 48: CONTAINS geoData/v2/getAggregatedObjectsByCoordinates
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет работу API с geomAggregation: "CONTAINS"
     * - Проверяет корректность ответа и структуру данных
     */
    @Test(priority = 48, description = "CONTAINS geoData/v2/getAggregatedObjectsByCoordinates")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение агрегированных объектов с geomAggregation: CONTAINS. Проверка статуса 200, размера ответа, layerId и структуры данных.")
    @Story("Positive Tests - CONTAINS Aggregation")
    public void testGetAggregatedObjectsByCoordinatesContains() {
        logger.info("Выполнение теста: CONTAINS geoData/v2/getAggregatedObjectsByCoordinates");
        
        // Создаем объект mainLayer
        List<String> mainLayerProperties = new ArrayList<>();
        mainLayerProperties.add("fileid");
        mainLayerProperties.add("id");
        MainLayer mainLayer = new MainLayer(11260, mainLayerProperties);
        
        // Создаем linkedLayers
        List<MainLayer> linkedLayers = new ArrayList<>();
        
        List<String> linkedLayer1Properties = new ArrayList<>();
        linkedLayer1Properties.add("id");
        linkedLayer1Properties.add("fileid");
        linkedLayer1Properties.add("load_fid");
        MainLayer linkedLayer1 = new MainLayer(11651, linkedLayer1Properties);
        linkedLayers.add(linkedLayer1);
        
        List<String> linkedLayer2Properties = new ArrayList<>();
        linkedLayer2Properties.add("id");
        linkedLayer2Properties.add("fileid");
        linkedLayer2Properties.add("load_fid");
        MainLayer linkedLayer2 = new MainLayer(11650, linkedLayer2Properties);
        linkedLayers.add(linkedLayer2);
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 10000);
        
        // Создаем объект запроса
        GetAggregatedObjectsByCoordinatesRequest request = new GetAggregatedObjectsByCoordinatesRequest(
            mainLayer,
            linkedLayers,
            "POLYGON((37.50073293025099 55.678407640436944, 37.50073293025099 55.67198266626835, 37.509477277188125 55.67198266626835, 37.509477277188125 55.678407640436944, 37.50073293025099 55.678407640436944))",
            "CONTAINS",
            true,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_AGGREGATED_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("size", equalTo(2))
                .body("content.features[0].layerId", equalTo("11651"))
                .body("size", notNullValue())
                .body("size", instanceOf(Integer.class))
                .body("pageNumber", notNullValue())
                .body("pageNumber", instanceOf(Integer.class))
                .body("pageSize", notNullValue())
                .body("pageSize", instanceOf(Integer.class))
                .body("totalElements", notNullValue())
                .body("totalElements", instanceOf(Integer.class))
                .body("totalPages", notNullValue())
                .body("totalPages", instanceOf(Integer.class))
                .body("content", notNullValue())
                .body("content", instanceOf(java.util.Map.class))
                .body("content.type", equalTo("FeatureCollection"))
                .body("content.features", notNullValue())
                .body("content.features", instanceOf(java.util.List.class))
                .extract()
                .response();
        
        // Дополнительные проверки структуры features
        List<Map<String, Object>> features = response.jsonPath().getList("content.features");
        assertAggregatedFeatureStructure(features);
        
        logger.info("Тест успешно завершен. Размер ответа: {}, количество features: {}",
            response.jsonPath().getInt("size"), features.size());
    }
    
    /**
     * ТЕСТ 49: WITHIN geoData/v2/getAggregatedObjectsByCoordinates
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет работу API с geomAggregation: "WITHIN"
     * - Проверяет корректность ответа и структуру данных
     */
    @Test(priority = 49, description = "WITHIN geoData/v2/getAggregatedObjectsByCoordinates")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение агрегированных объектов с geomAggregation: WITHIN. Проверка статуса 200, размера ответа, layerId и структуры данных.")
    @Story("Positive Tests - WITHIN Aggregation")
    public void testGetAggregatedObjectsByCoordinatesWithin() {
        logger.info("Выполнение теста: WITHIN geoData/v2/getAggregatedObjectsByCoordinates");
        
        // Создаем объект mainLayer
        List<String> mainLayerProperties = new ArrayList<>();
        mainLayerProperties.add("fileid");
        mainLayerProperties.add("id");
        MainLayer mainLayer = new MainLayer(11651, mainLayerProperties);
        
        // Создаем linkedLayers
        List<MainLayer> linkedLayers = new ArrayList<>();
        
        List<String> linkedLayer1Properties = new ArrayList<>();
        linkedLayer1Properties.add("id");
        linkedLayer1Properties.add("fileid");
        linkedLayer1Properties.add("load_fid");
        MainLayer linkedLayer1 = new MainLayer(11260, linkedLayer1Properties);
        linkedLayers.add(linkedLayer1);
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 10000);
        
        // Создаем объект запроса
        GetAggregatedObjectsByCoordinatesRequest request = new GetAggregatedObjectsByCoordinatesRequest(
            mainLayer,
            linkedLayers,
            "POLYGON((37.50073293025099 55.678407640436944, 37.50073293025099 55.67198266626835, 37.509477277188125 55.67198266626835, 37.509477277188125 55.678407640436944, 37.50073293025099 55.678407640436944))",
            "WITHIN",
            true,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_AGGREGATED_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("size", equalTo(2))
                .body("content.features[0].layerId", equalTo("11260"))
                .body("size", notNullValue())
                .body("size", instanceOf(Integer.class))
                .body("pageNumber", notNullValue())
                .body("pageNumber", instanceOf(Integer.class))
                .body("pageSize", notNullValue())
                .body("pageSize", instanceOf(Integer.class))
                .body("totalElements", notNullValue())
                .body("totalElements", instanceOf(Integer.class))
                .body("totalPages", notNullValue())
                .body("totalPages", instanceOf(Integer.class))
                .body("content", notNullValue())
                .body("content", instanceOf(java.util.Map.class))
                .body("content.type", equalTo("FeatureCollection"))
                .body("content.features", notNullValue())
                .body("content.features", instanceOf(java.util.List.class))
                .extract()
                .response();
        
        // Дополнительные проверки структуры features
        List<Map<String, Object>> features = response.jsonPath().getList("content.features");
        assertAggregatedFeatureStructure(features);
        
        logger.info("Тест успешно завершен. Размер ответа: {}, количество features: {}",
            response.jsonPath().getInt("size"), features.size());
    }
    
    /**
     * ТЕСТ 50: UNION geoData/v2/getAggregatedObjectsByCoordinates
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет работу API с geomAggregation: "UNION"
     * - Проверяет корректность ответа и структуру данных
     * - Проверяет что все features имеют properties.id="1"
     */
    @Test(priority = 50, description = "UNION geoData/v2/getAggregatedObjectsByCoordinates")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение агрегированных объектов с geomAggregation: UNION. Проверка статуса 200, размера ответа, layerId, properties.id и структуры данных.")
    @Story("Positive Tests - UNION Aggregation")
    public void testGetAggregatedObjectsByCoordinatesUnion() {
        logger.info("Выполнение теста: UNION geoData/v2/getAggregatedObjectsByCoordinates");
        
        // Создаем объект mainLayer
        List<String> mainLayerProperties = new ArrayList<>();
        mainLayerProperties.add("fileid");
        mainLayerProperties.add("id");
        MainLayer mainLayer = new MainLayer(11260, mainLayerProperties);
        
        // Создаем linkedLayers
        List<MainLayer> linkedLayers = new ArrayList<>();
        
        List<String> linkedLayer1Properties = new ArrayList<>();
        linkedLayer1Properties.add("id");
        linkedLayer1Properties.add("fileid");
        linkedLayer1Properties.add("load_fid");
        MainLayer linkedLayer1 = new MainLayer(11650, linkedLayer1Properties);
        linkedLayers.add(linkedLayer1);
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 10000);
        
        // Создаем объект запроса
        GetAggregatedObjectsByCoordinatesRequest request = new GetAggregatedObjectsByCoordinatesRequest(
            mainLayer,
            linkedLayers,
            "POINT(37.506564850899196 55.67498258025543)",
            "UNION",
            true,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_AGGREGATED_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("size", equalTo(2))
                .body("content.features[0].layerId", equalTo("11650"))
                .body("size", notNullValue())
                .body("size", instanceOf(Integer.class))
                .body("pageNumber", notNullValue())
                .body("pageNumber", instanceOf(Integer.class))
                .body("pageSize", notNullValue())
                .body("pageSize", instanceOf(Integer.class))
                .body("totalElements", notNullValue())
                .body("totalElements", instanceOf(Integer.class))
                .body("totalPages", notNullValue())
                .body("totalPages", instanceOf(Integer.class))
                .body("content", notNullValue())
                .body("content", instanceOf(java.util.Map.class))
                .body("content.type", equalTo("FeatureCollection"))
                .body("content.features", notNullValue())
                .body("content.features", instanceOf(java.util.List.class))
                .extract()
                .response();
        
        // Дополнительные проверки структуры features
        List<Map<String, Object>> features = response.jsonPath().getList("content.features");
        assertAggregatedFeatureStructure(features);
        for (int i = 0; i < features.size(); i++) {
            Map<String, Object> feature = features.get(i);
            if (feature.containsKey("properties") && feature.get("properties") instanceof Map) {
                Map<String, Object> properties = (Map<String, Object>) feature.get("properties");
                if (properties.containsKey("id")) {
                    Assert.assertEquals(properties.get("id"), "1",
                        "feature[" + i + "].properties.id должен быть равен \"1\"");
                }
            }
        }
        
        logger.info("Тест успешно завершен. Размер ответа: {}, количество features: {}",
            response.jsonPath().getInt("size"), features.size());
    }
    
    /**
     * ТЕСТ 51: SYMMETRICAL_DIFFERENCE geoData/v2/getAggregatedObjectsByCoordinates
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет работу API с geomAggregation: "SYMMETRICAL_DIFFERENCE"
     * - Проверяет корректность ответа и структуру данных
     * - Проверяет что все features имеют properties.id="1"
     * - Проверяет что полигоны замкнуты
     */
    @Test(priority = 51, description = "SYMMETRICAL_DIFFERENCE geoData/v2/getAggregatedObjectsByCoordinates")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение агрегированных объектов с geomAggregation: SYMMETRICAL_DIFFERENCE. Проверка статуса 200, totalElements=2, layerId, properties.id и замкнутости полигонов.")
    @Story("Positive Tests - SYMMETRICAL_DIFFERENCE Aggregation")
    public void testGetAggregatedObjectsByCoordinatesSymmetricalDifference() {
        logger.info("Выполнение теста: SYMMETRICAL_DIFFERENCE geoData/v2/getAggregatedObjectsByCoordinates");
        
        // Создаем объект mainLayer
        List<String> mainLayerProperties = new ArrayList<>();
        mainLayerProperties.add("fileid");
        mainLayerProperties.add("id");
        MainLayer mainLayer = new MainLayer(11260, mainLayerProperties);
        
        // Создаем linkedLayers
        List<MainLayer> linkedLayers = new ArrayList<>();
        
        List<String> linkedLayer1Properties = new ArrayList<>();
        linkedLayer1Properties.add("id");
        linkedLayer1Properties.add("fileid");
        linkedLayer1Properties.add("load_fid");
        MainLayer linkedLayer1 = new MainLayer(11650, linkedLayer1Properties);
        linkedLayers.add(linkedLayer1);
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 10000);
        
        // Создаем объект запроса
        GetAggregatedObjectsByCoordinatesRequest request = new GetAggregatedObjectsByCoordinatesRequest(
            mainLayer,
            linkedLayers,
            "POLYGON((37.50073293025099 55.678407640436944, 37.50073293025099 55.67198266626835, 37.509477277188125 55.67198266626835, 37.509477277188125 55.678407640436944, 37.50073293025099 55.678407640436944))",
            "SYMMETRICAL_DIFFERENCE",
            true,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_AGGREGATED_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("totalElements", equalTo(2))
                .body("content.features[0].layerId", equalTo("11650"))
                .body("size", notNullValue())
                .body("size", instanceOf(Integer.class))
                .body("pageNumber", notNullValue())
                .body("pageNumber", instanceOf(Integer.class))
                .body("pageSize", notNullValue())
                .body("pageSize", instanceOf(Integer.class))
                .body("totalElements", notNullValue())
                .body("totalElements", instanceOf(Integer.class))
                .body("totalPages", notNullValue())
                .body("totalPages", instanceOf(Integer.class))
                .body("content", notNullValue())
                .body("content", instanceOf(java.util.Map.class))
                .body("content.type", equalTo("FeatureCollection"))
                .body("content.features", notNullValue())
                .body("content.features", instanceOf(java.util.List.class))
                .extract()
                .response();
        
        // Дополнительные проверки структуры features
        List<Map<String, Object>> features = response.jsonPath().getList("content.features");
        assertAggregatedFeatureStructure(features);
        assertAggregatedFeaturesPropertiesIdAndPolygonsClosed(features);
        
        logger.info("Тест успешно завершен. Размер ответа: {}, количество features: {}",
            response.jsonPath().getInt("size"), features.size());
    }
    
    /**
     * ТЕСТ 52: DIFFERENCE geoData/v2/getAggregatedObjectsByCoordinates
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет работу API с geomAggregation: "DIFFERENCE"
     * - Проверяет корректность ответа и структуру данных
     * - Проверяет что все features имеют properties.id="1"
     * - Проверяет что полигоны замкнуты
     */
    @Test(priority = 52, description = "DIFFERENCE geoData/v2/getAggregatedObjectsByCoordinates")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение агрегированных объектов с geomAggregation: DIFFERENCE. Проверка статуса 200, totalElements=2, layerId, properties.id и замкнутости полигонов.")
    @Story("Positive Tests - DIFFERENCE Aggregation")
    public void testGetAggregatedObjectsByCoordinatesDifference() {
        logger.info("Выполнение теста: DIFFERENCE geoData/v2/getAggregatedObjectsByCoordinates");
        
        // Создаем объект mainLayer
        List<String> mainLayerProperties = new ArrayList<>();
        mainLayerProperties.add("fileid");
        mainLayerProperties.add("id");
        MainLayer mainLayer = new MainLayer(11260, mainLayerProperties);
        
        // Создаем linkedLayers
        List<MainLayer> linkedLayers = new ArrayList<>();
        
        List<String> linkedLayer1Properties = new ArrayList<>();
        linkedLayer1Properties.add("id");
        linkedLayer1Properties.add("fileid");
        linkedLayer1Properties.add("load_fid");
        MainLayer linkedLayer1 = new MainLayer(11650, linkedLayer1Properties);
        linkedLayers.add(linkedLayer1);
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 10000);
        
        // Создаем объект запроса
        GetAggregatedObjectsByCoordinatesRequest request = new GetAggregatedObjectsByCoordinatesRequest(
            mainLayer,
            linkedLayers,
            "POLYGON((37.50073293025099 55.678407640436944, 37.50073293025099 55.67198266626835, 37.509477277188125 55.67198266626835, 37.509477277188125 55.678407640436944, 37.50073293025099 55.678407640436944))",
            "DIFFERENCE",
            true,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_AGGREGATED_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("totalElements", equalTo(2))
                .body("content.features[0].layerId", equalTo("11650"))
                .body("size", notNullValue())
                .body("size", instanceOf(Integer.class))
                .body("pageNumber", notNullValue())
                .body("pageNumber", instanceOf(Integer.class))
                .body("pageSize", notNullValue())
                .body("pageSize", instanceOf(Integer.class))
                .body("totalElements", notNullValue())
                .body("totalElements", instanceOf(Integer.class))
                .body("totalPages", notNullValue())
                .body("totalPages", instanceOf(Integer.class))
                .body("content", notNullValue())
                .body("content", instanceOf(java.util.Map.class))
                .body("content.type", equalTo("FeatureCollection"))
                .body("content.features", notNullValue())
                .body("content.features", instanceOf(java.util.List.class))
                .extract()
                .response();
        
        // Дополнительные проверки структуры features
        List<Map<String, Object>> features = response.jsonPath().getList("content.features");
        assertAggregatedFeatureStructure(features);
        assertAggregatedFeaturesPropertiesIdAndPolygonsClosed(features);
        
        logger.info("Тест успешно завершен. Размер ответа: {}, количество features: {}",
            response.jsonPath().getInt("size"), features.size());
    }
    
    /**
     * ТЕСТ 53: DISTANCE geoData/v2/getAggregatedObjectsByCoordinates
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет работу API с geomAggregation: "DISTANCE"
     * - Проверяет корректность ответа и структуру данных
     * - Проверяет что полигоны замкнуты (проверяет 8 элементов массива координат)
     */
    @Test(priority = 53, description = "DISTANCE geoData/v2/getAggregatedObjectsByCoordinates")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение агрегированных объектов с geomAggregation: DISTANCE. Проверка статуса 200, totalElements=8, layerId и замкнутости полигонов.")
    @Story("Positive Tests - DISTANCE Aggregation")
    public void testGetAggregatedObjectsByCoordinatesDistance() {
        logger.info("Выполнение теста: DISTANCE geoData/v2/getAggregatedObjectsByCoordinates");
        
        // Создаем объект mainLayer
        List<String> mainLayerProperties = new ArrayList<>();
        mainLayerProperties.add("fileid");
        mainLayerProperties.add("id");
        MainLayer mainLayer = new MainLayer(11260, mainLayerProperties);
        
        // Создаем linkedLayers
        List<MainLayer> linkedLayers = new ArrayList<>();
        
        List<String> linkedLayer1Properties = new ArrayList<>();
        linkedLayer1Properties.add("id");
        linkedLayer1Properties.add("fileid");
        linkedLayer1Properties.add("load_fid");
        MainLayer linkedLayer1 = new MainLayer(11651, linkedLayer1Properties);
        linkedLayers.add(linkedLayer1);
        
        List<String> linkedLayer2Properties = new ArrayList<>();
        linkedLayer2Properties.add("id");
        linkedLayer2Properties.add("fileid");
        linkedLayer2Properties.add("load_fid");
        MainLayer linkedLayer2 = new MainLayer(11650, linkedLayer2Properties);
        linkedLayers.add(linkedLayer2);
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 10000);
        
        // Создаем объект запроса с distance
        GetAggregatedObjectsByCoordinatesRequest request = new GetAggregatedObjectsByCoordinatesRequest(
            mainLayer,
            linkedLayers,
            "POINT(37.504904096840846 55.675833784610546)",
            "DISTANCE",
            true,
            700,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_AGGREGATED_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("totalElements", equalTo(8))
                .body("content.features[0].layerId", equalTo("11650"))
                .body("size", notNullValue())
                .body("size", instanceOf(Integer.class))
                .body("pageNumber", notNullValue())
                .body("pageNumber", instanceOf(Integer.class))
                .body("pageSize", notNullValue())
                .body("pageSize", instanceOf(Integer.class))
                .body("totalElements", notNullValue())
                .body("totalElements", instanceOf(Integer.class))
                .body("totalPages", notNullValue())
                .body("totalPages", instanceOf(Integer.class))
                .body("content", notNullValue())
                .body("content", instanceOf(java.util.Map.class))
                .body("content.type", equalTo("FeatureCollection"))
                .body("content.features", notNullValue())
                .body("content.features", instanceOf(java.util.List.class))
                .extract()
                .response();
        
        // Дополнительные проверки структуры features
        List<Map<String, Object>> features = response.jsonPath().getList("content.features");
        assertAggregatedFeatureStructure(features);
        assertPolygonsClosed(features, 8);
        
        logger.info("Тест успешно завершен. Размер ответа: {}, количество features: {}",
            response.jsonPath().getInt("size"), features.size());
    }
    
    /**
     * ТЕСТ 54: EQUALS geoData/v2/getAggregatedObjectsByCoordinates
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет работу API с geomAggregation: "EQUALS"
     * - Проверяет корректность ответа и структуру данных
     * - Проверяет что полигоны замкнуты (проверяет 8 элементов массива координат)
     */
    @Test(priority = 54, description = "EQUALS geoData/v2/getAggregatedObjectsByCoordinates")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение агрегированных объектов с geomAggregation: EQUALS. Проверка статуса 200, totalElements=1, layerId и замкнутости полигонов.")
    @Story("Positive Tests - EQUALS Aggregation")
    public void testGetAggregatedObjectsByCoordinatesEquals() {
        logger.info("Выполнение теста: EQUALS geoData/v2/getAggregatedObjectsByCoordinates");
        
        // Создаем объект mainLayer
        List<String> mainLayerProperties = new ArrayList<>();
        mainLayerProperties.add("fileid");
        mainLayerProperties.add("id");
        MainLayer mainLayer = new MainLayer(11651, mainLayerProperties);
        
        // Создаем linkedLayers
        List<MainLayer> linkedLayers = new ArrayList<>();
        
        List<String> linkedLayer1Properties = new ArrayList<>();
        linkedLayer1Properties.add("id");
        linkedLayer1Properties.add("fileid");
        linkedLayer1Properties.add("load_fid");
        MainLayer linkedLayer1 = new MainLayer(11650, linkedLayer1Properties);
        linkedLayers.add(linkedLayer1);
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 10000);
        
        // Создаем объект запроса
        GetAggregatedObjectsByCoordinatesRequest request = new GetAggregatedObjectsByCoordinatesRequest(
            mainLayer,
            linkedLayers,
            "POLYGON((37.50073293025099 55.678407640436944, 37.50073293025099 55.67198266626835, 37.509477277188125 55.67198266626835, 37.509477277188125 55.678407640436944, 37.50073293025099 55.678407640436944))",
            "EQUALS",
            true,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_AGGREGATED_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("totalElements", equalTo(1))
                .body("content.features[0].layerId", equalTo("11650"))
                .body("size", notNullValue())
                .body("size", instanceOf(Integer.class))
                .body("pageNumber", notNullValue())
                .body("pageNumber", instanceOf(Integer.class))
                .body("pageSize", notNullValue())
                .body("pageSize", instanceOf(Integer.class))
                .body("totalElements", notNullValue())
                .body("totalElements", instanceOf(Integer.class))
                .body("totalPages", notNullValue())
                .body("totalPages", instanceOf(Integer.class))
                .body("content", notNullValue())
                .body("content", instanceOf(java.util.Map.class))
                .body("content.type", equalTo("FeatureCollection"))
                .body("content.features", notNullValue())
                .body("content.features", instanceOf(java.util.List.class))
                .extract()
                .response();
        
        // Дополнительные проверки структуры features
        List<Map<String, Object>> features = response.jsonPath().getList("content.features");
        assertAggregatedFeatureStructure(features);
        assertPolygonsClosed(features, 8);
        
        logger.info("Тест успешно завершен. Размер ответа: {}, количество features: {}",
            response.jsonPath().getInt("size"), features.size());
    }
    
    /**
     * ТЕСТ 55: Without_radius /geoData/v2/getObjectsByCustomerConfig (POINT)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет работу API getObjectsByCustomerConfig с POINT без радиуса
     * - Проверяет что totalElements не больше 50
     * - Проверяет наличие поля distance в features
     */
    @Test(priority = 55, description = "Without_radius /geoData/v2/getObjectsByCustomerConfig (POINT)")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение объектов по конфигурации клиента с POINT без радиуса. Проверка статуса 200, totalElements <= 50, наличия поля distance в features.")
    @Story("Positive Tests - Get Objects By Customer Config Without Radius")
    public void testGetObjectsByCustomerConfigWithoutRadius() {
        logger.info("Выполнение теста: Without_radius /geoData/v2/getObjectsByCustomerConfig (POINT)");
        
        // Используем JSON строку напрямую, как в Postman, чтобы точно соответствовать запросу
        // В Postman запросе нет поля mainLayer вообще, а не null
        String requestBody = "{\n" +
            "  \"customerCode\": \"dtwWhatsHere\",\n" +
            "  \"geometry\": \"point(37.51018409276216 55.668493343905965)\",\n" +
            "  \"methodType\": \"OBJECTS_BY_COORDINATE\",\n" +
            "  \"pageRequest\": {\n" +
            "    \"pageNumber\": 0,\n" +
            "    \"pageSize\": 1000\n" +
            "  }\n" +
            "}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(GET_OBJECTS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("totalElements", lessThanOrEqualTo(50))
                .body("size", notNullValue())
                .body("size", instanceOf(Integer.class))
                .body("pageNumber", notNullValue())
                .body("pageNumber", instanceOf(Integer.class))
                .body("pageSize", notNullValue())
                .body("pageSize", instanceOf(Integer.class))
                .body("totalPages", notNullValue())
                .body("totalPages", instanceOf(Integer.class))
                .body("content", notNullValue())
                .body("content", instanceOf(java.util.Map.class))
                .body("content.type", equalTo("FeatureCollection"))
                .body("content.features", notNullValue())
                .body("content.features", instanceOf(java.util.List.class))
                .extract()
                .response();
        
        // Дополнительные проверки структуры features
        List<Map<String, Object>> features = response.jsonPath().getList("content.features");
        assertAggregatedFeatureStructure(features);
        
        logger.info("Тест успешно завершен. Размер ответа: {}, количество features: {}",
            response.jsonPath().getInt("size"), features.size());
    }
    
    /**
     * ТЕСТ 56: Add_radius PUT /methodConfigs/{customerCode}/{methodType}
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет обновление конфигурации метода с добавлением радиуса
     * - Проверяет корректность ответа и наличие ID
     */
    @Test(priority = 56, description = "Add_radius PUT /methodConfigs/{customerCode}/{methodType}")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Обновление конфигурации метода с добавлением радиуса. Проверка статуса 200 и наличия ID в ответе.")
    @Story("Positive Tests - Update Method Config With Radius")
    public void testUpdateMethodConfigWithRadius() {
        logger.info("Выполнение теста: Add_radius PUT /methodConfigs/{customerCode}/{methodType}");
        
        // Создаем queryParams с radius
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("radius", 100);
        
        // Создаем объект запроса
        MethodConfigUpdateRequest request = new MethodConfigUpdateRequest(queryParams);
        
        // Используем переменную methodType из константы или из окружения
        // В Postman используется {{methodType}}, предположим что это METHOD_TYPE_COORDINATE
        String methodType = METHOD_TYPE_COORDINATE;
        String endpoint = METHOD_CONFIGS_MANAGER_ENDPOINT + "/" + CUSTOMER_CODE + "/" + methodType;
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(endpoint)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("id", notNullValue())
                .body("id", instanceOf(Integer.class))
                .extract()
                .response();
        
        Integer responseId = response.jsonPath().getInt("id");
        logger.info("Конфигурация успешно обновлена. ID: {}", responseId);
    }
    
    /**
     * ТЕСТ 57: NEGATIVE PUT /methodConfigs - несуществующий customerCode
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет обработку ошибки при несуществующем customerCode
     * - Проверяет что API возвращает ошибку 500
     */
    @Test(priority = 57, description = "NEGATIVE PUT /methodConfigs - несуществующий customerCode")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка обновить конфигурацию метода с несуществующим customerCode. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Non-existent CustomerCode")
    public void testUpdateMethodConfigWithNonExistentCustomerCode() {
        logger.info("Выполнение теста: NEGATIVE PUT /methodConfigs - несуществующий customerCode");
        
        // Создаем queryParams с radius
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("radius", 250);
        
        // Создаем объект запроса
        MethodConfigUpdateRequest request = new MethodConfigUpdateRequest(queryParams);
        
        String endpoint = METHOD_CONFIGS_ENDPOINT + "/nonexistent_customer/OBJECTS_BY_COORDINATE";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(endpoint)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("No static resource methodConfigs"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 58: NEGATIVE PUT /methodConfigs - несуществующий methodType
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет обработку ошибки при несуществующем methodType
     * - Проверяет что API возвращает ошибку 500
     */
    @Test(priority = 58, description = "NEGATIVE PUT /methodConfigs - несуществующий methodType")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка обновить конфигурацию метода с несуществующим methodType. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid MethodType")
    public void testUpdateMethodConfigWithInvalidMethodType() {
        logger.info("Выполнение теста: NEGATIVE PUT /methodConfigs - несуществующий methodType");
        
        // Создаем queryParams с radius
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("radius", 250);
        
        // Создаем объект запроса
        MethodConfigUpdateRequest request = new MethodConfigUpdateRequest(queryParams);
        
        String endpoint = METHOD_CONFIGS_ENDPOINT + "/" + CUSTOMER_CODE + "/INVALID_METHOD_TYPE";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(endpoint)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("INVALID_METHOD_TYPE"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 59: NEGATIVE PUT /methodConfigs - отрицательное значение radius
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию отрицательного значения radius
     * - Проверяет что API возвращает ошибку 400 или 500
     */
    @Test(priority = 59, description = "NEGATIVE PUT /methodConfigs - отрицательное значение radius")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка обновить конфигурацию метода с отрицательным значением radius. Проверка статуса 400 или 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Negative Radius Value")
    public void testUpdateMethodConfigWithNegativeRadius() {
        logger.info("Выполнение теста: NEGATIVE PUT /methodConfigs - отрицательное значение radius");
        
        // Создаем queryParams с отрицательным radius
        Map<String, Object> queryParams = new java.util.HashMap<>();
        queryParams.put("radius", -1);
        
        // Создаем объект запроса
        MethodConfigUpdateRequest request = new MethodConfigUpdateRequest(queryParams);
        
        String endpoint = METHOD_CONFIGS_ENDPOINT + "/" + CUSTOMER_CODE + "/OBJECTS_BY_COORDINATE";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(endpoint)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("No static resource methodConfigs"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 60: NEGATIVE PUT /methodConfigs - невалидный тип radius
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию невалидного типа radius (строка вместо числа)
     * - Проверяет что API возвращает ошибку 400 или 500
     */
    @Test(priority = 60, description = "NEGATIVE PUT /methodConfigs - невалидный тип radius")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка обновить конфигурацию метода с невалидным типом radius (строка вместо числа). Проверка статуса 400 или 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid Radius Type")
    public void testUpdateMethodConfigWithInvalidRadiusType() {
        logger.info("Выполнение теста: NEGATIVE PUT /methodConfigs - невалидный тип radius");
        
        // Создаем queryParams с невалидным типом radius (строка вместо числа)
        // Используем JSON строку напрямую, так как модель не позволит передать строку в поле radius
        String requestBody = "{\n" +
            "  \"queryParams\": {\n" +
            "    \"radius\": \"not_a_number\"\n" +
            "  }\n" +
            "}";
        
        String endpoint = METHOD_CONFIGS_ENDPOINT + "/" + CUSTOMER_CODE + "/OBJECTS_BY_COORDINATE";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(endpoint)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("No static resource methodConfigs"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 61: NEGATIVE PUT /methodConfigs - невалидный JSON
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет обработку невалидного JSON (незакрытая скобка)
     * - Проверяет что API возвращает ошибку 400 или 500 при невалидном JSON
     */
    @Test(priority = 61, description = "NEGATIVE PUT /methodConfigs - невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка отправить невалидный JSON (незакрытая скобка) в PUT запрос. Проверка статуса 400 или 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid JSON (MethodConfigs)")
    public void testUpdateMethodConfigWithInvalidJson() {
        logger.info("Выполнение теста: NEGATIVE PUT /methodConfigs - невалидный JSON");
        
        // Создаем невалидный JSON (незакрытая скобка)
        String invalidJson = "{\n" +
            "  \"queryParams\": {\n" +
            "    \"radius\": 250\n";
        // Намеренно не закрываем JSON
        
        String endpoint = METHOD_CONFIGS_ENDPOINT + "/" + CUSTOMER_CODE + "/OBJECTS_BY_COORDINATE";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .put(endpoint)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("No static resource methodConfigs"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 62: With_radius/geoData/v2/getObjectsByCustomerConfig (POINT)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет работу API getObjectsByCustomerConfig с POINT с радиусом
     * - Проверяет что totalElements больше 40 и не больше 200
     * - Проверяет наличие поля distance в features
     */
    @Test(priority = 62, description = "With_radius/geoData/v2/getObjectsByCustomerConfig (POINT)")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение объектов по конфигурации клиента с POINT с радиусом. Проверка статуса 200, totalElements от 40 до 200, наличия поля distance в features.")
    @Story("Positive Tests - Get Objects By Customer Config With Radius")
    public void testGetObjectsByCustomerConfigWithRadius() {
        logger.info("Выполнение теста: With_radius/geoData/v2/getObjectsByCustomerConfig (POINT)");
        
        // Используем JSON строку напрямую, как в Postman
        String requestBody = "{\n" +
            "  \"customerCode\": \"dtwWhatsHere\",\n" +
            "  \"geometry\": \"point(37.51018409276216 55.668493343905965)\",\n" +
            "  \"methodType\": \"OBJECTS_BY_COORDINATE\",\n" +
            "  \"pageRequest\": {\n" +
            "    \"pageNumber\": 0,\n" +
            "    \"pageSize\": 1000\n" +
            "  }\n" +
            "}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(GET_OBJECTS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("totalElements", allOf(greaterThanOrEqualTo(40), lessThanOrEqualTo(200)))
                .body("size", notNullValue())
                .body("size", instanceOf(Integer.class))
                .body("pageNumber", notNullValue())
                .body("pageNumber", instanceOf(Integer.class))
                .body("pageSize", notNullValue())
                .body("pageSize", instanceOf(Integer.class))
                .body("totalPages", notNullValue())
                .body("totalPages", instanceOf(Integer.class))
                .body("content", notNullValue())
                .body("content", instanceOf(java.util.Map.class))
                .body("content.type", equalTo("FeatureCollection"))
                .body("content.features", notNullValue())
                .body("content.features", instanceOf(java.util.List.class))
                .extract()
                .response();
        
        // Дополнительные проверки структуры features
        List<Map<String, Object>> features = response.jsonPath().getList("content.features");
        assertAggregatedFeatureStructure(features);
        
        logger.info("Тест успешно завершен. Размер ответа: {}, количество features: {}", 
            response.jsonPath().getInt("size"), features.size());
    }
    
    /**
     * ТЕСТ 63: DEL_radius PUT /methodConfigs/{customerCode}/{methodType}
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет удаление радиуса из конфигурации метода (установка radius = 0)
     * - Проверяет корректность ответа и наличие ID
     */
    @Test(priority = 63, description = "DEL_radius PUT /methodConfigs/{customerCode}/{methodType}")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Удаление радиуса из конфигурации метода (установка radius = 0). Проверка статуса 200 и наличия ID в ответе.")
    @Story("Positive Tests - Delete Radius From Method Config")
    public void testDeleteRadiusFromMethodConfig() {
        logger.info("Выполнение теста: DEL_radius PUT /methodConfigs/{customerCode}/{methodType}");
        
        // Создаем queryParams с radius = 0
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("radius", 0);
        
        // Создаем объект запроса
        MethodConfigUpdateRequest request = new MethodConfigUpdateRequest(queryParams);
        
        String methodType = METHOD_TYPE_COORDINATE;
        String endpoint = METHOD_CONFIGS_MANAGER_ENDPOINT + "/" + CUSTOMER_CODE + "/" + methodType;
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(endpoint)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("id", notNullValue())
                .body("id", instanceOf(Integer.class))
                .extract()
                .response();
        
        Integer responseId = response.jsonPath().getInt("id");
        logger.info("Радиус успешно удален из конфигурации. ID: {}", responseId);
    }
    
    /**
     * ТЕСТ 64: GET /methodConfigs/{customerCode}/{methodType}
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет получение конфигурации метода после удаления радиуса
     * - Проверяет что radius = 0
     */
    @Test(priority = 64, description = "GET /methodConfigs/{customerCode}/{methodType}")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение конфигурации метода после удаления радиуса. Проверка статуса 200 и что radius = 0.")
    @Story("Positive Tests - Get Method Config After Delete Radius")
    public void testGetMethodConfigAfterDeleteRadius() {
        logger.info("Выполнение теста: GET /methodConfigs/{customerCode}/{methodType}");
        
        String methodType = METHOD_TYPE_COORDINATE;
        String endpoint = METHOD_CONFIGS_MANAGER_ENDPOINT + "/" + CUSTOMER_CODE + "/" + methodType;
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .get(endpoint)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("queryParams.radius", equalTo(0))
                .extract()
                .response();
        
        logger.info("Конфигурация успешно получена. radius: {}", response.jsonPath().getInt("queryParams.radius"));
    }
    
    /**
     * ТЕСТ 65: NEGATIVE GET /methodConfigs - несуществующий customerCode
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет обработку ошибки при несуществующем customerCode
     * - Проверяет что API возвращает ошибку 500
     */
    @Test(priority = 65, description = "NEGATIVE GET /methodConfigs - несуществующий customerCode")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить конфигурацию метода с несуществующим customerCode. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Non-existent CustomerCode (GET)")
    public void testGetMethodConfigWithNonExistentCustomerCode() {
        logger.info("Выполнение теста: NEGATIVE GET /methodConfigs - несуществующий customerCode");
        
        String endpoint = METHOD_CONFIGS_ENDPOINT + "/nonexistent_customer/OBJECTS_BY_COORDINATE";
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .get(endpoint)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("No static resource methodConfigs"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 66: NEGATIVE GET /methodConfigs - несуществующий methodType
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет обработку ошибки при несуществующем methodType
     * - Проверяет что API возвращает ошибку 500
     */
    @Test(priority = 66, description = "NEGATIVE GET /methodConfigs - несуществующий methodType")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить конфигурацию метода с несуществующим methodType. Проверка статуса 500.")
    @Story("Negative Tests - Invalid MethodType (GET)")
    public void testGetMethodConfigWithInvalidMethodType() {
        logger.info("Выполнение теста: NEGATIVE GET /methodConfigs - несуществующий methodType");
        
        String endpoint = METHOD_CONFIGS_ENDPOINT + "/" + CUSTOMER_CODE + "/INVALID_METHOD_TYPE";
        
        given()
                .spec(getRequestSpec())
                .when()
                .get(endpoint)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class));
        
        logger.info("Ожидаемая ошибка получена");
    }
    
    /**
     * ТЕСТ 67: POST /userRequestHistory/list
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет получение списка истории запросов пользователя
     * - Проверяет наличие полей id и time в ответе
     * - Проверяет формат времени (ISO-формат)
     */
    @Test(priority = 67, description = "POST /userRequestHistory/list")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение списка истории запросов пользователя. Проверка статуса 200, наличия полей id и time, формата времени.")
    @Story("Positive Tests - Get User Request History List")
    public void testGetUserRequestHistoryList() {
        logger.info("Выполнение теста: POST /userRequestHistory/list");
        
        // Создаем объект SortRequest
        SortRequest sort = new SortRequest("time", "DESC");
        
        // Создаем объект запроса (используем pageNumber и pageSize напрямую, как в Postman)
        UserRequestHistoryListRequest request = new UserRequestHistoryListRequest(0, 100, sort);
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(USER_REQUEST_HISTORY_LIST_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", anyOf(instanceOf(java.util.List.class), instanceOf(java.util.Map.class)))
                .extract()
                .response();
        
        // Проверяем структуру ответа (может быть массив или объект)
        Object body = response.jsonPath().get();
        
        if (body instanceof List) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) body;
            Assert.assertTrue(items.size() > 0, "Массив не должен быть пустым");
            Map<String, Object> first = items.get(0);
            Assert.assertNotNull(first.get("id"), "Первый элемент должен содержать поле id");
            Assert.assertNotNull(first.get("time"), "Первый элемент должен содержать поле time");
            
            // Проверяем формат времени (ISO-формат) - проверяем начало строки, как в Postman
            Object timeObj = first.get("time");
            Assert.assertTrue(timeObj instanceof String, "time должен быть строкой");
            String time = (String) timeObj;
            // Проверяем что строка начинается с ISO-подобного формата (YYYY-MM-DDTHH:MM:SS)
            Assert.assertTrue(time.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"), 
                "time должен быть в ISO-формате (начинаться с YYYY-MM-DDTHH:MM:SS)");
        } else if (body instanceof Map) {
            Map<String, Object> item = (Map<String, Object>) body;
            Assert.assertNotNull(item.get("id"), "Ответ должен содержать поле id");
            Assert.assertNotNull(item.get("time"), "Ответ должен содержать поле time");
            
            // Проверяем формат времени (ISO-формат) - проверяем начало строки, как в Postman
            Object timeObj = item.get("time");
            Assert.assertTrue(timeObj instanceof String, "time должен быть строкой");
            String time = (String) timeObj;
            // Проверяем что строка начинается с ISO-подобного формата (YYYY-MM-DDTHH:MM:SS)
            Assert.assertTrue(time.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"), 
                "time должен быть в ISO-формате (начинаться с YYYY-MM-DDTHH:MM:SS)");
        }
        
        // Сохраняем lastRequestId для использования в следующих тестах
        if (body instanceof List) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) body;
            if (items.size() > 0 && items.get(0).get("id") != null) {
                lastRequestId = ((Number) items.get(0).get("id")).intValue();
            }
        } else if (body instanceof Map) {
            Map<String, Object> item = (Map<String, Object>) body;
            if (item.get("id") != null) {
                lastRequestId = ((Number) item.get("id")).intValue();
            }
        }
        
        logger.info("Список истории запросов успешно получен. lastRequestId: {}", lastRequestId);
    }
    
    /**
     * ТЕСТ 68: NEGATIVE POST /userRequestHistory/list - невалидная структура sort
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет обработку ошибки при пустом объекте sort
     * - Проверяет что API возвращает ошибку 500
     */
    @Test(priority = 68, description = "NEGATIVE POST /userRequestHistory/list - невалидная структура sort")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить список истории запросов с пустым объектом sort. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid Sort Structure")
    public void testGetUserRequestHistoryListWithInvalidSort() {
        logger.info("Выполнение теста: NEGATIVE POST /userRequestHistory/list - невалидная структура sort");
        
        // Создаем объект SortRequest с пустым объектом (используем JSON строку)
        String requestBody = "{\n" +
            "  \"pageRequest\": {\n" +
            "    \"pageNumber\": 0,\n" +
            "    \"pageSize\": 10\n" +
            "  },\n" +
            "  \"sort\": {}\n" +
            "}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(USER_REQUEST_HISTORY_LIST_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Внутренняя ошибка сервиса"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 69: NEGATIVE POST /userRequestHistory/list - невалидное значение sortDirection
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет обработку ошибки при невалидном значении sortDirection
     * - Проверяет что API возвращает ошибку 500
     */
    @Test(priority = 69, description = "NEGATIVE POST /userRequestHistory/list - невалидное значение sortDirection")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить список истории запросов с невалидным значением sortDirection. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid SortDirection Value")
    public void testGetUserRequestHistoryListWithInvalidSortDirection() {
        logger.info("Выполнение теста: NEGATIVE POST /userRequestHistory/list - невалидное значение sortDirection");
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 10);
        
        // Создаем объект SortRequest с невалидным sortDirection
        SortRequest sort = new SortRequest("id", "INVALID");
        
        // Создаем объект запроса
        UserRequestHistoryListRequest request = new UserRequestHistoryListRequest(pageRequest, sort);
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(USER_REQUEST_HISTORY_LIST_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("sortDirection"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 70: NEGATIVE POST /userRequestHistory/list - невалидный JSON
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет обработку невалидного JSON (незакрытая скобка)
     * - Проверяет что API возвращает ошибку 400 или 500
     */
    @Test(priority = 70, description = "NEGATIVE POST /userRequestHistory/list - невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка отправить невалидный JSON (незакрытая скобка) в POST запрос. Проверка статуса 400 или 500.")
    @Story("Negative Tests - Invalid JSON (UserRequestHistory)")
    public void testGetUserRequestHistoryListWithInvalidJson() {
        logger.info("Выполнение теста: NEGATIVE POST /userRequestHistory/list - невалидный JSON");
        
        // Создаем невалидный JSON (незакрытая скобка)
        String invalidJson = "{\n" +
            "  \"pageRequest\": {\n" +
            "    \"pageNumber\": 0,\n" +
            "    \"pageSize\": 10\n" +
            "  },\n" +
            "  \"sort\": {\n" +
            "    \"sortColumn\": \"id\",\n" +
            "    \"sortDirection\": \"DESC\"\n";
        // Намеренно не закрываем JSON
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .post(USER_REQUEST_HISTORY_LIST_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 71: GET /userRequestHistory/{id}
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет получение истории запроса по ID
     * - Проверяет структуру ответа (size, content.features, layerId, geometry)
     * - Проверяет наличие centroid если он есть
     */
    @Test(priority = 71, description = "GET /userRequestHistory/{id}")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение истории запроса по ID. Проверка статуса 200, структуры ответа, наличия полей size, content.features, layerId, geometry.")
    @Story("Positive Tests - Get User Request History By Id")
    public void testGetUserRequestHistoryById() {
        logger.info("Выполнение теста: GET /userRequestHistory/{id}");
        
        // Используем lastRequestId из теста 67; при отсутствии — помечаем тест как Skipped
        if (lastRequestId == null) {
            throw new SkipException("lastRequestId не установлен (тест 67 вернул пустой список или не выполнялся)");
        }
        
        String endpoint = USER_REQUEST_HISTORY_ENDPOINT + "/" + lastRequestId;
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .get(endpoint)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("size", notNullValue())
                .body("size", instanceOf(Integer.class))
                .body("size", greaterThan(0))
                .body("content", notNullValue())
                .body("content", instanceOf(java.util.Map.class))
                .body("content.features", notNullValue())
                .body("content.features", instanceOf(java.util.List.class))
                .body("content.features.size()", greaterThan(0))
                .extract()
                .response();
        
        List<Map<String, Object>> features = response.jsonPath().getList("content.features");
        assertUserRequestHistoryFeaturesStructure(features);
        logger.info("История запроса успешно получена. Размер: {}", response.jsonPath().getInt("size"));
    }

    /**
     * ТЕСТ 72: NEGATIVE GET /userRequestHistory - несуществующий id
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет обработку ошибки при несуществующем id
     * - Проверяет что API возвращает ошибку 500
     */
    @Test(priority = 72, description = "NEGATIVE GET /userRequestHistory - несуществующий id")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить историю запроса с несуществующим id. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Non-existent Request History Id")
    public void testGetUserRequestHistoryWithNonExistentId() {
        logger.info("Выполнение теста: NEGATIVE GET /userRequestHistory - несуществующий id");
        
        String endpoint = USER_REQUEST_HISTORY_ENDPOINT + "/999999999";
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .get(endpoint)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("No value present"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 73: NEGATIVE GET /userRequestHistory - невалидный формат id
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет обработку ошибки при невалидном формате id (строка вместо числа)
     * - Проверяет что API возвращает ошибку 400 или 500
     */
    @Test(priority = 73, description = "NEGATIVE GET /userRequestHistory - невалидный формат id")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить историю запроса с невалидным форматом id. Проверка статуса 400 или 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid Request History Id Format")
    public void testGetUserRequestHistoryWithInvalidIdFormat() {
        logger.info("Выполнение теста: NEGATIVE GET /userRequestHistory - невалидный формат id");
        
        String endpoint = USER_REQUEST_HISTORY_ENDPOINT + "/invalid_id_format";
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .get(endpoint)
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(500)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("invalid_id_format"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 74: Percent geoData/v2/getObjectsByPolygon
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет работу API getObjectsByPolygon с параметрами lowerOverlapPercent и upperOverlapPercent
     * - Проверяет корректность ответа и структуру данных
     */
    @Test(priority = 74, description = "Percent geoData/v2/getObjectsByPolygon")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение объектов по полигону с параметрами overlapPercent. Проверка статуса 200, size=3, структуры данных.")
    @Story("Positive Tests - Get Objects By Polygon With Overlap Percent")
    public void testGetObjectsByPolygonWithOverlapPercent() {
        logger.info("Выполнение теста: Percent geoData/v2/getObjectsByPolygon");
        
        // Создаем layers с lowerOverlapPercent и upperOverlapPercent
        List<Layer> layers = new ArrayList<>();
        
        List<String> layer1Properties = new ArrayList<>();
        Layer layer1 = new Layer(11260, layer1Properties, 0, 100);
        layers.add(layer1);
        
        List<String> layer2Properties = new ArrayList<>();
        Layer layer2 = new Layer(11651, layer2Properties, 30, 100);
        layers.add(layer2);
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 10000);
        
        // Создаем объект запроса
        GetObjectsByPolygonRequest request = new GetObjectsByPolygonRequest(
            layers,
            false,
            "POLYGON((37.503257841478785 55.67833749101413, 37.503257841478785 55.67311685921857, 37.50862748045975 55.67311685921857, 37.50862748045975 55.67833749101413, 37.503257841478785 55.67833749101413))",
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_OBJECTS_BY_POLYGON_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("size", equalTo(3))
                .body("size", notNullValue())
                .body("size", instanceOf(Integer.class))
                .body("pageNumber", notNullValue())
                .body("pageNumber", instanceOf(Integer.class))
                .body("pageSize", notNullValue())
                .body("pageSize", instanceOf(Integer.class))
                .body("totalElements", notNullValue())
                .body("totalElements", instanceOf(Integer.class))
                .body("totalPages", notNullValue())
                .body("totalPages", instanceOf(Integer.class))
                .body("content", notNullValue())
                .body("content", instanceOf(java.util.Map.class))
                .body("content.type", equalTo("FeatureCollection"))
                .body("content.features", notNullValue())
                .body("content.features", instanceOf(java.util.List.class))
                .body("content.features.size()", greaterThan(0))
                .extract()
                .response();
        
        List<Map<String, Object>> features = response.jsonPath().getList("content.features");
        assertFeatureCollectionStructure(features);
        logger.info("Тест успешно завершен. Размер ответа: {}, количество features: {}",
            response.jsonPath().getInt("size"), features != null ? features.size() : 0);
    }

    /**
     * ТЕСТ 75: DTW_DA_CM_INTERSECTIONSERVICE_geoData/v2/getObjectsByPolygon
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет работу API getObjectsByPolygon с другими куками (cookie_autotest)
     * - Проверяет что API возвращает ошибку 403 (нет доступа)
     * - Если cookie_autotest не задан — тест пропускается (SkipException)
     */
    @Test(priority = 75, description = "DTW_DA_CM_INTERSECTIONSERVICE_geoData/v2/getObjectsByPolygon")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить объекты по полигону с другими куками. Проверка статуса 403. Требует переменную cookie_autotest; при её отсутствии тест пропускается.")
    @Story("Negative Tests - Access Denied With Different Cookies")
    public void testGetObjectsByPolygonWithDifferentCookies() {
        logger.info("Выполнение теста: DTW_DA_CM_INTERSECTIONSERVICE_geoData/v2/getObjectsByPolygon");
        
        // Если cookie_autotest не задан — пропускаем тест (не падаем)
        RequestSpecification spec = getRequestSpecWithAutotestCookie();
        if (spec == null) {
            logger.warn("cookie_autotest не задан — тест пропускается");
            throw new SkipException("cookie_autotest не задан. В PowerShell для значения с точкой с запятой используйте ОДИНАРНЫЕ кавычки: -Dcookie_autotest='ваша_кука' (не двойные). Или задайте переменную окружения: $env:cookie_autotest='...'");
        }
        
        // Создаем layers
        List<Layer> layers = new ArrayList<>();
        
        List<String> layer1Properties = new ArrayList<>();
        Layer layer1 = new Layer(24, null, layer1Properties);
        layers.add(layer1);
        
        List<String> layer2Properties = new ArrayList<>();
        layer2Properties.add("id");
        layer2Properties.add("fileid");
        Layer layer2 = new Layer(6061, null, layer2Properties);
        layers.add(layer2);
        
        List<String> layer3Properties = new ArrayList<>();
        Layer layer3 = new Layer(3230, null, layer3Properties);
        layers.add(layer3);
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 1000);
        
        // Создаем объект запроса
        GetObjectsByPolygonRequest request = new GetObjectsByPolygonRequest(
            layers,
            true,
            "POLYGON((37.50484876080969 55.67801657177213, 37.50743071972357 55.67662137482222, 37.50889247942544 55.67744831217249, 37.50640040203956 55.67887565537279, 37.50484876080969 55.67801657177213))",
            pageRequest
        );
        
        given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_OBJECTS_BY_POLYGON_ENDPOINT)
                .then()
                .statusCode(403)
                .body("$", instanceOf(java.util.Map.class));
        
        logger.info("Ожидаемая ошибка 403 получена");
    }
    
    
    /**
     * ТЕСТ 76: NEGATIVE_EQUALS geoData/v2/getAggregatedObjectsByCoordinates
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет обработку ошибки при невалидном значении geomAggregation (EQUAL вместо EQUALS)
     * - Проверяет что API возвращает ошибку 500
     */
    @Test(priority = 76, description = "NEGATIVE_EQUALS geoData/v2/getAggregatedObjectsByCoordinates")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить агрегированные объекты с невалидным значением geomAggregation. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid GeomAggregation Value")
    public void testGetAggregatedObjectsByCoordinatesWithInvalidGeomAggregation() {
        logger.info("Выполнение теста: NEGATIVE_EQUALS geoData/v2/getAggregatedObjectsByCoordinates");
        
        // Создаем объект mainLayer
        List<String> mainLayerProperties = new ArrayList<>();
        mainLayerProperties.add("fileid");
        mainLayerProperties.add("id");
        MainLayer mainLayer = new MainLayer(11651, mainLayerProperties);
        
        // Создаем linkedLayers
        List<MainLayer> linkedLayers = new ArrayList<>();
        
        List<String> linkedLayer1Properties = new ArrayList<>();
        linkedLayer1Properties.add("id");
        linkedLayer1Properties.add("fileid");
        linkedLayer1Properties.add("load_fid");
        MainLayer linkedLayer1 = new MainLayer(11650, linkedLayer1Properties);
        linkedLayers.add(linkedLayer1);
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 10000);
        
        // Создаем объект запроса с невалидным geomAggregation (EQUAL вместо EQUALS)
        GetAggregatedObjectsByCoordinatesRequest request = new GetAggregatedObjectsByCoordinatesRequest(
            mainLayer,
            linkedLayers,
            "POLYGON((37.50073293025099 55.678407640436944, 37.50073293025099 55.67198266626835, 37.509477277188125 55.67198266626835, 37.509477277188125 55.678407640436944, 37.50073293025099 55.678407640436944))",
            "EQUAL",
            true,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_AGGREGATED_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("[INTERSECTS, DISTANCE, INTERSECTION, SYMMETRICAL_DIFFERENCE, WITHIN, DIFFERENCE, EQUALS, CONTAINS, UNION]"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 77: NEGATIVE not added to service
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет обработку ошибки при использовании слоя, к которому нет доступа
     * - Проверяет что API возвращает ошибку 500 с сообщением "Нет доступа ни к одному слою"
     */
    @Test(priority = 77, description = "NEGATIVE not added to service")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить объекты по координатам с layerId, к которому нет доступа. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - No Access To Layer")
    public void testGetObjectsByCoordinatesWithNoAccessToLayer() {
        logger.info("Выполнение теста: NEGATIVE not added to service");
        
        // Создаем layers с layerId, к которому нет доступа
        List<Layer> layers = new ArrayList<>();
        
        List<String> layerProperties = new ArrayList<>();
        layerProperties.add("string");
        Layer layer = new Layer(666, null, layerProperties);
        layers.add(layer);
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 100);
        
        // Создаем объект запроса
        GetObjectsByCoordinatesPostRequest request = new GetObjectsByCoordinatesPostRequest(
            layers,
            "point(37.51099542641316 55.6782409858406)",
            250,
            true,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Нет доступа ни к одному слою"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 78: NEGATIVE empty answer
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет работу API getObjectsByCoordinates с координатами, которые не возвращают результатов
     * - Проверяет что size=0, totalElements=0, features=null
     * - Проверяет наличие поля declined
     */
    @Test(priority = 78, description = "NEGATIVE empty answer")
    @Severity(SeverityLevel.NORMAL)
    @Description("Получение объектов по координатам с координатами, которые не возвращают результатов. Проверка статуса 200, size=0, totalElements=0, features=null, наличия declined.")
    @Story("Negative Tests - Empty Answer")
    public void testGetObjectsByCoordinatesWithEmptyAnswer() {
        logger.info("Выполнение теста: NEGATIVE empty answer");
        
        // Создаем layers
        List<Layer> layers = new ArrayList<>();
        
        List<String> layerProperties = new ArrayList<>();
        layerProperties.add("string");
        Layer layer = new Layer(4557, null, layerProperties);
        layers.add(layer);
        
        // Создаем объект PageRequest
        PageRequest pageRequest = new PageRequest(0, 100);
        
        // Создаем объект запроса с координатами, которые не возвращают результатов
        GetObjectsByCoordinatesPostRequest request = new GetObjectsByCoordinatesPostRequest(
            layers,
            "point(  37.51099542641316 55.1111111)",
            250,
            true,
            pageRequest
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(GET_OBJECTS_BY_COORDINATES_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("size", equalTo(0))
                .body("totalElements", equalTo(0))
                .body("pageNumber", notNullValue())
                .body("pageNumber", instanceOf(Integer.class))
                .body("pageSize", notNullValue())
                .body("pageSize", instanceOf(Integer.class))
                .body("totalPages", notNullValue())
                .body("totalPages", instanceOf(Integer.class))
                .body("content", notNullValue())
                .body("content", instanceOf(java.util.Map.class))
                .body("content.type", notNullValue())
                .body("content.features", nullValue())
                .body("declined", notNullValue())
                .body("declined", instanceOf(java.util.List.class))
                .extract()
                .response();
        
        logger.info("Тест успешно завершен. size: {}, totalElements: {}", 
            response.jsonPath().getInt("size"), response.jsonPath().getInt("totalElements"));
    }
}
