package com.dt.tests;

import com.dt.base.BaseTest;
import com.dt.base.Config;
import com.dt.base.SensitiveDataFilter;
import com.dt.model.metrics.*;
import io.qameta.allure.*;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * КЛАСС С ТЕСТАМИ ДЛЯ API METRICS (МЕТРИКИ)
 * 
 * ЗАЧЕМ НУЖЕН ЭТОТ КЛАСС:
 * - Содержит все тесты для API работы с метриками (очистка, отправка, проверка)
 * - Наследуется от BaseTest - получает все методы для работы с API
 * - Полностью независим от CustomerApiTest - не затрагивает тесты Customer
 * 
 * КАК РАБОТАЕТ:
 * - TestNG находит методы с аннотацией @Test
 * - Выполняет их в порядке priority (1, 2, 3...)
 * - @BeforeClass выполняется один раз перед всеми тестами
 * 
 * АННОТАЦИИ ALLURE:
 * - @Epic - большая группа тестов (например, "Metrics API Testing")
 * - @Feature - функциональность (например, "Metrics Management")
 */
@Epic("Metrics API Testing")
@Feature("Metrics Management")
public class MetricsApiTest extends BaseTest {
    
    /**
     * БАЗОВЫЙ URL API ПО УМОЛЧАНИЮ
     * 
     * ВАЖНО: Metrics API использует другой base URL, чем Customer API:
     * - Customer API: задаётся через baseUrl
     * - Metrics API: задаётся через baseUrl (обычно отдельный префикс)
     */
    // Публичный репозиторий: не храним здесь URL внутренних стендов.
    // Для запуска задайте baseUrl через -DbaseUrl или переменную окружения baseUrl.
    private static final String DEFAULT_BASE_URL = "https://example.com";
    
    /**
     * ENDPOINT (ПУТЬ) ДЛЯ РАБОТЫ С МЕТРИКАМИ
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Путь к API для работы с метриками
     * - Используется в запросах: BASE_URL + METRICS_ENDPOINT = полный URL
     * - ВАЖНО: Metrics API не использует префикс /da-cm-map-backend-manager-intersect-object/
     *   в отличие от других API (Customer, Widget и т.д.)
     */
    private static final String METRICS_ENDPOINT = "/metrics";
    
    /**
     * ENDPOINT (ПУТЬ) ДЛЯ ЧТЕНИЯ МЕТРИК
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Путь к API для чтения метрик
     * - Используется в запросах: BASE_URL + METRICS_READ_ENDPOINT = полный URL
     */
    private static final String METRICS_READ_ENDPOINT = "/metrics/read";
    
    /**
     * ENDPOINT (ПУТЬ) ДЛЯ ДОБАВЛЕНИЯ SUMMARY К СЕССИИ
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Путь к API для добавления summary к сессии
     * - Используется в запросах: BASE_URL + METRICS_SESSION_SUMMARY_ENDPOINT = полный URL
     */
    private static final String METRICS_SESSION_SUMMARY_ENDPOINT = "/metrics/session/summary";
    
    /**
     * ENDPOINT (ПУТЬ) ДЛЯ ДОБАВЛЕНИЯ АКТИВНОЙ СЕССИИ
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Путь к API для добавления активной сессии
     * - Используется в запросах: BASE_URL + METRICS_SESSION_START_ENDPOINT = полный URL
     */
    private static final String METRICS_SESSION_START_ENDPOINT = "/metrics/session/start";
    
    /**
     * ENDPOINT (ПУТЬ) ДЛЯ ЗАВЕРШЕНИЯ СЕССИИ
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Путь к API для завершения сессии
     * - Используется в запросах: BASE_URL + METRICS_SESSION_FINISH_ENDPOINT = полный URL
     */
    private static final String METRICS_SESSION_FINISH_ENDPOINT = "/metrics/session/finish";
    
    /**
     * ENDPOINT (ПУТЬ) ДЛЯ ДОБАВЛЕНИЯ МЕТРИК ЭТАПА
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Путь к API для добавления метрик этапа
     * - Используется в запросах: BASE_URL + METRICS_SESSION_STAGE_ENDPOINT = полный URL
     */
    private static final String METRICS_SESSION_STAGE_ENDPOINT = "/metrics/session/stage";
    
    /**
     * ENDPOINT (ПУТЬ) ДЛЯ НАСТРОЙКИ ОКРУЖЕНИЯ СЕССИИ
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Путь к API для настройки окружения сессии
     * - Используется в запросах: BASE_URL + METRICS_SESSION_ENV_ENDPOINT = полный URL
     */
    private static final String METRICS_SESSION_ENV_ENDPOINT = "/metrics/session/env";

    /**
     * ENDPOINT (ПУТЬ) ДЛЯ ПОИСКА МЕТРИК
     *
     * POST /metrics-info/search
     */
    private static final String METRICS_INFO_SEARCH_ENDPOINT = "/metrics-info/search";

    /**
     * GET /metrics-info/users
     */
    private static final String METRICS_INFO_USERS_ENDPOINT = "/metrics-info/users";

    /**
     * GET /metrics-info/session/{sessionId}
     */
    private static final String METRICS_INFO_SESSION_ENDPOINT = "/metrics-info/session";

    /**
     * GET /metrics-info/presets
     */
    private static final String METRICS_INFO_PRESETS_ENDPOINT = "/metrics-info/presets";

    /**
     * GET /metrics-info/enigma
     */
    private static final String METRICS_INFO_ENIGMA_ENDPOINT = "/metrics-info/enigma";

    /**
     * GET /metrics-info/archive
     */
    private static final String METRICS_INFO_ARCHIVE_ENDPOINT = "/metrics-info/archive";
    
    /**
     * Переменные для хранения данных между тестами
     */
    private String generatedSessionId;
    private String generatedStartedAt;
    private String activeSessionId;
    private String activeSessionStartedAt;
    private Integer startupPageLoadSecValue;
    private Integer stageNumberValue;
    private String foundSessionIdAutotest;
    private String foundStartedAtAutotest;
    
    /**
     * ИНИЦИАЛИЗАЦИЯ ПЕРЕД ВСЕМИ ТЕСТАМИ
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Настраивает базовый URL и куки для всех тестов
     * - Используется для общей настройки (URL, куки, подключения и т.д.)
     * 
     * КОГДА ВЫЗЫВАЕТСЯ:
     * - TestNG вызывает этот метод автоматически перед первым тестом
     * - Выполняется только один раз, даже если тестов много
     */
    @BeforeClass
    public void setUp() {
        // Устанавливаем базовый URL для всех запросов
        // Используем Config.getBaseUrl() для чтения из переменных окружения или системных переменных
        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        setBaseUrl(baseUrl);
        
        // Получаем куку из переменной окружения или системной переменной
        // Используем ту же куку, что и в CustomerApiTest
        String cookieValue = Config.getCookie();
        
        // Если кука найдена - устанавливаем её
        if (cookieValue != null && !cookieValue.isEmpty()) {
            setCookie("cookie", cookieValue);
            logger.info("Cookie установлена из переменной окружения (маскированная): {}", SensitiveDataFilter.maskForLogging(cookieValue));
        } else {
            logger.error("КРИТИЧЕСКАЯ ОШИБКА: Cookie не установлена! Установите переменную окружения 'cookie' или системную переменную -Dcookie=value");
            logger.error("Пример запуска: mvn test -Dtest=MetricsApiTest -Dcookie=\"your-cookie-value\"");
        }
        
        logger.info("Инициализация тестового класса MetricsApiTest. Base URL: {}", baseUrl);
    }
    
    /**
     * Генерация UUID для sessionId
     * 
     * @return UUID в формате строки
     */
    private String generateUUID() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Генерация текущего времени в формате ISO 8601
     * 
     * @return строка с текущим временем в формате ISO 8601
     */
    private String getCurrentTimeISO() {
        // Делаем как в Postman Date().toISOString(): стабильные миллисекунды
        return Instant.now().truncatedTo(ChronoUnit.MILLIS).toString();
    }
    
    /**
     * Нормализация даты для сравнения (убирает trailing zeros после точки)
     * Backend может возвращать "2026-01-28T09:09:47.02" вместо "2026-01-28T09:09:47.020"
     * 
     * @param dateStr строка с датой
     * @return нормализованная строка без trailing zeros
     */
    private String normalizeDateForComparison(String dateStr) {
        if (dateStr == null) return null;
        // Убираем Z в конце если есть
        String normalized = dateStr.replaceAll("Z$", "");
        // Убираем trailing zeros после точки (но оставляем точку если есть дробная часть)
        if (normalized.contains(".")) {
            normalized = normalized.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return normalized;
    }

    /**
     * Генерация сегодняшней даты в формате YYYY-MM-DD
     */
    private String getTodayDate() {
        return LocalDate.now().toString();
    }
    
    /**
     * Создание базового объекта Session с минимальными данными
     * 
     * @param sessionId ID сессии
     * @param startedAt время начала сессии
     * @return объект Session
     */
    private Session createBasicSession(String sessionId, String startedAt) {
        Session session = new Session();
        session.setSessionId(sessionId);
        session.setUserName("autotest");
        session.setPresetName("autotest");
        session.setEnded(true);
        session.setStartedAt(startedAt);
        
        // Создаем базовое окружение
        Environment environment = new Environment();
        environment.setOs("string");
        environment.setRamGb(0);
        environment.setCpu("string");
        environment.setGpu("string");
        environment.setBrowser("string");
        environment.setBrowserVersion("string");
        
        List<Display> displays = new ArrayList<>();
        displays.add(new Display(0, 0));
        environment.setDisplays(displays);
        
        List<Display> viewer = new ArrayList<>();
        viewer.add(new Display(0, 0));
        environment.setViewer(viewer);
        
        session.setEnvironment(environment);
        
        // Создаем базовые данные
        session.setLayers(new ArrayList<>());
        session.getLayers().add("string");
        
        List<AppVersion> appVersions = new ArrayList<>();
        appVersions.add(new AppVersion("string", "string", "7.8.2025"));
        session.setAppVersions(appVersions);
        
        // Создаем базовый summary
        Summary summary = createBasicSummary();
        session.setSummary(summary);
        session.setSummaryAlert(summary);
        
        // Создаем stagesSummary с хотя бы одним Stage (API требует непустой список)
        List<Stage> stagesSummary = new ArrayList<>();
        stagesSummary.add(createBasicStage());
        session.setStagesSummary(stagesSummary);
        
        // stagesAlert может быть пустым
        session.setStagesAlert(new ArrayList<>());
        
        return session;
    }
    
    /**
     * Создание упрощенного объекта Session для endpoint /session/start
     * (без summary, stagesSummary, stagesAlert)
     * 
     * @param sessionId ID сессии
     * @param userName имя пользователя
     * @param presetName имя пресета
     * @param startedAt время начала сессии
     * @return объект Session
     */
    private Session createSimpleSessionForStart(String sessionId, String userName, String presetName, String startedAt) {
        Session session = new Session();
        session.setSessionId(sessionId);
        session.setUserName(userName);
        session.setPresetName(presetName);
        session.setEnded(true);
        session.setStartedAt(startedAt);
        
        // Создаем базовое окружение
        Environment environment = new Environment();
        environment.setOs("string");
        environment.setRamGb(0);
        environment.setCpu("string");
        environment.setGpu("string");
        environment.setBrowser("string");
        environment.setBrowserVersion("string");
        
        List<Display> displays = new ArrayList<>();
        displays.add(new Display(0, 0));
        environment.setDisplays(displays);
        
        List<Display> viewer = new ArrayList<>();
        viewer.add(new Display(0, 0));
        environment.setViewer(viewer);
        
        session.setEnvironment(environment);
        
        // Для /session/start не нужны summary, stagesSummary, stagesAlert
        return session;
    }
    
    /**
     * Генерация времени минус 3 часа от текущего момента
     * 
     * @return строка с временем в формате ISO 8601
     */
    private String getStartedAtMinus3Hours() {
        Instant now = Instant.now();
        Instant threeHoursAgo = now.minusSeconds(3 * 60 * 60);
        return threeHoursAgo.toString();
    }
    
    /**
     * Проверка что ответ не является HTML страницей логина
     * Если получена HTML страница - значит кука не работает
     * 
     * @param responseBody тело ответа
     * @throws AssertionError если получена HTML страница логина
     */
    private void checkNotLoginPage(String responseBody) {
        if (responseBody != null && (responseBody.contains("<!DOCTYPE html>") || 
                                     responseBody.contains("login-pf") || 
                                     responseBody.contains("kc-form-login"))) {
            logger.error("ОШИБКА: Получена HTML страница логина вместо JSON!");
            logger.error("Это означает, что кука не передается или невалидна.");
            logger.error("Проверьте что кука установлена: mvn test -Dtest=MetricsApiTest -Dcookie=\"your-cookie-value\"");
            throw new AssertionError("Получена HTML страница логина. Кука не работает или невалидна. " +
                                   "Установите куку через -Dcookie=\"value\" или переменную окружения 'cookie'");
        }
    }
    
    /**
     * Создание базового объекта Summary
     * 
     * @return объект Summary
     */
    private Summary createBasicSummary() {
        Summary summary = new Summary();
        summary.setStartupPageLoadSec(0.0);
        summary.setFirstDataLoadSec(0.0);
        summary.setLayerTreeLoadSec(0.0);
        summary.setFpsAvg(0.0);
        summary.setFpsAboveNormPercent(0.0);
        summary.setFpsNormPercent(0.0);
        summary.setFpsBelowNormPercent(0.0);
        summary.setDrawCallsTotal(0);
        summary.setDownloadSpeedAvgMbps(0.0);
        summary.setRequestsTotal(0);
        summary.setDataVolumeTotalMb(0.0);
        
        Timing timing = new Timing();
        timing.setTotalSeconds(0.0);
        timing.setQueueingPercent(0.0);
        timing.setStalledPercent(0.0);
        timing.setWaitingTtfbPercent(0.0);
        timing.setReceivePercent(0.0);
        summary.setTiming(timing);
        
        List<DataComposition> dataComposition = new ArrayList<>();
        DataComposition comp = new DataComposition();
        comp.setComponent("string");
        comp.setDrawCallsPercent(0.0);
        comp.setQueueingPercent(0.0);
        comp.setStalledPercent(0.0);
        comp.setWaitingTtfbPercent(0.0);
        comp.setReceivePercent(0.0);
        comp.setRequestsPercent(0.0);
        comp.setDataVolumePercent(0.0);
        dataComposition.add(comp);
        summary.setDataComposition(dataComposition);
        
        return summary;
    }
    
    /**
     * Создание базового объекта Stage
     * 
     * @return объект Stage
     */
    private Stage createBasicStage() {
        Stage stage = new Stage();
        stage.setStageNumber(1);
        stage.setStageName("test_stage");
        
        // Создаем базовый FPS
        Fps fps = new Fps();
        fps.setAvgFps(0.0);
        fps.setBelowNormPercent(0.0);
        fps.setNormPercent(0.0);
        fps.setAboveNormPercent(0.0);
        stage.setFps(fps);
        
        // Создаем базовый Timing
        Timing timing = new Timing();
        timing.setTotalSeconds(0.0);
        timing.setQueueingPercent(0.0);
        timing.setStalledPercent(0.0);
        timing.setWaitingTtfbPercent(0.0);
        timing.setReceivePercent(0.0);
        stage.setTiming(timing);
        
        // Создаем базовый CameraPosition (API требует не null)
        CameraPosition cameraPosition = new CameraPosition();
        cameraPosition.setX(0.0);
        cameraPosition.setY(0.0);
        cameraPosition.setZ(0.0);
        cameraPosition.setD(0.0);
        cameraPosition.setHeading(0.0);
        cameraPosition.setPitch(0.0);
        cameraPosition.setRoll(0.0);
        stage.setCameraPosition(cameraPosition);
        
        // Создаем базовый DownloadSpeed (API требует не null)
        DownloadSpeed downloadSpeed = new DownloadSpeed();
        downloadSpeed.setMbps(0.0);
        stage.setDownloadSpeed(downloadSpeed);
        
        // Остальные поля могут быть пустыми
        stage.setMetrics(new ArrayList<>());
        stage.setRequests(new ArrayList<>());
        
        return stage;
    }
    
    /**
     * ТЕСТ 1: ОЧИСТКА МЕТРИК (POSITIVE TEST)
     * 
     * GET /metrics
     * Проверяет что API позволяет очистить метрики
     */
    @Test(priority = 1, description = "GET /metrics - очистка метрик")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка очистки метрик через GET /metrics")
    @Story("Metrics Cleanup")
    public void testClearMetrics() {
        logger.info("=== ТЕСТ: Очистка метрик ===");
        
        // Проверяем что кука установлена
        String cookieValue = Config.getCookie();
        if (cookieValue == null || cookieValue.isEmpty()) {
            logger.error("КРИТИЧЕСКАЯ ОШИБКА: Cookie не установлена! Установите переменную окружения 'cookie' или системную переменную -Dcookie=value");
            throw new IllegalStateException("Cookie не установлена. Установите переменную окружения 'cookie' или системную переменную -Dcookie=value");
        }
        
        // Логируем URL для отладки
        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_ENDPOINT);
        logger.info("Cookie установлена: {}", SensitiveDataFilter.maskForLogging(cookieValue));
        
        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl) // Явно указываем baseUri в запросе
                .when()
                .get(METRICS_ENDPOINT)
                .then()
                .statusCode(200)
                .extract()
                .response();
        
        // Проверяем что ответ не HTML страница логина
        String responseBody = response.getBody().asString();
        checkNotLoginPage(responseBody);
        
        logger.info("Метрики успешно очищены");
    }
    
    /**
     * ТЕСТ 2: ОТПРАВКА МЕТРИК (POSITIVE TEST)
     * 
     * POST /metrics
     * Проверяет что API позволяет отправить метрики
     * Генерирует UUID для sessionId и текущее время для startedAt
     */
    @Test(priority = 2, description = "POST /metrics - отправка метрик")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка отправки метрик через POST /metrics с автоматической генерацией sessionId и startedAt")
    @Story("Metrics Submission")
    public void testSubmitMetrics() {
        logger.info("=== ТЕСТ: Отправка метрик ===");
        
        // Генерируем UUID для sessionId
        generatedSessionId = generateUUID();
        logger.info("Сгенерированный sessionId: {}", generatedSessionId);
        
        // Генерируем текущее время для startedAt
        generatedStartedAt = getCurrentTimeISO();
        logger.info("Сгенерированное startedAt: {}", generatedStartedAt);
        
        // Создаем сессию с метриками
        Session session = createBasicSession(generatedSessionId, generatedStartedAt);
        MetricsRequest request = new MetricsRequest(session);
        
        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_ENDPOINT);
        
        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl) // Явно указываем baseUri в запросе
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(METRICS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(200), is(201))) // Проверяем успешный статус код
                .extract()
                .response();
        
        // Проверяем что ответ не HTML страница логина
        String responseBody = response.getBody().asString();
        checkNotLoginPage(responseBody);
        
        logger.info("Метрики успешно отправлены. SessionId: {}, StartedAt: {}", generatedSessionId, generatedStartedAt);
    }
    
    /**
     * ТЕСТ 3: NEGATIVE - ОТПРАВКА МЕТРИК БЕЗ ОБЯЗАТЕЛЬНЫХ ПОЛЕЙ
     * 
     * POST /metrics
     * Проверяет валидацию обязательных полей
     */
    @Test(priority = 3, description = "NEGATIVE POST /metrics - отсутствуют обязательные поля")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверка валидации обязательных полей при отправке метрик")
    @Story("Metrics Validation")
    public void testSubmitMetricsWithoutRequiredFields() {
        logger.info("=== ТЕСТ: Отправка метрик без обязательных полей ===");
        
        // Создаем сессию без обязательных полей (sessionId, userName, presetName, startedAt)
        Session session = new Session();
        session.setEnded(true);
        
        // Создаем только environment и другие необязательные поля
        Environment environment = new Environment();
        environment.setOs("string");
        environment.setRamGb(0);
        environment.setCpu("string");
        environment.setGpu("string");
        environment.setBrowser("string");
        environment.setBrowserVersion("string");
        
        List<Display> displays = new ArrayList<>();
        displays.add(new Display(0, 0));
        environment.setDisplays(displays);
        
        List<Display> viewer = new ArrayList<>();
        viewer.add(new Display(0, 0));
        environment.setViewer(viewer);
        
        session.setEnvironment(environment);
        session.setLayers(new ArrayList<>());
        session.getLayers().add("string");
        
        List<AppVersion> appVersions = new ArrayList<>();
        appVersions.add(new AppVersion("string", "string", "7.8.2025"));
        session.setAppVersions(appVersions);
        
        Summary summary = createBasicSummary();
        session.setSummary(summary);
        session.setSummaryAlert(summary);
        session.setStagesSummary(new ArrayList<>());
        session.setStagesAlert(new ArrayList<>());
        
        MetricsRequest request = new MetricsRequest(session);
        
        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_ENDPOINT);
        
        // Простые проверки через .body() в цепочке
        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl) // Явно указываем baseUri в запросе
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(METRICS_ENDPOINT)
                .then()
                .statusCode(400)
                .body(not(emptyString())) // Проверяем что ответ не пустой
                .extract()
                .response();
        
        logger.info("Статус код: {}", response.getStatusCode());
        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);
        
        // Проверяем что ответ не HTML страница логина
        checkNotLoginPage(responseBody);
        
        // Сложные проверки с множественными условиями (OR) - через TestNG Assert
        Assert.assertTrue(
                responseBody.contains("session.presetName") || responseBody.contains("presetName"),
                "Ответ должен содержать ошибку для session.presetName"
        );
        Assert.assertTrue(
                responseBody.contains("session.startedAt") || responseBody.contains("startedAt"),
                "Ответ должен содержать ошибку для session.startedAt"
        );
        Assert.assertTrue(
                responseBody.contains("session.userName") || responseBody.contains("userName"),
                "Ответ должен содержать ошибку для session.userName"
        );
    }
    
    /**
     * ТЕСТ 4: NEGATIVE - ОТПРАВКА НЕВАЛИДНОГО JSON
     * 
     * POST /metrics
     * Проверяет обработку невалидного JSON
     */
    @Test(priority = 4, description = "NEGATIVE POST /metrics - невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверка обработки невалидного JSON при отправке метрик")
    @Story("Metrics Validation")
    public void testSubmitMetricsWithInvalidJson() {
        logger.info("=== ТЕСТ: Отправка невалидного JSON ===");
        
        // Создаем невалидный JSON (отсутствует закрывающая фигурная скобка)
        String invalidJson = "{\n" +
                "  \"session\": {\n" +
                "    \"sessionId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\n" +
                "    \"userName\": \"autotest\",\n" +
                "    \"presetName\": \"autotest\",\n" +
                "    \"ended\": true,\n" +
                "    \"startedAt\": \"2026-01-26T09:56:59.003Z\"";
        
        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_ENDPOINT);
        
        // Простые проверки через .body() в цепочке
        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl) // Явно указываем baseUri в запросе
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .post(METRICS_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class)) // Проверяем что ответ - валидный JSON объект
                .body("message", notNullValue()) // Поле "message" существует
                .body("message", instanceOf(String.class)) // "message" - строка
                .body("message", containsString("JSON parse error")) // Сообщение содержит текст
                .extract()
                .response();
        
        logger.info("Статус код: {}", response.getStatusCode());
        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);
        
        // Проверяем что ответ не HTML страница логина
        checkNotLoginPage(responseBody);
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 5: ПРОВЕРКА ОТПРАВЛЕННЫХ МЕТРИК
     * 
     * GET /metrics/read
     * Проверяет наличие отправленных метрик
     */
    @Test(priority = 5, description = "GET /metrics/read - проверка отправленных метрик")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка наличия отправленных метрик через GET /metrics/read")
    @Story("Metrics Verification")
    public void testReadMetrics() {
        logger.info("=== ТЕСТ: Проверка отправленных метрик ===");
        
        // Проверяем что у нас есть данные из предыдущего теста
        if (generatedSessionId == null || generatedStartedAt == null) {
            logger.warn("Данные из предыдущего теста отсутствуют, пропускаем проверку");
            return;
        }
        
        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_READ_ENDPOINT);
        
        // Простые проверки через .body() в цепочке
        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl) // Явно указываем baseUri в запросе
                .when()
                .get(METRICS_READ_ENDPOINT)
                .then()
                .statusCode(200)
                .body(not(emptyString())) // Проверяем что ответ не пустой
                .body(containsString("user_name=\"autotest\"")) // Проверяем наличие user_name
                .body(containsString("preset_name=\"autotest\"")) // Проверяем наличие preset_name
                .body(containsString("session_id=\"")) // Проверяем наличие session_id
                .extract()
                .response();
        
        String responseBody = response.getBody().asString();
        logger.info("Статус код: {}", response.getStatusCode());
        logger.info("Ответ: {}", responseBody);
        
        // Проверяем что ответ не HTML страница логина
        checkNotLoginPage(responseBody);
        
        // Сложные проверки (извлечение session_id, форматирование даты) - через extract + Assert
        Pattern sessionIdPattern = Pattern.compile("session_id=\"([^\"]+)\"");
        Matcher sessionIdMatcher = sessionIdPattern.matcher(responseBody);
        Assert.assertTrue(sessionIdMatcher.find(), "Ответ должен содержать session_id");
        String foundSessionId = sessionIdMatcher.group(1);
        logger.info("Найденный session_id: {}", foundSessionId);
        // Сохраняем для последующих тестов (как foundSessionId в Postman)
        foundSessionIdAutotest = foundSessionId;
        
        // Проверяем наличие started_at (используем регулярное выражение для гибкой проверки)
        // API возвращает формат без Z, формат может отличаться по количеству цифр после точки
        // Извлекаем базовую часть даты (до точки с миллисекундами) для проверки
        String baseDate = generatedStartedAt.replace("Z", "");
        if (baseDate.contains(".")) {
            baseDate = baseDate.substring(0, baseDate.indexOf("."));
        }
        // Проверяем что started_at содержит нашу дату (год, месяц, день, час, минута, секунда)
        Pattern startedAtPattern = Pattern.compile("started_at=\"([^\"]+)\"");
        Matcher startedAtMatcher = startedAtPattern.matcher(responseBody);
        Assert.assertTrue(startedAtMatcher.find(), "Ответ должен содержать started_at");
        String foundStartedAt = startedAtMatcher.group(1);
        Assert.assertTrue(
                foundStartedAt.startsWith(baseDate),
                "started_at должен начинаться с нашей даты: " + baseDate + ", но найден: " + foundStartedAt
        );
        logger.info("Найденный started_at: {}", foundStartedAt);
        // Сохраняем startedAt в том виде, как его реально отдает API (без Z)
        foundStartedAtAutotest = foundStartedAt;
    }
    
    /**
     * ТЕСТ 6: ПОВТОРНАЯ ОЧИСТКА МЕТРИК
     * 
     * GET /metrics
     * Проверяет наличие метрик перед очисткой
     */
    @Test(priority = 6, description = "GET /metrics - повторная очистка метрик")
    @Severity(SeverityLevel.NORMAL)
    @Description("Повторная очистка метрик с проверкой наличия метрик перед очисткой")
    @Story("Metrics Cleanup")
    public void testClearMetricsAgain() {
        logger.info("=== ТЕСТ: Повторная очистка метрик ===");
        
        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_ENDPOINT);
        
        // Простые проверки через .body() в цепочке
        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl) // Явно указываем baseUri в запросе
                .when()
                .get(METRICS_ENDPOINT)
                .then()
                .statusCode(200)
                .body(not(emptyString())) // Проверяем что ответ не пустой
                .body(containsString("user_name=\"autotest\"")) // Проверяем наличие user_name
                .body(containsString("preset_name=\"autotest\"")) // Проверяем наличие preset_name
                .extract()
                .response();
        
        String responseBody = response.getBody().asString();
        logger.info("Статус код: {}", response.getStatusCode());
        logger.info("Ответ: {}", responseBody);
        
        // Проверяем что ответ не HTML страница логина
        checkNotLoginPage(responseBody);
        
        // Сложные проверки (условная проверка с форматированием даты) - через extract + assert
        if (generatedStartedAt != null) {
            // API возвращает формат без Z, формат может отличаться по количеству цифр после точки
            // Извлекаем базовую часть даты (до точки с миллисекундами) для проверки
            String baseDate = generatedStartedAt.replace("Z", "");
            if (baseDate.contains(".")) {
                baseDate = baseDate.substring(0, baseDate.indexOf("."));
            }
            // Проверяем что started_at содержит нашу дату (год, месяц, день, час, минута, секунда)
            Pattern startedAtPattern = Pattern.compile("started_at=\"([^\"]+)\"");
            Matcher startedAtMatcher = startedAtPattern.matcher(responseBody);
            Assert.assertTrue(startedAtMatcher.find(), "Ответ должен содержать started_at");
            String foundStartedAt = startedAtMatcher.group(1);
            Assert.assertTrue(
                    foundStartedAt.startsWith(baseDate),
                    "started_at должен начинаться с нашей даты: " + baseDate + ", но найден: " + foundStartedAt
            );
            logger.info("Найденный started_at: {}", foundStartedAt);
        }
    }
    
    /**
     * ТЕСТ 7: NEGATIVE - ДОБАВЛЕНИЕ SUMMARY К НЕСУЩЕСТВУЮЩЕЙ СЕССИИ
     * 
     * POST /metrics/session/summary
     * Проверяет обработку попытки добавить summary к несуществующей сессии
     */
    @Test(priority = 7, description = "NEGATIVE POST /metrics/session/summary - добавление summary к несуществующей сессии")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверка обработки попытки добавить summary к несуществующей сессии")
    @Story("Metrics Validation")
    public void testAddSummaryToNonExistentSession() {
        logger.info("=== ТЕСТ: Добавление summary к несуществующей сессии ===");
        
        // Генерируем рандомное число от 1 до 20 для startupPageLoadSec
        int randomValue = (int)(Math.random() * 20) + 1;
        logger.info("Сгенерированное значение startupPageLoadSec: {}", randomValue);
        
        // Создаем summary с рандомным значением
        Summary summaryAlert = createBasicSummary();
        summaryAlert.setStartupPageLoadSec((double)randomValue);
        
        // Создаем запрос с несуществующим sessionId
        SessionSummaryRequest request = new SessionSummaryRequest();
        request.setSessionId("c6584cf4-679a-4ef6-b428-24765726b8f9");
        request.setSummary(createBasicSummary());
        request.setSummaryAlert(summaryAlert);
        
        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_SESSION_SUMMARY_ENDPOINT);
        
        // Простые проверки через .body() в цепочке
        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl) // Явно указываем baseUri в запросе
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(METRICS_SESSION_SUMMARY_ENDPOINT)
                .then()
                .statusCode(500)
                .body(not(emptyString())) // Проверяем что ответ не пустой
                .extract()
                .response();
        
        logger.info("Статус код: {}", response.getStatusCode());
        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);
        
        // Проверяем что ответ не HTML страница логина
        checkNotLoginPage(responseBody);
        
        // Сложные проверки с множественными условиями (OR) - через TestNG Assert
        Assert.assertTrue(
                responseBody.contains("message") ||
                responseBody.contains("does not exist") ||
                responseBody.contains("not found"),
                "Ответ должен содержать ошибку о несуществующей сессии"
        );
    }
    
    /**
     * ТЕСТ 8: ДОБАВЛЕНИЕ АКТИВНОЙ СЕССИИ
     * 
     * POST /metrics/session/start
     * Проверяет что API позволяет добавить активную сессию
     */
    @Test(priority = 8, description = "POST /metrics/session/start - добавление активной сессии")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка добавления активной сессии через POST /metrics/session/start")
    @Story("Active Session Management")
    public void testAddActiveSession() {
        logger.info("=== ТЕСТ: Добавление активной сессии ===");
        
        // Генерируем UUID для sessionId
        String sessionId = generateUUID();
        logger.info("Сгенерированный sessionId: {}", sessionId);
        
        // Генерируем фактическое время (как для первой метрики)
        String startedAt = getCurrentTimeISO();
        logger.info("Сгенерированное startedAt : {}", startedAt);
        // Сохраняем startedAt для дальнейших проверок (аналог startedAt2 в Postman)
        activeSessionStartedAt = startedAt;
        
        // Создаем упрощенную сессию для /session/start
        Session session = createSimpleSessionForStart(sessionId, "autotest_2", "autotest_2", startedAt);
        MetricsRequest request = new MetricsRequest(session);
        
        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_SESSION_START_ENDPOINT);
        
        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(METRICS_SESSION_START_ENDPOINT)
                .then()
                .statusCode(200)
                .body(not(emptyString()))
                .extract()
                .response();
        
        String responseBody = response.getBody().asString();
        logger.info("Статус код: {}", response.getStatusCode());
        logger.info("Ответ: {}", responseBody);
        
        // Проверяем что ответ не HTML страница логина
        checkNotLoginPage(responseBody);
        
        // Проверяем что ответ содержит id
        Assert.assertTrue(responseBody.contains("\"id\""), "Ответ должен содержать поле id");
        
        // Извлекаем id из ответа
        Pattern idPattern = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
        Matcher idMatcher = idPattern.matcher(responseBody);
        if (idMatcher.find()) {
            activeSessionId = idMatcher.group(1);
            logger.info("Полученный activeSessionId: {}", activeSessionId);
            Assert.assertTrue(
                    activeSessionId != null && !activeSessionId.isEmpty(),
                    "activeSessionId должен быть не пустым"
            );
        } else {
            throw new AssertionError("Не удалось извлечь id из ответа");
        }
    }
    
    /**
     * ТЕСТ 9: NEGATIVE - ОТПРАВКА НЕВАЛИДНОГО JSON В /session/start
     * 
     * POST /metrics/session/start
     * Проверяет обработку невалидного JSON
     */
    @Test(priority = 9, description = "NEGATIVE POST /metrics/session/start - невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверка обработки невалидного JSON при добавлении активной сессии")
    @Story("Metrics Validation")
    public void testAddActiveSessionWithInvalidJson() {
        logger.info("=== ТЕСТ: Отправка невалидного JSON в /session/start ===");
        
        // Создаем невалидный JSON (отсутствует закрывающая фигурная скобка)
        String invalidJson = "{\n" +
                "  \"session\": {\n" +
                "    \"sessionId\": \"" + generateUUID() + "\",\n" +
                "    \"userName\": \"autotest_2\",\n" +
                "    \"presetName\": \"autotest_2\"";
        
        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_SESSION_START_ENDPOINT);
        
        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .post(METRICS_SESSION_START_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class)) // Проверяем что ответ - валидный JSON объект
                .body("message", notNullValue()) // Поле "message" существует
                .body("message", instanceOf(String.class)) // "message" - строка
                .body("message", containsString("JSON parse error")) // Сообщение содержит текст
                .extract()
                .response();
        
        logger.info("Статус код: {}", response.getStatusCode());
        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);
        
        // Проверяем что ответ не HTML страница логина
        checkNotLoginPage(responseBody);
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 10: NEGATIVE - ОТСУТСТВУЮТ userName И presetName В /session/start
     * 
     * POST /metrics/session/start
     * Проверяет валидацию обязательных полей
     */
    @Test(priority = 10, description = "NEGATIVE POST /metrics/session/start - отсутствуют userName и presetName")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверка валидации обязательных полей при добавлении активной сессии")
    @Story("Metrics Validation")
    public void testAddActiveSessionWithoutRequiredFields() {
        logger.info("=== ТЕСТ: Отправка запроса без userName и presetName ===");
        
        // Создаем сессию без userName и presetName
        String sessionId = generateUUID();
        String startedAt = getStartedAtMinus3Hours();
        Session session = createSimpleSessionForStart(sessionId, null, null, startedAt);
        MetricsRequest request = new MetricsRequest(session);
        
        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_SESSION_START_ENDPOINT);
        
        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(METRICS_SESSION_START_ENDPOINT)
                .then()
                .statusCode(400)
                .body(not(emptyString()))
                .extract()
                .response();
        
        logger.info("Статус код: {}", response.getStatusCode());
        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);
        
        // Проверяем что ответ не HTML страница логина
        checkNotLoginPage(responseBody);
        
        // Проверяем наличие ошибок валидации
        Assert.assertTrue(
                responseBody.contains("session.userName") ||
                responseBody.contains("session.presetName") ||
                responseBody.contains("userName") ||
                responseBody.contains("presetName"),
                "Ответ должен содержать ошибку для userName или presetName"
        );
    }
    
    /**
     * ТЕСТ 11: ДОБАВЛЕНИЕ SUMMARY К АКТИВНОЙ СЕССИИ
     * 
     * POST /metrics/session/summary
     * Проверяет что API позволяет добавить summary к активной сессии
     */
    @Test(priority = 11, description = "POST /metrics/session/summary - добавление общих метрик сессии")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка добавления общих метрик сессии через POST /metrics/session/summary")
    @Story("Active Session Management")
    public void testAddSummaryToActiveSession() {
        logger.info("=== ТЕСТ: Добавление summary к активной сессии ===");
        
        // Проверяем что у нас есть activeSessionId из предыдущего теста
        if (activeSessionId == null || activeSessionId.isEmpty()) {
            logger.warn("activeSessionId отсутствует из предыдущего теста, пропускаем проверку");
            return;
        }
        
        // Генерируем рандомное число от 1 до 20 для startupPageLoadSec
        int randomValue = (int)(Math.random() * 20) + 1;
        logger.info("Сгенерированное значение startupPageLoadSec: {}", randomValue);
        // Сохраняем значение для дальнейшей проверки через /metrics/read (как startupPageLoadSec в Postman)
        startupPageLoadSecValue = randomValue;
        
        // Создаем summary с рандомным значением
        Summary summaryAlert = createBasicSummary();
        summaryAlert.setStartupPageLoadSec((double)randomValue);
        
        // Создаем запрос с activeSessionId
        SessionSummaryRequest request = new SessionSummaryRequest();
        request.setSessionId(activeSessionId);
        request.setSummary(createBasicSummary());
        request.setSummaryAlert(summaryAlert);
        
        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_SESSION_SUMMARY_ENDPOINT);
        logger.info("Используемый activeSessionId: {}", activeSessionId);
        
        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(METRICS_SESSION_SUMMARY_ENDPOINT)
                .then()
                .statusCode(anyOf(is(200), is(201))) // Проверяем успешный статус код
                .body("$", instanceOf(java.util.Map.class)) // Проверяем что ответ - валидный JSON объект
                .body("id", notNullValue()) // Поле "id" существует
                .body("id", instanceOf(String.class)) // "id" - строка
                .body("id", equalTo(activeSessionId)) // id равен activeSessionId
                .extract()
                .response();
        
        logger.info("Статус код: {}", response.getStatusCode());
        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);
        
        // Проверяем что ответ не HTML страница логина
        checkNotLoginPage(responseBody);
        
        // Дополнительная проверка через jsonPath
        String returnedId = response.jsonPath().getString("id");
        Assert.assertTrue(
                returnedId != null && returnedId.equals(activeSessionId),
                "Ответ должен содержать id равный activeSessionId. Ожидалось: " + activeSessionId + ", получено: " + returnedId
        );
        logger.info("Успешно добавлен summary к сессии с id: {}", returnedId);
    }
    
    /**
     * ТЕСТ 12: NEGATIVE - ОТПРАВКА НЕВАЛИДНОГО JSON В /session/summary
     * 
     * POST /metrics/session/summary
     * Проверяет обработку невалидного JSON
     */
    @Test(priority = 12, description = "NEGATIVE POST /metrics/session/summary - невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверка обработки невалидного JSON при добавлении summary")
    @Story("Metrics Validation")
    public void testAddSummaryWithInvalidJson() {
        logger.info("=== ТЕСТ: Отправка невалидного JSON в /session/summary ===");
        
        // Создаем невалидный JSON (отсутствует закрывающая фигурная скобка)
        String invalidJson = "{\n" +
                "  \"sessionId\": \"" + (activeSessionId != null ? activeSessionId : generateUUID()) + "\",\n" +
                "  \"summary\": {";
        
        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_SESSION_SUMMARY_ENDPOINT);
        
        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .post(METRICS_SESSION_SUMMARY_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class)) // Проверяем что ответ - валидный JSON объект
                .body("message", notNullValue()) // Поле "message" существует
                .body("message", instanceOf(String.class)) // "message" - строка
                .body("message", containsString("JSON parse error")) // Сообщение содержит текст
                .extract()
                .response();
        
        logger.info("Статус код: {}", response.getStatusCode());
        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);
        
        // Проверяем что ответ не HTML страница логина
        checkNotLoginPage(responseBody);
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 13: NEGATIVE - ОТСУТСТВУЕТ sessionId В /session/summary
     * 
     * POST /metrics/session/summary
     * Проверяет валидацию обязательного поля sessionId
     */
    @Test(priority = 13, description = "NEGATIVE POST /metrics/session/summary - отсутствует sessionId")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверка валидации обязательного поля sessionId при добавлении summary")
    @Story("Metrics Validation")
    public void testAddSummaryWithoutSessionId() {
        logger.info("=== ТЕСТ: Отправка запроса без sessionId ===");
        
        // Создаем запрос без sessionId
        SessionSummaryRequest request = new SessionSummaryRequest();
        // sessionId не устанавливаем (null)
        request.setSummary(createBasicSummary());
        request.setSummaryAlert(createBasicSummary());
        
        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_SESSION_SUMMARY_ENDPOINT);
        
        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(METRICS_SESSION_SUMMARY_ENDPOINT)
                .then()
                .statusCode(400)
                .body(not(emptyString()))
                .extract()
                .response();
        
        logger.info("Статус код: {}", response.getStatusCode());
        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);
        
        // Проверяем что ответ не HTML страница логина
        checkNotLoginPage(responseBody);
        
        // Проверяем наличие ошибки валидации для sessionId
        Assert.assertTrue(
                responseBody.contains("sessionId") ||
                responseBody.contains("must not be null"),
                "Ответ должен содержать ошибку для sessionId"
        );
    }
    
    /**
     * ТЕСТ 14: NEGATIVE - ПОПЫТКА ЗАВЕРШИТЬ СЕССИЮ ДО ДОБАВЛЕНИЯ ВСЕХ ДАННЫХ
     * 
     * POST /metrics/session/finish
     * Проверяет что нельзя завершить сессию до добавления всех данных
     */
    @Test(priority = 14, description = "NEGATIVE POST /metrics/session/finish - окончание сессии (негативный)")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверка что нельзя завершить сессию до добавления всех данных")
    @Story("Session Management")
    public void testFinishSessionBeforeAllData() {
        logger.info("=== ТЕСТ: Попытка завершить сессию до добавления всех данных ===");
        
        if (activeSessionId == null || activeSessionId.isEmpty()) {
            logger.warn("activeSessionId отсутствует из предыдущего теста, пропускаем проверку");
            return;
        }
        
        // Создаем запрос на завершение сессии
        SessionFinishRequest request = new SessionFinishRequest();
        request.setSessionId(activeSessionId);
        
        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_SESSION_FINISH_ENDPOINT);
        logger.info("Используемый activeSessionId: {}", activeSessionId);
        
        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(METRICS_SESSION_FINISH_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class)) // Проверяем что ответ - валидный JSON объект
                .body("message", notNullValue()) // Поле "message" существует
                .body("message", instanceOf(String.class)) // "message" - строка
                .body("message", containsString("session can not be finished")) // Сообщение содержит текст
                .extract()
                .response();
        
        logger.info("Статус код: {}", response.getStatusCode());
        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);
        
        // Проверяем что ответ не HTML страница логина
        checkNotLoginPage(responseBody);
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 15: ДОБАВЛЕНИЕ МЕТРИК ЭТАПА
     * 
     * POST /metrics/session/stage
     * Проверяет что API позволяет добавить метрики этапа к активной сессии
     */
    @Test(priority = 15, description = "POST /metrics/session/stage - добавление метрик этапа")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка добавления метрик этапа через POST /metrics/session/stage")
    @Story("Active Session Management")
    public void testAddStageToActiveSession() {
        logger.info("=== ТЕСТ: Добавление метрик этапа к активной сессии ===");
        
        if (activeSessionId == null || activeSessionId.isEmpty()) {
            logger.warn("activeSessionId отсутствует из предыдущего теста, пропускаем проверку");
            return;
        }
        
        // Генерируем рандомное число от 100 до 1000 для stageNumber
        int randomValue = (int)(Math.random() * 901) + 100;
        logger.info("Сгенерированное значение stageNumber: {}", randomValue);
        // Сохраняем stageNumber для дальнейшей проверки через /metrics/read (как stageNumber в Postman)
        stageNumberValue = randomValue;
        
        // Создаем stageSummary и stageAlert с одинаковым рандомным stageNumber
        Stage stageSummary = createBasicStage();
        stageSummary.setStageNumber(randomValue);
        Stage stageAlert = createBasicStage();
        stageAlert.setStageNumber(randomValue); // Убеждаемся что номера одинаковые
        
        // Создаем запрос с activeSessionId
        SessionStageRequest request = new SessionStageRequest();
        request.setSessionId(activeSessionId);
        request.setStageSummary(stageSummary);
        request.setStageAlert(stageAlert);
        
        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_SESSION_STAGE_ENDPOINT);
        logger.info("Используемый activeSessionId: {}", activeSessionId);
        
        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(METRICS_SESSION_STAGE_ENDPOINT)
                .then()
                .statusCode(anyOf(is(200), is(201))) // Проверяем успешный статус код
                .body("$", instanceOf(java.util.Map.class)) // Проверяем что ответ - валидный JSON объект
                .body("id", notNullValue()) // Поле "id" существует
                .body("id", instanceOf(String.class)) // "id" - строка
                .body("id", equalTo(activeSessionId)) // id равен activeSessionId
                .extract()
                .response();
        
        logger.info("Статус код: {}", response.getStatusCode());
        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);
        
        // Проверяем что ответ не HTML страница логина
        checkNotLoginPage(responseBody);
        
        // Дополнительная проверка через jsonPath
        String returnedId = response.jsonPath().getString("id");
        Assert.assertTrue(
                returnedId != null && returnedId.equals(activeSessionId),
                "Ответ должен содержать id равный activeSessionId. Ожидалось: " + activeSessionId + ", получено: " + returnedId
        );
        logger.info("Успешно добавлены метрики этапа к сессии с id: {}", returnedId);
    }
    
    /**
     * ТЕСТ 16: NEGATIVE - ОТПРАВКА НЕВАЛИДНОГО JSON В /session/stage
     * 
     * POST /metrics/session/stage
     * Проверяет обработку невалидного JSON
     */
    @Test(priority = 16, description = "NEGATIVE POST /metrics/session/stage - невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверка обработки невалидного JSON при добавлении метрик этапа")
    @Story("Metrics Validation")
    public void testAddStageWithInvalidJson() {
        logger.info("=== ТЕСТ: Отправка невалидного JSON в /session/stage ===");
        
        // Создаем невалидный JSON (отсутствует закрывающая фигурная скобка)
        String invalidJson = "{\n" +
                "  \"sessionId\": \"" + (activeSessionId != null ? activeSessionId : generateUUID()) + "\",\n" +
                "  \"stageSummary\": {";
        
        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_SESSION_STAGE_ENDPOINT);
        
        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .post(METRICS_SESSION_STAGE_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class)) // Проверяем что ответ - валидный JSON объект
                .body("message", notNullValue()) // Поле "message" существует
                .body("message", instanceOf(String.class)) // "message" - строка
                .body("message", containsString("JSON parse error")) // Сообщение содержит текст
                .extract()
                .response();
        
        logger.info("Статус код: {}", response.getStatusCode());
        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);
        
        // Проверяем что ответ не HTML страница логина
        checkNotLoginPage(responseBody);
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 17: NEGATIVE - ОТСУТСТВУЕТ sessionId В /session/stage
     * 
     * POST /metrics/session/stage
     * Проверяет валидацию обязательного поля sessionId
     */
    @Test(priority = 17, description = "NEGATIVE POST /metrics/session/stage - отсутствует sessionId")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверка валидации обязательного поля sessionId при добавлении метрик этапа")
    @Story("Metrics Validation")
    public void testAddStageWithoutSessionId() {
        logger.info("=== ТЕСТ: Отправка запроса без sessionId ===");
        
        // Создаем запрос без sessionId
        SessionStageRequest request = new SessionStageRequest();
        // sessionId не устанавливаем (null)
        request.setStageSummary(createBasicStage());
        request.setStageAlert(createBasicStage());
        
        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_SESSION_STAGE_ENDPOINT);
        
        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(METRICS_SESSION_STAGE_ENDPOINT)
                .then()
                .statusCode(400)
                .body(not(emptyString()))
                .extract()
                .response();
        
        logger.info("Статус код: {}", response.getStatusCode());
        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);
        
        // Проверяем что ответ не HTML страница логина
        checkNotLoginPage(responseBody);
        
        // Проверяем наличие ошибки валидации для sessionId
        // Согласно Postman: jsonData.sessionId должно быть равно "must not be null"
        String sessionIdError = response.jsonPath().getString("sessionId");
        Assert.assertTrue(
                sessionIdError != null && sessionIdError.equals("must not be null"),
                "Ответ должен содержать sessionId со значением 'must not be null'. Получено: " + sessionIdError
        );
        logger.info("Ожидаемая ошибка валидации получена: sessionId = {}", sessionIdError);
    }
    
    /**
     * ТЕСТ 18: НАСТРОЙКА ОКРУЖЕНИЯ СЕССИИ
     * 
     * POST /metrics/session/env
     * Проверяет что API позволяет настроить окружение активной сессии
     */
    @Test(priority = 18, description = "POST /metrics/session/env - настройка окружения")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка настройки окружения сессии через POST /metrics/session/env")
    @Story("Active Session Management")
    public void testSetSessionEnvironment() {
        logger.info("=== ТЕСТ: Настройка окружения сессии ===");
        
        if (activeSessionId == null || activeSessionId.isEmpty()) {
            logger.warn("activeSessionId отсутствует из предыдущего теста, пропускаем проверку");
            return;
        }
        
        // Создаем запрос с activeSessionId
        SessionEnvRequest request = new SessionEnvRequest();
        request.setSessionId(activeSessionId);
        
        // Устанавливаем layers
        List<String> layers = new ArrayList<>();
        layers.add("string");
        request.setLayers(layers);
        
        // Устанавливаем appVersions
        List<AppVersion> appVersions = new ArrayList<>();
        appVersions.add(new AppVersion("autotest_autotest", "string", "7.8.2025"));
        request.setAppVersions(appVersions);
        
        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_SESSION_ENV_ENDPOINT);
        logger.info("Используемый activeSessionId: {}", activeSessionId);
        
        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(METRICS_SESSION_ENV_ENDPOINT)
                .then()
                .statusCode(anyOf(is(200), is(201))) // Проверяем успешный статус код (200, 201)
                .extract()
                .response();
        
        logger.info("Статус код: {}", response.getStatusCode());
        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);
        
        // Проверяем что ответ не HTML страница логина
        checkNotLoginPage(responseBody);
        
        // Если статус код не 204 (No Content), проверяем наличие id в ответе
        if (response.getStatusCode() != 204 && responseBody != null && !responseBody.isEmpty()) {
            // Проверяем что ответ - валидный JSON объект
            Assert.assertTrue(responseBody.trim().startsWith("{"), "Ответ должен быть валидным JSON объектом");
            
            String returnedId = response.jsonPath().getString("id");
            Assert.assertTrue(
                    returnedId != null && returnedId.equals(activeSessionId),
                    "Ответ должен содержать id равный activeSessionId. Ожидалось: " + activeSessionId + ", получено: " + returnedId
            );
            logger.info("Успешно настроено окружение для сессии с id: {}", returnedId);
        } else {
            logger.info("Успешно настроено окружение для сессии (статус 204 - No Content или пустой ответ)");
        }
    }
    
    /**
     * ТЕСТ 19: NEGATIVE - ОТПРАВКА НЕВАЛИДНОГО JSON В /session/env
     * 
     * POST /metrics/session/env
     * Проверяет обработку невалидного JSON
     */
    @Test(priority = 19, description = "NEGATIVE POST /metrics/session/env - невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверка обработки невалидного JSON при настройке окружения")
    @Story("Metrics Validation")
    public void testSetSessionEnvironmentWithInvalidJson() {
        logger.info("=== ТЕСТ: Отправка невалидного JSON в /session/env ===");
        
        // Создаем невалидный JSON (отсутствует закрывающая фигурная скобка)
        String invalidJson = "{\n" +
                "  \"sessionId\": \"" + (activeSessionId != null ? activeSessionId : generateUUID()) + "\",\n" +
                "  \"layers\": [";
        
        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_SESSION_ENV_ENDPOINT);
        
        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .post(METRICS_SESSION_ENV_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class)) // Проверяем что ответ - валидный JSON объект
                .body("message", notNullValue()) // Поле "message" существует
                .body("message", instanceOf(String.class)) // "message" - строка
                .body("message", containsString("JSON parse error")) // Сообщение содержит текст
                .extract()
                .response();
        
        logger.info("Статус код: {}", response.getStatusCode());
        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);
        
        // Проверяем что ответ не HTML страница логина
        checkNotLoginPage(responseBody);
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }
    
    /**
     * ТЕСТ 20: NEGATIVE - ОТСУТСТВУЕТ sessionId В /session/env
     * 
     * POST /metrics/session/env
     * Проверяет валидацию обязательного поля sessionId
     */
    @Test(priority = 20, description = "NEGATIVE POST /metrics/session/env - отсутствует sessionId")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверка валидации обязательного поля sessionId при настройке окружения")
    @Story("Metrics Validation")
    public void testSetSessionEnvironmentWithoutSessionId() {
        logger.info("=== ТЕСТ: Отправка запроса без sessionId ===");
        
        // Создаем запрос без sessionId
        SessionEnvRequest request = new SessionEnvRequest();
        // sessionId не устанавливаем (null)
        
        // Устанавливаем layers
        List<String> layers = new ArrayList<>();
        layers.add("string");
        request.setLayers(layers);
        
        // Устанавливаем appVersions
        List<AppVersion> appVersions = new ArrayList<>();
        appVersions.add(new AppVersion("autotest_autotest", "string", "7.8.2025"));
        request.setAppVersions(appVersions);
        
        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_SESSION_ENV_ENDPOINT);
        
        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(METRICS_SESSION_ENV_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(404))) // Проверяем статус код 400 или 404
                .body(not(emptyString()))
                .extract()
                .response();
        
        logger.info("Статус код: {}", response.getStatusCode());
        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);
        
        // Проверяем что ответ не HTML страница логина
        checkNotLoginPage(responseBody);
        
        // Проверяем наличие ошибки валидации для sessionId
        // Согласно Postman: jsonData.sessionId должно быть равно "must not be null"
        String sessionIdError = response.jsonPath().getString("sessionId");
        Assert.assertTrue(
                sessionIdError != null && sessionIdError.equals("must not be null"),
                "Ответ должен содержать sessionId со значением 'must not be null'. Получено: " + sessionIdError
        );
        logger.info("Ожидаемая ошибка валидации получена: sessionId = {}", sessionIdError);
    }
    
    /**
     * ТЕСТ 21: ЗАВЕРШЕНИЕ СЕССИИ (ПОЗИТИВНЫЙ)
     * 
     * POST /metrics/session/finish
     * Проверяет что API позволяет завершить сессию после добавления всех данных
     */
    @Test(priority = 21, description = "POST /metrics/session/finish - окончание сессии (позитивный)")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка завершения сессии через POST /metrics/session/finish после добавления всех данных")
    @Story("Session Management")
    public void testFinishSession() {
        logger.info("=== ТЕСТ: Завершение сессии (позитивный) ===");
        
        if (activeSessionId == null || activeSessionId.isEmpty()) {
            logger.warn("activeSessionId отсутствует из предыдущего теста, пропускаем проверку");
            return;
        }
        
        // Создаем запрос на завершение сессии
        SessionFinishRequest request = new SessionFinishRequest();
        request.setSessionId(activeSessionId);
        
        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_SESSION_FINISH_ENDPOINT);
        logger.info("Используемый activeSessionId: {}", activeSessionId);
        
        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(METRICS_SESSION_FINISH_ENDPOINT)
                .then()
                .statusCode(anyOf(is(200), is(201))) // Проверяем успешный статус код
                .body("$", instanceOf(java.util.Map.class)) // Проверяем что ответ - валидный JSON объект
                .body("id", notNullValue()) // Поле "id" существует
                .body("id", instanceOf(String.class)) // "id" - строка
                .body("id", equalTo(activeSessionId)) // id равен activeSessionId
                .extract()
                .response();
        
        logger.info("Статус код: {}", response.getStatusCode());
        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);
        
        // Проверяем что ответ не HTML страница логина
        checkNotLoginPage(responseBody);
        
        // Дополнительная проверка через jsonPath
        String returnedId = response.jsonPath().getString("id");
        Assert.assertTrue(
                returnedId != null && returnedId.equals(activeSessionId),
                "Ответ должен содержать id равный activeSessionId. Ожидалось: " + activeSessionId + ", получено: " + returnedId
        );
        logger.info("Успешно завершена сессия с id: {}", returnedId);
    }

    /**
     * ТЕСТ 22: NEGATIVE - POST /metrics/session/finish - невалидный JSON
     */
    @Test(priority = 22, description = "NEGATIVE POST /metrics/session/finish - невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверка обработки невалидного JSON при завершении сессии")
    @Story("Metrics Validation")
    public void testFinishSessionWithInvalidJson() {
        logger.info("=== ТЕСТ: Отправка невалидного JSON в /session/finish ===");

        // Создаем невалидный JSON (отсутствует закрывающая фигурная скобка)
        String invalidJson = "{\n" +
                "  \"sessionId\": \"" + (activeSessionId != null ? activeSessionId : generateUUID()) + "\"";

        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_SESSION_FINISH_ENDPOINT);

        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .post(METRICS_SESSION_FINISH_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("JSON parse error"))
                .extract()
                .response();

        logger.info("Статус код: {}", response.getStatusCode());
        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);

        checkNotLoginPage(responseBody);
    }

    /**
     * ТЕСТ 23: NEGATIVE - POST /metrics/session/finish - повторное завершение
     */
    @Test(priority = 23, description = "NEGATIVE POST /metrics/session/finish - повторное завершение")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверка что повторное завершение уже завершённой сессии возвращает ошибку")
    @Story("Session Management")
    public void testFinishSessionTwice() {
        logger.info("=== ТЕСТ: Повторное завершение сессии ===");

        if (activeSessionId == null || activeSessionId.isEmpty()) {
            logger.warn("activeSessionId отсутствует из предыдущего теста, пропускаем проверку");
            return;
        }

        SessionFinishRequest request = new SessionFinishRequest(activeSessionId);

        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_SESSION_FINISH_ENDPOINT);
        logger.info("Используемый activeSessionId: {}", activeSessionId);

        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(METRICS_SESSION_FINISH_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString(" is already ended"))
                .extract()
                .response();

        logger.info("Статус код: {}", response.getStatusCode());
        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);

        checkNotLoginPage(responseBody);
    }

    /**
     * ТЕСТ 24: NEGATIVE - POST /metrics/session/finish - отсутствует sessionId
     */
    @Test(priority = 24, description = "NEGATIVE POST /metrics/session/finish - отсутствует sessionId")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверка валидации обязательного поля sessionId при завершении сессии")
    @Story("Metrics Validation")
    public void testFinishSessionWithoutSessionId() {
        logger.info("=== ТЕСТ: Завершение сессии без sessionId ===");

        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_SESSION_FINISH_ENDPOINT);

        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .post(METRICS_SESSION_FINISH_ENDPOINT)
                .then()
                .statusCode(400)
                .body("$", instanceOf(java.util.Map.class))
                .body("sessionId", notNullValue())
                .body("sessionId", instanceOf(String.class))
                .body("sessionId", containsString("must not be null"))
                .extract()
                .response();

        logger.info("Статус код: {}", response.getStatusCode());
        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);

        checkNotLoginPage(responseBody);
    }

    /**
     * ТЕСТ 25: GET /metrics/read - проверка измененных значений
     */
    @Test(priority = 25, description = "GET /metrics/read - проверка измененных значений")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка что /metrics/read содержит измененные значения (startupPageLoadSec, started_at, stageNumber, component)")
    @Story("Metrics Read")
    public void testReadMetricsChangedValues() {
        logger.info("=== ТЕСТ: GET /metrics/read - проверка измененных значений ===");

        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_READ_ENDPOINT);

        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .when()
                .get(METRICS_READ_ENDPOINT)
                .then()
                .statusCode(200)
                .extract()
                .response();

        String responseText = response.getBody().asString();
        logger.info("Ответ /metrics/read: {}", responseText);

        checkNotLoginPage(responseText);

        // startupPageLoadSec: ищем строку с metric="startup_page_load_sec" и проверяем значение " <n>.0"
        if (startupPageLoadSecValue != null) {
            String[] lines = responseText.split("\n");
            String metricLine = null;
            for (String line : lines) {
                if (line.contains("metric=\"startup_page_load_sec\"")) {
                    metricLine = line;
                    break;
                }
            }
            Assert.assertNotNull(metricLine, "Не найдена строка с metric=\"startup_page_load_sec\"");
            Assert.assertTrue(
                    metricLine.contains(" " + startupPageLoadSecValue + ".0"),
                    "Ожидалось что строка метрики содержит значение ' " + startupPageLoadSecValue + ".0', но было: " + metricLine
            );
        }

        // started_at: сравниваем только дату YYYY-MM-DD (как в Postman)
        if (activeSessionStartedAt != null) {
            String dateStr = activeSessionStartedAt.replaceAll("Z$", "");
            String dateWithoutTime = dateStr.substring(0, 10); // YYYY-MM-DD
            Assert.assertTrue(
                    responseText.contains("started_at=\"" + dateWithoutTime),
                    "Ответ должен содержать started_at с датой " + dateWithoutTime
            );
        }

        // stageNumber: stage_number="<random>"
        if (stageNumberValue != null) {
            Assert.assertTrue(
                    responseText.contains("stage_number=\"" + stageNumberValue + "\""),
                    "Ответ должен содержать stage_number=\"" + stageNumberValue + "\""
            );
        }

        // component=autotest_autotest
        Assert.assertTrue(
                responseText.contains("component=\"autotest_autotest\""),
                "Ответ должен содержать component=\"autotest_autotest\""
        );
    }

    /**
     * ТЕСТ 26: POST /metrics-info/search - поиск метрик для autotest_2
     */
    @Test(priority = 26, description = "POST /metrics-info/search - поиск метрик для autotest_2")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Поиск метрик для autotest_2 и проверка последней метрики по activeSessionId и startedAt2")
    @Story("Metrics Info")
    public void testMetricsInfoSearchAutotest2() {
        logger.info("=== ТЕСТ: POST /metrics-info/search - autotest_2 ===");

        String todayDate = getTodayDate();
        logger.info("todayDate: {}", todayDate);

        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_INFO_SEARCH_ENDPOINT);

        String body = "{\n" +
                "  \"userName\": \"autotest_2\",\n" +
                "  \"presetName\": \"autotest_2\",\n" +
                "  \"period\": {\n" +
                "    \"start\": \"" + todayDate + "\",\n" +
                "    \"end\": \"" + todayDate + "\"\n" +
                "  }\n" +
                "}";

        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(METRICS_INFO_SEARCH_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class))
                .extract()
                .response();

        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);
        checkNotLoginPage(responseBody);

        List<java.util.Map<String, Object>> items = response.jsonPath().getList("$");
        Assert.assertNotNull(items, "Ответ должен быть массивом");

        if (activeSessionId != null && activeSessionStartedAt != null) {
            // startedAt2 в Postman сравнивается как строка без Z, но backend обрезает trailing zeros
            String expectedStartedAt = normalizeDateForComparison(activeSessionStartedAt);
            java.util.Map<String, Object> found = null;
            for (java.util.Map<String, Object> item : items) {
                Object sessionIdObj = item.get("sessionId");
                if (sessionIdObj != null && activeSessionId.equals(sessionIdObj.toString())) {
                    found = item;
                    break;
                }
            }
            Assert.assertNotNull(found, "Не найдена метрика по activeSessionId=" + activeSessionId);
            Assert.assertEquals(
                    String.valueOf(found.get("sessionId")),
                    activeSessionId,
                    "sessionId должен быть равен activeSessionId"
            );
            String actualStartedAt = normalizeDateForComparison(String.valueOf(found.get("startedAt")));
            Assert.assertEquals(
                    actualStartedAt,
                    expectedStartedAt,
                    "startedAt должен быть равен " + expectedStartedAt + ", но был " + actualStartedAt
            );
        }
    }

    /**
     * ТЕСТ 27: POST /metrics-info/search - поиск метрик для autotest
     */
    @Test(priority = 27, description = "POST /metrics-info/search - поиск метрик для autotest")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Поиск метрик для autotest и проверка метрики по sessionId и startedAt из теста 2")
    @Story("Metrics Info")
    public void testMetricsInfoSearchAutotest() {
        logger.info("=== ТЕСТ: POST /metrics-info/search - autotest ===");

        String todayDate = getTodayDate();
        logger.info("todayDate: {}", todayDate);

        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_INFO_SEARCH_ENDPOINT);

        String body = "{\n" +
                "  \"userName\": \"autotest\",\n" +
                "  \"presetName\": \"autotest\",\n" +
                "  \"period\": {\n" +
                "    \"start\": \"" + todayDate + "\",\n" +
                "    \"end\": \"" + todayDate + "\"\n" +
                "  }\n" +
                "}";

        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(METRICS_INFO_SEARCH_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class))
                .extract()
                .response();

        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);
        checkNotLoginPage(responseBody);

        List<java.util.Map<String, Object>> items = response.jsonPath().getList("$");
        Assert.assertNotNull(items, "Ответ должен быть массивом");

        // Важно: backend в /metrics/read возвращает другой session_id (он отличается от отправленного).
        // Поэтому для строгого соответствия Postman используем foundSessionId + startedAt, снятые из /metrics/read.
        if (foundSessionIdAutotest != null && foundStartedAtAutotest != null) {
            String expectedSessionId = foundSessionIdAutotest;
            String expectedStartedAt = normalizeDateForComparison(foundStartedAtAutotest);
            java.util.Map<String, Object> found = null;
            for (java.util.Map<String, Object> item : items) {
                Object sessionIdObj = item.get("sessionId");
                if (sessionIdObj != null && expectedSessionId.equals(sessionIdObj.toString())) {
                    found = item;
                    break;
                }
            }
            Assert.assertNotNull(found, "Не найдена метрика по sessionId=" + expectedSessionId);
            Assert.assertEquals(
                    String.valueOf(found.get("sessionId")),
                    expectedSessionId,
                    "sessionId должен быть равен " + expectedSessionId
            );
            String actualStartedAt = normalizeDateForComparison(String.valueOf(found.get("startedAt")));
            Assert.assertEquals(
                    actualStartedAt,
                    expectedStartedAt,
                    "startedAt должен быть равен " + expectedStartedAt + ", но был " + actualStartedAt
            );
        }
    }

    /**
     * ТЕСТ 28: NEGATIVE POST /metrics-info/search - невалидный JSON
     */
    @Test(priority = 28, description = "NEGATIVE POST /metrics-info/search - невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Негативный тест: отправка невалидного JSON в POST /metrics-info/search")
    @Story("Metrics Info")
    public void testMetricsInfoSearchWithInvalidJson() {
        logger.info("=== ТЕСТ: NEGATIVE POST /metrics-info/search - невалидный JSON ===");

        String invalidJson = "{\n" +
                "  \"userName\": \"autotest\",\n" +
                "  \"presetName\": \"autotest\",\n" +
                "  \"period\": {";

        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_INFO_SEARCH_ENDPOINT);

        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .post(METRICS_INFO_SEARCH_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("JSON parse error"))
                .extract()
                .response();

        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);
        checkNotLoginPage(responseBody);
    }

    /**
     * ТЕСТ 29: NEGATIVE POST /metrics-info/search - отсутствуют обязательные поля
     */
    @Test(priority = 29, description = "NEGATIVE POST /metrics-info/search - отсутствуют обязательные поля")
    @Severity(SeverityLevel.NORMAL)
    @Description("Негативный тест: отсутствуют обязательные поля userName/presetName в POST /metrics-info/search")
    @Story("Metrics Info")
    public void testMetricsInfoSearchWithoutRequiredFields() {
        logger.info("=== ТЕСТ: NEGATIVE POST /metrics-info/search - отсутствуют обязательные поля ===");

        String todayDate = getTodayDate();

        String body = "{\n" +
                "  \"period\": {\n" +
                "    \"start\": \"" + todayDate + "\",\n" +
                "    \"end\": \"" + todayDate + "\"\n" +
                "  }\n" +
                "}";

        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_INFO_SEARCH_ENDPOINT);

        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(METRICS_INFO_SEARCH_ENDPOINT)
                .then()
                .statusCode(400)
                .body("$", instanceOf(java.util.Map.class))
                .body("userName", equalTo("must not be empty"))
                .extract()
                .response();

        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);
        checkNotLoginPage(responseBody);
    }

    /**
     * ТЕСТ 30: GET /metrics-info/users - получение списка пользователей
     */
    @Test(priority = 30, description = "GET /metrics-info/users - получение списка пользователей")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка получения списка пользователей через GET /metrics-info/users")
    @Story("Metrics Info")
    public void testMetricsInfoUsers() {
        logger.info("=== ТЕСТ: GET /metrics-info/users ===");

        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_INFO_USERS_ENDPOINT);

        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .when()
                .get(METRICS_INFO_USERS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class))
                .extract()
                .response();

        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);
        checkNotLoginPage(responseBody);

        List<String> users = response.jsonPath().getList("$", String.class);
        Assert.assertNotNull(users, "Ответ должен быть массивом");
        Assert.assertTrue(users.contains("autotest"), "Ответ должен содержать 'autotest'");
        Assert.assertTrue(users.contains("autotest_2"), "Ответ должен содержать 'autotest_2'");
    }

    /**
     * ТЕСТ 31: GET /metrics-info/session/{sessionId} - получение метрики по foundSessionId
     */
    @Test(priority = 31, description = "GET /metrics-info/session/{sessionId} - получение метрики по foundSessionId")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка получения метрики по foundSessionId (из /metrics/read)")
    @Story("Metrics Info")
    public void testMetricsInfoSessionByFoundSessionId() {
        logger.info("=== ТЕСТ: GET /metrics-info/session/{sessionId} ===");

        if (foundSessionIdAutotest == null || foundSessionIdAutotest.isEmpty()) {
            logger.warn("foundSessionIdAutotest отсутствует из /metrics/read, пропускаем проверку");
            return;
        }

        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        String endpoint = METRICS_INFO_SESSION_ENDPOINT + "/" + foundSessionIdAutotest;
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, endpoint);

        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .when()
                .get(endpoint)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("userName", equalTo("autotest"))
                .body("presetName", equalTo("autotest"))
                .extract()
                .response();

        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);
        checkNotLoginPage(responseBody);

        // startedAt: сравниваем как в Postman (убираем только Z у исходного startedAt), но нормализуем обе строки
        if (generatedStartedAt != null) {
            String expectedStartedAt = normalizeDateForComparison(generatedStartedAt);
            String actualStartedAt = normalizeDateForComparison(response.jsonPath().getString("startedAt"));
            Assert.assertEquals(
                    actualStartedAt,
                    expectedStartedAt,
                    "startedAt должен быть равен " + expectedStartedAt + ", но был " + actualStartedAt
            );
        }
    }

    /**
     * ТЕСТ 32: GET /metrics-info/session/{sessionId} - получение метрики по activeSessionId
     */
    @Test(priority = 32, description = "GET /metrics-info/session/{sessionId} - получение метрики по activeSessionId")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка метрики по activeSessionId: startupPageLoadSec, stageNumber, startedAt, component=autotest_autotest")
    @Story("Metrics Info")
    public void testMetricsInfoSessionByActiveSessionId() {
        logger.info("=== ТЕСТ: GET /metrics-info/session/{activeSessionId} ===");

        if (activeSessionId == null || activeSessionId.isEmpty()) {
            logger.warn("activeSessionId отсутствует, пропускаем проверку");
            return;
        }

        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        String endpoint = METRICS_INFO_SESSION_ENDPOINT + "/" + activeSessionId;
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, endpoint);

        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .when()
                .get(endpoint)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .extract()
                .response();

        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);
        checkNotLoginPage(responseBody);

        // startupPageLoadSec из summaryAlert
        if (startupPageLoadSecValue != null) {
            // Backend возвращает значение как 7.0 (Double), поэтому читаем как Double и сравниваем по целой части
            Double actualStartup = response.jsonPath().getDouble("summaryAlert.startupPageLoadSec");
            Assert.assertTrue(
                    actualStartup != null && actualStartup.intValue() == startupPageLoadSecValue,
                    "startupPageLoadSec должен быть " + startupPageLoadSecValue + ", но был " + actualStartup
            );
        }

        // stageNumber из stagesSummary[0]
        if (stageNumberValue != null) {
            Integer actualStageNumber = response.jsonPath().getInt("stagesSummary[0].stageNumber");
            Assert.assertTrue(
                    actualStageNumber != null && actualStageNumber.equals(stageNumberValue),
                    "stageNumber должен быть " + stageNumberValue + ", но был " + actualStageNumber
            );
        }

        // startedAt: сравниваем без Z (как startedAt2 в Postman), но нормализуем обе строки
        if (activeSessionStartedAt != null) {
            String expectedStartedAt = normalizeDateForComparison(activeSessionStartedAt);
            String actualStartedAt = normalizeDateForComparison(response.jsonPath().getString("startedAt"));
            Assert.assertEquals(
                    actualStartedAt,
                    expectedStartedAt,
                    "startedAt должен быть равен " + expectedStartedAt + ", но был " + actualStartedAt
            );
        }

        // component=autotest_autotest в appVersions[0].component
        String component = response.jsonPath().getString("appVersions[0].component");
        Assert.assertTrue(
                component != null && component.equals("autotest_autotest"),
                "component в appVersions[0] должен быть 'autotest_autotest', но был '" + component + "'"
        );
    }

    /**
     * ТЕСТ 33: GET /metrics-info/presets - получение списка уникальных пресетов
     */
    @Test(priority = 33, description = "GET /metrics-info/presets - получение списка уникальных пресетов")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверка получения списка уникальных пресетов через GET /metrics-info/presets")
    @Story("Metrics Info")
    public void testMetricsInfoPresets() {
        logger.info("=== ТЕСТ: GET /metrics-info/presets ===");

        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_INFO_PRESETS_ENDPOINT);

        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .when()
                .get(METRICS_INFO_PRESETS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class))
                .extract()
                .response();

        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);
        checkNotLoginPage(responseBody);

        List<String> presets = response.jsonPath().getList("$", String.class);
        Assert.assertNotNull(presets, "Ответ должен быть массивом");
        Assert.assertTrue(presets.contains("autotest"), "Ответ должен содержать 'autotest'");
        Assert.assertTrue(presets.contains("autotest_2"), "Ответ должен содержать 'autotest_2'");
    }

    /**
     * ТЕСТ 34: GET /metrics-info/enigma - получение статистики по пресетам
     */
    @Test(priority = 34, description = "GET /metrics-info/enigma - получение статистики по пресетам")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверка статистики по пресетам через GET /metrics-info/enigma")
    @Story("Metrics Info")
    public void testMetricsInfoEnigma() {
        logger.info("=== ТЕСТ: GET /metrics-info/enigma ===");

        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_INFO_ENIGMA_ENDPOINT);

        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .when()
                .get(METRICS_INFO_ENIGMA_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class))
                .extract()
                .response();

        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);
        checkNotLoginPage(responseBody);

        List<java.util.Map<String, Object>> items = response.jsonPath().getList("$");
        Assert.assertNotNull(items, "Ответ должен быть массивом");

        boolean hasAutotest = false;
        boolean hasAutotest2 = false;
        for (java.util.Map<String, Object> item : items) {
            Object presetNameObj = item.get("presetName");
            if (presetNameObj == null) continue;
            String presetName = presetNameObj.toString();
            if ("autotest".equals(presetName)) {
                hasAutotest = true;
            }
            if ("autotest_2".equals(presetName)) {
                hasAutotest2 = true;
            }
        }
        Assert.assertTrue(hasAutotest, "Ответ должен содержать пресет 'autotest'");
        Assert.assertTrue(hasAutotest2, "Ответ должен содержать пресет 'autotest_2'");
    }

    /**
     * ТЕСТ 35: GET /metrics-info/archive - получение архивных метрик
     */
    @Test(priority = 35, description = "GET /metrics-info/archive - получение архивных метрик")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверка получения архивных метрик через GET /metrics-info/archive")
    @Story("Metrics Info")
    public void testMetricsInfoArchive() {
        logger.info("=== ТЕСТ: GET /metrics-info/archive ===");

        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        logger.info("Используемый Base URL: {}", baseUrl);
        logger.info("Полный URL запроса: {}{}", baseUrl, METRICS_INFO_ARCHIVE_ENDPOINT);

        Response response = given()
                .spec(getRequestSpec())
                .baseUri(baseUrl)
                .when()
                .get(METRICS_INFO_ARCHIVE_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class))
                .extract()
                .response();

        String responseBody = response.getBody().asString();
        logger.info("Ответ: {}", responseBody);
        checkNotLoginPage(responseBody);

        List<java.util.Map<String, Object>> items = response.jsonPath().getList("$");
        Assert.assertNotNull(items, "Ответ должен быть массивом");

        // Архив метрик — данные всегда хранятся за 2024 год.
        int expectedYear = 2024;
        boolean hasExpectedYear = false;
        for (java.util.Map<String, Object> item : items) {
            Object yearObj = item.get("dataYear");
            if (yearObj == null) continue;
            int year;
            try {
                year = Integer.parseInt(yearObj.toString());
            } catch (NumberFormatException e) {
                continue;
            }
            if (year == expectedYear) {
                hasExpectedYear = true;
                break;
            }
        }
        Assert.assertTrue(
                hasExpectedYear,
                "Ответ должен содержать хотя бы одну запись с dataYear " + expectedYear
        );
    }
}
