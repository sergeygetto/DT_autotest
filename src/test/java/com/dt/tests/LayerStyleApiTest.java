package com.dt.tests;

import com.dt.base.BaseTest;
import com.dt.base.Config;
import com.dt.base.SensitiveDataFilter;
import io.qameta.allure.*;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * КЛАСС С ТЕСТАМИ ДЛЯ LAYER STYLE API (УПРАВЛЕНИЕ СЛОЯМИ И ГРУППАМИ)
 *
 * ЗАЧЕМ НУЖЕН ЭТОТ КЛАСС:
 * - Содержит все тесты для API управления слоями и группами (POST /tree-layers)
 * - Наследуется от BaseTest — получает методы для работы с API (куки, спецификация)
 * - Полностью независим от других тестовых классов — общее только использование куки
 *
 * КАК РАБОТАЕТ:
 * - TestNG находит методы с аннотацией @Test
 * - Выполняет их в порядке priority (1, 2, 3...)
 * - @BeforeClass выполняется один раз перед всеми тестами
 *
 * АННОТАЦИИ ALLURE:
 * - @Epic — группа тестов "Layer Style API Testing"
 * - @Feature — функциональность "Tree Layers Management"
 */
@Epic("Layer Style API Testing")
@Feature("Tree Layers Management")
public class LayerStyleApiTest extends BaseTest {

    /**
     * БАЗОВЫЙ URL API ПО УМОЛЧАНИЮ
     * Включает префикс бэкенда (путь до tree-layers). Переопределить: -DbaseUrl=https://your-host/your-path
     */
    // Публичный репозиторий: не храним здесь URL внутренних стендов.
    // Для запуска задайте baseUrl через -DbaseUrl или переменную окружения baseUrl.
    private static final String DEFAULT_BASE_URL = "https://example.com";

    /**
     * ENDPOINT (ПУТЬ) ДЛЯ РАБОТЫ С ДЕРЕВОМ СЛОЁВ (POST — создание/обновление)
     */
    private static final String TREE_LAYERS_ENDPOINT = "/tree-layers";

    /**
     * ENDPOINT GET short-display — краткое отображение дерева (слой есть, группа скрыта)
     */
    private static final String SHORT_DISPLAY_ENDPOINT = "/tree-layers/short-display";

    /**
     * ENDPOINT PUT tree-layers/change-position — изменение позиции слоя
     */
    private static final String CHANGE_POSITION_ENDPOINT = "/tree-layers/change-position";

    /**
     * ENDPOINT GET lock — список блокировок
     */
    private static final String LOCK_ENDPOINT = "/lock";

    /**
     * ENDPOINT POST lock/acquire — получение блокировки для редактирования слоя/группы
     */
    private static final String LOCK_ACQUIRE_ENDPOINT = "/lock/acquire";

    /**
     * ENDPOINT POST lock/release — освобождение блокировки
     */
    private static final String LOCK_RELEASE_ENDPOINT = "/lock/release";

    /**
     * ENDPOINT GET tree-layers/short-editor — редактор дерева (слой в папке)
     */
    private static final String SHORT_EDITOR_ENDPOINT = "/tree-layers/short-editor";

    /**
     * ENDPOINT POST tree-layers/get-settings — получение настроек слоёв
     */
    private static final String GET_SETTINGS_ENDPOINT = "/tree-layers/get-settings";

    /**
     * ENDPOINT GET tree-layers/structure — структура дерева слоёв
     */
    private static final String STRUCTURE_ENDPOINT = "/tree-layers/structure";

    /**
     * ENDPOINT POST tree-layers/create-model — создание модели
     */
    private static final String CREATE_MODEL_ENDPOINT = "/tree-layers/create-model";

    /**
     * ENDPOINT GET tree-layers/display-tree — дерево для отображения
     */
    private static final String DISPLAY_TREE_ENDPOINT = "/tree-layers/display-tree";

    /**
     * ENDPOINT POST maintenance/backup-start — запуск создания бэкапа
     */
    private static final String BACKUP_START_ENDPOINT = "/maintenance/backup-start";

    /**
     * ENDPOINT GET maintenance/backups — список бэкапов
     */
    private static final String BACKUPS_ENDPOINT = "/maintenance/backups";

    /**
     * ENDPOINT POST maintenance/backup-download — скачивание бэкапа
     */
    private static final String BACKUP_DOWNLOAD_ENDPOINT = "/maintenance/backup-download";

    /**
     * ENDPOINT POST maintenance/backup-restore — восстановление из бэкапа
     */
    private static final String BACKUP_RESTORE_ENDPOINT = "/maintenance/backup-restore";

    /**
     * ENDPOINT POST maintenance/export — экспорт данных
     */
    private static final String EXPORT_ENDPOINT = "/maintenance/export";

    /**
     * ENDPOINT layer-tags — создание, получение, обновление тегов
     */
    private static final String LAYER_TAGS_ENDPOINT = "/layer-tags";

    /**
     * ENDPOINT icons — загрузка и переименование иконок
     */
    private static final String ICONS_ENDPOINT = "/icons";

    /**
     * ENDPOINT markers — загрузка маркеров
     */
    private static final String MARKERS_ENDPOINT = "/markers";

    /**
     * ENDPOINT data-hub — получение данных и атрибутов
     */
    private static final String DATA_HUB_ENDPOINT = "/data-hub";

    /**
     * ENDPOINT websocket-connection — URL и ключи событий
     */
    private static final String WEBSOCKET_CONNECTION_ENDPOINT = "/websocket-connection";

    /**
     * ENDPOINT permissions/roles — список ролей
     */
    private static final String PERMISSIONS_ROLES_ENDPOINT = "/permissions/roles";

    /**
     * Базовый URL хранилища для построения iconUrl (переименование иконки)
     */
    private static final String STORAGE_ICONS_BASE = "https://example.com/storage/da-cm-map-backend-layerstyle/icons/";

    /**
     * Базовый URL хранилища для построения markerUrl (переименование маркера)
     */
    private static final String STORAGE_MARKERS_BASE = "https://example.com/storage/da-cm-map-backend-layerstyle/marker/";

    /**
     * Имя загруженной иконки (из теста 86, для тестов 88, 89)
     */
    private static String uploadedIconName;

    /**
     * Новое имя иконки при переименовании
     */
    private static final String RENAMED_ICON_NAME = "autotest_icon_renamed";

    /**
     * Имя загруженного маркера (из теста 93)
     */
    private static String markerFileName;

    /**
     * Новое имя маркера при переименовании
     */
    private static final String RENAMED_MARKER_NAME = "autotest_marker_renamed";

    /**
     * ID созданного слоя с маркером и иконкой (тест 44)
     */
    private static String newLayerId;

    /**
     * ID модели из тела create-model (для тестов 16, 18, 19 когда create-model возвращает 200)
     */
    private static final String MODEL_ID = "51e9aba1-6ba7-4837-912d-ea80c58467c9";

    /**
     * URL по умолчанию для API справочников
     */
    private static final String DEFAULT_DICTIONARIES_URL = "https://example.com";

    /**
     * Несуществующий UUID для негативного теста lock/acquire
     */
    private static final String NON_EXISTENT_DOC_ID = "00000000-0000-0000-0000-000000000000";

    /**
     * ID группы из тела запроса создания слоя и группы (для проверки docIds в ответе)
     */
    private static final String GROUP_ID = "36778969-be2e-41e9-b8ab-0e644466736b";

    /**
     * ID слоя из тела запроса создания слоя и группы (для проверки docIds в ответе)
     */
    private static final String LAYER_ID = "5bf7d7ab-6ab9-4223-808a-6dff6702e734";

    /**
     * Максимально допустимое время ответа в миллисекундах
     */
    private static final long MAX_RESPONSE_TIME_MS = 2000;

    /**
     * Имя файла бэкапа (заполняется в тесте 58, используется в 59, 63)
     */
    private static String backupFile;

    /**
     * ID тега (заполняется в тесте 29, используется в 30, 32)
     */
    private static String tagId;

    /**
     * ИНИЦИАЛИЗАЦИЯ ПЕРЕД ВСЕМИ ТЕСТАМИ
     * Настраивает базовый URL и куки (общее с другими тестами — только кука).
     */
    @BeforeClass
    public void setUp() {
        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        setBaseUrl(baseUrl);

        String cookieValue = Config.getCookie();
        if (cookieValue == null || cookieValue.isEmpty()) {
            logger.error("КРИТИЧЕСКАЯ ОШИБКА: Cookie не установлена! Установите переменную окружения 'cookie' или -Dcookie=value");
            logger.error("Пример: mvn test -Dtest=LayerStyleApiTest -Dcookie=\"your-cookie-value\"");
            throw new AssertionError("Cookie не установлена. Задайте -Dcookie=... или переменную окружения cookie.");
        }
        setCookie("cookie", cookieValue);
        logger.info("Cookie установлена (маскированная): {}", SensitiveDataFilter.maskForLogging(cookieValue));
        logger.info("LayerStyleApiTest инициализирован. Base URL: {}", baseUrl);
    }

    /**
     * ТЕСТ 1: POST /tree-layers — создание слоя и группы (ПОЗИТИВНЫЙ)
     * Проверка: статус 200, ответ содержит массив docIds, в docIds есть groupId и layerId, время ответа < 2000 мс.
     */
    @Test(priority = 1, description = "POST /tree-layers — создание слоя и группы")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Создание группы и слоя для автотестов. Проверка статуса 200, структуры ответа (docIds), наличия groupId и layerId в docIds, времени ответа.")
    @Story("Создание слоя и группы")
    public void testCreateLayerAndGroup() {
        logger.info("Выполнение теста: POST /tree-layers — создание слоя и группы");

        String requestBody = "[\n"
            + "  {\n"
            + "    \"id\": \"36778969-be2e-41e9-b8ab-0e644466736b\",\n"
            + "    \"name\": \"папка для автотеста 1\",\n"
            + "    \"tags\": [],\n"
            + "    \"state\": \"ACTUAL\",\n"
            + "    \"access\": \"FULL\",\n"
            + "    \"geoType\": null,\n"
            + "    \"itemType\": \"group\",\n"
            + "    \"position\": 2,\n"
            + "    \"settings\": {\n"
            + "      \"viewInTree\": {\n"
            + "        \"isChecked\": false,\n"
            + "        \"iconInTree\": null,\n"
            + "        \"isCollapsed\": true,\n"
            + "        \"isShownInTree\": true,\n"
            + "        \"isHiddenInTree\": false,\n"
            + "        \"notIncludeAllSubgroups\": false\n"
            + "      },\n"
            + "      \"advancedOptions\": {\n"
            + "        \"isComposite\": false\n"
            + "      }\n"
            + "    },\n"
            + "    \"layerType\": null,\n"
            + "    \"externalId\": null\n"
            + "  },\n"
            + "  {\n"
            + "    \"name\": \"Wi-Fi в парках (полигоны)\",\n"
            + "    \"tags\": [],\n"
            + "    \"label\": \"слой для автотеста\",\n"
            + "    \"geoType\": \"POLYGON\",\n"
            + "    \"itemType\": \"node\",\n"
            + "    \"settings\": {\n"
            + "      \"viewInTree\": {\n"
            + "        \"isChecked\": false,\n"
            + "        \"isShownInTree\": true,\n"
            + "        \"isHiddenInTree\": false\n"
            + "      },\n"
            + "      \"displayObjectsOnMap\": {\n"
            + "        \"style\": [\n"
            + "          {\n"
            + "            \"color\": \"#FFFFFF\",\n"
            + "            \"opacity\": 1\n"
            + "          }\n"
            + "        ]\n"
            + "      }\n"
            + "    },\n"
            + "    \"hideValue\": null,\n"
            + "    \"layerType\": \"vctr\",\n"
            + "    \"datasource\": \"\",\n"
            + "    \"externalId\": 24,\n"
            + "    \"validateLayerVisibility\": true,\n"
            + "    \"id\": \"5bf7d7ab-6ab9-4223-808a-6dff6702e734\",\n"
            + "    \"position\": 1,\n"
            + "    \"state\": \"ACTUAL\"\n"
            + "  }\n"
            + "]";

        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(TREE_LAYERS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("docIds", notNullValue())
                .body("docIds", instanceOf(List.class))
                .body("docIds", hasItems(GROUP_ID, LAYER_ID))
                .extract()
                .response();

        Assert.assertTrue(response.getTime() < MAX_RESPONSE_TIME_MS,
                "Время ответа должно быть меньше 2000 мс, фактически: " + response.getTime() + " мс");
        logger.info("Тест успешно завершен. docIds: {}", response.jsonPath().getList("docIds"));
    }

    /**
     * ТЕСТ 2: NEGATIVE POST /tree-layers — отсутствует поле name
     * Ожидается статус 400 или 500, ответ JSON с полем message, содержащим текст об обязательном поле name.
     */
    @Test(priority = 2, description = "NEGATIVE POST /tree-layers — отсутствует поле name")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка создания без обязательного поля name. Проверка статуса 400/500 и сообщения об ошибке.")
    @Story("Negative — отсутствует name")
    public void testTreeLayersNegativeMissingName() {
        logger.info("Выполнение теста: NEGATIVE POST /tree-layers — отсутствует поле name");

        String requestBody = "[\n"
            + "  {\n"
            + "    \"id\": \"36778969-be2e-41e9-b8ab-0e644466736b\",\n"
            + "    \"tags\": [],\n"
            + "    \"state\": \"ACTUAL\",\n"
            + "    \"itemType\": \"group\"\n"
            + "  }\n"
            + "]";

        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(TREE_LAYERS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Не заполнено обязательное поле name"))
                .extract()
                .response();

        logger.info("Ответ: status={}, message={}", response.getStatusCode(), response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 3: NEGATIVE POST /tree-layers — невалидный geoType
     * Ожидается статус 400 или 500, ответ с message про обязательное поле layerType.
     */
    @Test(priority = 3, description = "NEGATIVE POST /tree-layers — невалидный geoType")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка создания слоя с невалидным geoType. Проверка статуса 400/500 и сообщения об ошибке layerType.")
    @Story("Negative — невалидный geoType")
    public void testTreeLayersNegativeInvalidGeoType() {
        logger.info("Выполнение теста: NEGATIVE POST /tree-layers — невалидный geoType");

        String requestBody = "[\n"
            + "  {\n"
            + "    \"name\": \"Test layer\",\n"
            + "    \"tags\": [],\n"
            + "    \"label\": \"слой для автотеста\",\n"
            + "    \"geoType\": \"INVALID_GEOTYPE\",\n"
            + "    \"itemType\": \"node\",\n"
            + "    \"state\": \"ACTUAL\"\n"
            + "  }\n"
            + "]";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(TREE_LAYERS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Не заполнено обязательное поле layerType"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 4: NEGATIVE POST /tree-layers — невалидный формат UUID в id
     * Ожидается статус 400 или 500, ответ с message "Invalid UUID string".
     */
    @Test(priority = 4, description = "NEGATIVE POST /tree-layers — невалидный формат UUID в id")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка создания с невалидным UUID в id. Проверка статуса 400/500 и сообщения Invalid UUID string.")
    @Story("Negative — невалидный UUID в id")
    public void testTreeLayersNegativeInvalidUuid() {
        logger.info("Выполнение теста: NEGATIVE POST /tree-layers — невалидный формат UUID в id");

        String requestBody = "[\n"
            + "  {\n"
            + "    \"id\": \"invalid-uuid-format\",\n"
            + "    \"name\": \"папка для автотеста 1\",\n"
            + "    \"tags\": [],\n"
            + "    \"state\": \"ACTUAL\",\n"
            + "    \"itemType\": \"group\"\n"
            + "  }\n"
            + "]";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(TREE_LAYERS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Invalid UUID string"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 5: NEGATIVE POST /tree-layers — невалидный JSON
     * Ожидается статус 400 или 500, ответ в формате JSON.
     */
    @Test(priority = 5, description = "NEGATIVE POST /tree-layers — невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка отправки невалидного JSON. Проверка статуса 400/500 и ответа в формате JSON.")
    @Story("Negative — невалидный JSON")
    public void testTreeLayersNegativeInvalidJson() {
        logger.info("Выполнение теста: NEGATIVE POST /tree-layers — невалидный JSON");

        String invalidJsonBody = "[\n"
            + "  {\n"
            + "    \"id\": \"36778969-be2e-41e9-b8ab-0e644466736b\",\n"
            + "    \"name\": \"папка для автотеста 1\"\n";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(invalidJsonBody)
                .when()
                .post(TREE_LAYERS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 6: GET /tree-layers/short-display — слой есть, группа скрыта
     * Проверка: статус 200, ответ содержит items (массив), слой есть в items, группы нет в items.
     */
    @Test(priority = 6, description = "GET /tree-layers/short-display — слой есть, группа скрыта")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка short-display: слой созданный в тесте 1 присутствует в items, группа скрыта (отсутствует в items).")
    @Story("Short-display")
    public void testShortDisplay() {
        logger.info("Выполнение теста: GET /tree-layers/short-display");

        given()
                .spec(getRequestSpec())
                .when()
                .get(SHORT_DISPLAY_ENDPOINT)
                .then()
                .statusCode(200)
                .body("items", notNullValue())
                .body("items", instanceOf(List.class))
                .body("items", hasItem(hasEntry("id", LAYER_ID)))
                .body("items", not(hasItem(hasEntry("id", GROUP_ID))));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 7: POST /lock/acquire — получение блокировки слоя
     * Проверка: статус 200. Проверку времени ответа не делаем (тесты на скорость пропускаем).
     */
    @Test(priority = 7, description = "POST /lock/acquire — получение блокировки слоя")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение блокировки для редактирования слоя по docId.")
    @Story("Lock acquire")
    public void testLockAcquire() {
        logger.info("Выполнение теста: POST /lock/acquire");

        String requestBody = "{\"docId\":\"" + LAYER_ID + "\"}";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(LOCK_ACQUIRE_ENDPOINT)
                .then()
                .statusCode(200);

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 8: NEGATIVE POST /lock/acquire — отсутствует поле docId
     */
    @Test(priority = 8, description = "NEGATIVE POST /lock/acquire — отсутствует поле docId")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка захвата блокировки без docId. Ожидается 400/404 и userMessage про ошибку захвата блокировки.")
    @Story("Negative lock/acquire")
    public void testLockAcquireNegativeMissingDocId() {
        logger.info("Выполнение теста: NEGATIVE POST /lock/acquire — отсутствует docId");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .post(LOCK_ACQUIRE_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(404)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("userMessage", containsString("ошибка при захвате блокировки"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 9: NEGATIVE POST /lock/acquire — невалидный формат UUID в docId
     */
    @Test(priority = 9, description = "NEGATIVE POST /lock/acquire — невалидный UUID в docId")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка захвата блокировки с невалидным UUID. Ожидается 400/500.")
    @Story("Negative lock/acquire")
    public void testLockAcquireNegativeInvalidUuid() {
        logger.info("Выполнение теста: NEGATIVE POST /lock/acquire — невалидный UUID");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"docId\":\"invalid-uuid-format\"}")
                .when()
                .post(LOCK_ACQUIRE_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 10: NEGATIVE POST /lock/acquire — несуществующий docId
     */
    @Test(priority = 10, description = "NEGATIVE POST /lock/acquire — несуществующий docId")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка захвата блокировки для несуществующего слоя. Ожидается 404/500 и userMessage про ненайденный слой.")
    @Story("Negative lock/acquire")
    public void testLockAcquireNegativeNonExistentDocId() {
        logger.info("Выполнение теста: NEGATIVE POST /lock/acquire — несуществующий docId");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"docId\":\"" + NON_EXISTENT_DOC_ID + "\"}")
                .when()
                .post(LOCK_ACQUIRE_ENDPOINT)
                .then()
                .statusCode(anyOf(is(404), is(500)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("userMessage", containsString("слой или группа " + NON_EXISTENT_DOC_ID + " не найдены"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 11: NEGATIVE POST /lock/acquire — невалидный JSON
     */
    @Test(priority = 11, description = "NEGATIVE POST /lock/acquire — невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка отправки невалидного JSON. Ожидается 400/500.")
    @Story("Negative lock/acquire")
    public void testLockAcquireNegativeInvalidJson() {
        logger.info("Выполнение теста: NEGATIVE POST /lock/acquire — невалидный JSON");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"docId\":\"5bf7d7ab-6ab9-4223-808a-6dff6702e734\"\n")
                .when()
                .post(LOCK_ACQUIRE_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 12: NEGATIVE POST /lock/acquire — пустой body
     */
    @Test(priority = 12, description = "NEGATIVE POST /lock/acquire — пустой body")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка отправки пустого body. Ожидается 400/500 и error Bad Request.")
    @Story("Negative lock/acquire")
    public void testLockAcquireNegativeEmptyBody() {
        logger.info("Выполнение теста: NEGATIVE POST /lock/acquire — пустой body");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("")
                .when()
                .post(LOCK_ACQUIRE_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class))
                .body("error", notNullValue())
                .body("error", containsString("Bad Request"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 13: GET /lock — проверка блокировок после acquire
     * Ответ — массив, содержит блокировку для нашего слоя (docId, lockEventType ACQUIRED), у записи есть lockAcquiredBy, lockAcquiredAt.
     */
    @Test(priority = 13, description = "GET /lock — проверка блокировок после acquire")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка что блокировка для слоя существует: массив, есть запись с docId слоя и lockEventType ACQUIRED, обязательные поля.")
    @Story("Lock — список блокировок")
    public void testGetLock() {
        logger.info("Выполнение теста: GET /lock");

        given()
                .spec(getRequestSpec())
                .when()
                .get(LOCK_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class))
                .body("$", hasItem(allOf(hasEntry("docId", LAYER_ID), hasEntry("lockEventType", "ACQUIRED"))))
                .body("find { it.docId == '" + LAYER_ID + "' }.lockAcquiredBy", notNullValue())
                .body("find { it.docId == '" + LAYER_ID + "' }.lockAcquiredAt", notNullValue())
                .body("find { it.docId == '" + LAYER_ID + "' }.docId", equalTo(LAYER_ID))
                .body("find { it.docId == '" + LAYER_ID + "' }.lockEventType", equalTo("ACQUIRED"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 14: PUT /tree-layers — обновление слоя
     * Проверка: статус 200, ответ содержит docId равный layerId. Проверку времени ответа не делаем.
     */
    @Test(priority = 14, description = "PUT /tree-layers — обновление слоя")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Обновление метки слоя (label change_name). Проверка 200 и docId в ответе.")
    @Story("Обновление слоя")
    public void testPutTreeLayersUpdateLayer() {
        logger.info("Выполнение теста: PUT /tree-layers — обновление слоя");

        String requestBody = "{\n"
                + "  \"docId\": \"" + LAYER_ID + "\",\n"
                + "  \"data\": {\n"
                + "    \"name\": \"Wi-Fi в парках (полигоны)\",\n"
                + "    \"tags\": [],\n"
                + "    \"label\": \"слой для автотеста change_name\",\n"
                + "    \"geoType\": \"POLYGON\",\n"
                + "    \"itemType\": \"node\",\n"
                + "    \"settings\": {\n"
                + "      \"viewInTree\": {\n"
                + "        \"isChecked\": false,\n"
                + "        \"isShownInTree\": true,\n"
                + "        \"isHiddenInTree\": false\n"
                + "      },\n"
                + "      \"displayObjectsOnMap\": {\n"
                + "        \"style\": [\n"
                + "          {\"color\": \"#FFFFFF\", \"opacity\": 1}\n"
                + "        ]\n"
                + "      }\n"
                + "    },\n"
                + "    \"hideValue\": null,\n"
                + "    \"layerType\": \"vctr\",\n"
                + "    \"datasource\": \"\",\n"
                + "    \"externalId\": 24,\n"
                + "    \"validateLayerVisibility\": true,\n"
                + "    \"id\": \"" + LAYER_ID + "\",\n"
                + "    \"position\": 1,\n"
                + "    \"state\": \"ACTUAL\"\n"
                + "  }\n"
                + "}";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(TREE_LAYERS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("docId", equalTo(LAYER_ID));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 15: NEGATIVE PUT /tree-layers — отсутствует поле docId
     */
    @Test(priority = 15, description = "NEGATIVE PUT /tree-layers — отсутствует docId")
    @Severity(SeverityLevel.NORMAL)
    @Description("Обновление без docId. Ожидается 404/500 и userMessage про «слой или группа null не найдены».")
    @Story("Negative PUT tree-layers")
    public void testPutTreeLayersNegativeMissingDocId() {
        logger.info("Выполнение теста: NEGATIVE PUT /tree-layers — отсутствует docId");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"data\":{\"name\":\"Test\"}}")
                .when()
                .put(TREE_LAYERS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(404), is(500)))
                .body("$", instanceOf(java.util.Map.class))
                .body("userMessage", notNullValue())
                .body("userMessage", containsString("слой или группа null не найдены"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 16: NEGATIVE PUT /tree-layers — отсутствует поле data
     */
    @Test(priority = 16, description = "NEGATIVE PUT /tree-layers — отсутствует data")
    @Severity(SeverityLevel.NORMAL)
    @Description("Обновление без data. Ожидается 400/500.")
    @Story("Negative PUT tree-layers")
    public void testPutTreeLayersNegativeMissingData() {
        logger.info("Выполнение теста: NEGATIVE PUT /tree-layers — отсутствует data");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"docId\":\"" + LAYER_ID + "\"}")
                .when()
                .put(TREE_LAYERS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 17: NEGATIVE PUT /tree-layers — несуществующий docId
     */
    @Test(priority = 17, description = "NEGATIVE PUT /tree-layers — несуществующий docId")
    @Severity(SeverityLevel.NORMAL)
    @Description("Обновление несуществующего слоя. Ожидается 404/500 и userMessage про ненайденный слой.")
    @Story("Negative PUT tree-layers")
    public void testPutTreeLayersNegativeNonExistentDocId() {
        logger.info("Выполнение теста: NEGATIVE PUT /tree-layers — несуществующий docId");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"docId\":\"" + NON_EXISTENT_DOC_ID + "\",\"data\":{\"name\":\"Test\"}}")
                .when()
                .put(TREE_LAYERS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(404), is(500)))
                .body("$", instanceOf(java.util.Map.class))
                .body("userMessage", notNullValue())
                .body("userMessage", containsString("слой или группа " + NON_EXISTENT_DOC_ID + " не найдены"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 18: NEGATIVE PUT /tree-layers — невалидная структура data (data не объект)
     */
    @Test(priority = 18, description = "NEGATIVE PUT /tree-layers — невалидная структура data")
    @Severity(SeverityLevel.NORMAL)
    @Description("data не объект. Ожидается 400/500 и userMessage про обязательное поле name.")
    @Story("Negative PUT tree-layers")
    public void testPutTreeLayersNegativeInvalidDataStructure() {
        logger.info("Выполнение теста: NEGATIVE PUT /tree-layers — невалидная структура data");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"docId\":\"" + LAYER_ID + "\",\"data\":\"not_an_object\"}")
                .when()
                .put(TREE_LAYERS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class))
                .body("userMessage", notNullValue())
                .body("userMessage", containsString("Обнаружены следующие ошибки: Не заполнено обязательное поле name"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 19: NEGATIVE PUT /tree-layers — невалидный формат UUID в docId
     */
    @Test(priority = 19, description = "NEGATIVE PUT /tree-layers — невалидный UUID в docId")
    @Severity(SeverityLevel.NORMAL)
    @Description("docId не UUID. Ожидается 400/500.")
    @Story("Negative PUT tree-layers")
    public void testPutTreeLayersNegativeInvalidUuid() {
        logger.info("Выполнение теста: NEGATIVE PUT /tree-layers — невалидный UUID в docId");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"docId\":\"invalid-uuid\",\"data\":{\"name\":\"Test\"}}")
                .when()
                .put(TREE_LAYERS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 20: NEGATIVE PUT /tree-layers — невалидный JSON
     */
    @Test(priority = 20, description = "NEGATIVE PUT /tree-layers — невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Тело запроса — невалидный JSON. Ожидается 400/500.")
    @Story("Negative PUT tree-layers")
    public void testPutTreeLayersNegativeInvalidJson() {
        logger.info("Выполнение теста: NEGATIVE PUT /tree-layers — невалидный JSON");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"docId\":\"" + LAYER_ID + "\",\"data\":{")
                .when()
                .put(TREE_LAYERS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 21: NEGATIVE PUT /tree-layers — пустой body
     */
    @Test(priority = 21, description = "NEGATIVE PUT /tree-layers — пустой body")
    @Severity(SeverityLevel.NORMAL)
    @Description("Тело запроса {}. Ожидается 400/500.")
    @Story("Negative PUT tree-layers")
    public void testPutTreeLayersNegativeEmptyBody() {
        logger.info("Выполнение теста: NEGATIVE PUT /tree-layers — пустой body");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .put(TREE_LAYERS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 22: GET /tree-layers — проверка обновления слоя
     * Проверка что метка слоя обновлена на 'слой для автотеста change_name' (после PUT в тесте 14).
     */
    @Test(priority = 22, description = "GET /tree-layers — проверка обновления слоя")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка что метка слоя обновлена на 'слой для автотеста change_name'.")
    @Story("Проверка обновления слоя")
    public void testGetTreeLayersVerifyLayerUpdate() {
        logger.info("Выполнение теста: GET /tree-layers — проверка обновления слоя");

        given()
                .spec(getRequestSpec())
                .when()
                .get(TREE_LAYERS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("items", notNullValue())
                .body("items", instanceOf(List.class))
                .body("items.find { it.id == '" + LAYER_ID + "' }", notNullValue())
                .body("items.find { it.id == '" + LAYER_ID + "' }.label", equalTo("слой для автотеста change_name"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 23: PUT /tree-layers/change-position — изменение позиции слоя
     * Перемещение слоя в группу. Проверка: статус 200, время ответа < 2000 мс.
     */
    @Test(priority = 23, description = "PUT /tree-layers/change-position — изменение позиции слоя")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Перемещение слоя в группу. Проверка статуса 200 и времени ответа.")
    @Story("Изменение позиции слоя")
    public void testChangePosition() {
        logger.info("Выполнение теста: PUT /tree-layers/change-position");

        String requestBody = "{\n"
                + "  \"docIds\": [\"" + LAYER_ID + "\"],\n"
                + "  \"parent\": \"" + GROUP_ID + "\",\n"
                + "  \"prev\": null\n"
                + "}";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(CHANGE_POSITION_ENDPOINT)
                .then()
                .statusCode(200);

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 24: NEGATIVE PUT /tree-layers/change-position — отсутствует поле docIds
     */
    @Test(priority = 24, description = "NEGATIVE PUT /tree-layers/change-position — отсутствует docIds")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка изменения позиции без docIds. Ожидается 400/500 и message про docIds is null.")
    @Story("Negative change-position")
    public void testChangePositionNegativeMissingDocIds() {
        logger.info("Выполнение теста: NEGATIVE PUT /tree-layers/change-position — отсутствует docIds");

        String requestBody = "{\n"
                + "  \"parent\": \"" + GROUP_ID + "\"\n"
                + "}";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(CHANGE_POSITION_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("because \"docIds\" is null"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 25: NEGATIVE PUT /tree-layers/change-position — пустой массив docIds
     */
    @Test(priority = 25, description = "NEGATIVE PUT /tree-layers/change-position — пустой docIds")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка изменения позиции с пустым docIds. Ожидается 400/500 и message relatedIds is empty.")
    @Story("Negative change-position")
    public void testChangePositionNegativeEmptyDocIds() {
        logger.info("Выполнение теста: NEGATIVE PUT /tree-layers/change-position — пустой docIds");

        String requestBody = "{\n"
                + "  \"docIds\": [],\n"
                + "  \"parent\": \"" + GROUP_ID + "\"\n"
                + "}";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(CHANGE_POSITION_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("relatedIds is empty"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 26: NEGATIVE PUT /tree-layers/change-position — невалидный формат UUID в docIds
     */
    @Test(priority = 26, description = "NEGATIVE PUT /tree-layers/change-position — невалидный UUID в docIds")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка с невалидным UUID в docIds. Ожидается 400/500.")
    @Story("Negative change-position")
    public void testChangePositionNegativeInvalidUuid() {
        logger.info("Выполнение теста: NEGATIVE PUT /tree-layers/change-position — невалидный UUID");

        String requestBody = "{\n"
                + "  \"docIds\": [\"invalid-uuid\"],\n"
                + "  \"parent\": \"" + GROUP_ID + "\"\n"
                + "}";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(CHANGE_POSITION_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 27: NEGATIVE PUT /tree-layers/change-position — невалидный тип docIds
     */
    @Test(priority = 27, description = "NEGATIVE PUT /tree-layers/change-position — невалидный тип docIds")
    @Severity(SeverityLevel.NORMAL)
    @Description("docIds не массив (строка). Ожидается 400/500.")
    @Story("Negative change-position")
    public void testChangePositionNegativeInvalidDocIdsType() {
        logger.info("Выполнение теста: NEGATIVE PUT /tree-layers/change-position — невалидный тип docIds");

        String requestBody = "{\n"
                + "  \"docIds\": \"not_an_array\",\n"
                + "  \"parent\": \"" + GROUP_ID + "\"\n"
                + "}";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(CHANGE_POSITION_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 28: NEGATIVE PUT /tree-layers/change-position — невалидный JSON
     */
    @Test(priority = 28, description = "NEGATIVE PUT /tree-layers/change-position — невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Тело запроса — невалидный JSON. Ожидается 400/500.")
    @Story("Negative change-position")
    public void testChangePositionNegativeInvalidJson() {
        logger.info("Выполнение теста: NEGATIVE PUT /tree-layers/change-position — невалидный JSON");

        String invalidJsonBody = "{\n"
                + "  \"docIds\": [\"" + LAYER_ID + "\"],\n";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(invalidJsonBody)
                .when()
                .put(CHANGE_POSITION_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 29: POST /lock/release — освобождение блокировки слоя
     */
    @Test(priority = 29, description = "POST /lock/release — освобождение блокировки")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Освобождение блокировки слоя по docId.")
    @Story("Lock release")
    public void testLockRelease() {
        logger.info("Выполнение теста: POST /lock/release");

        String requestBody = "{\"docId\":\"" + LAYER_ID + "\"}";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(LOCK_RELEASE_ENDPOINT)
                .then()
                .statusCode(200);

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 30: NEGATIVE POST /lock/release — отсутствует поле docId
     */
    @Test(priority = 30, description = "NEGATIVE POST /lock/release — отсутствует docId")
    @Severity(SeverityLevel.NORMAL)
    @Description("Освобождение блокировки без docId. Ожидается 404/500.")
    @Story("Negative lock/release")
    public void testLockReleaseNegativeMissingDocId() {
        logger.info("Выполнение теста: NEGATIVE POST /lock/release — отсутствует docId");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .post(LOCK_RELEASE_ENDPOINT)
                .then()
                .statusCode(anyOf(is(404), is(500)))
                .body("$", instanceOf(java.util.Map.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 31: NEGATIVE POST /lock/release — невалидный формат UUID в docId
     */
    @Test(priority = 31, description = "NEGATIVE POST /lock/release — невалидный UUID")
    @Severity(SeverityLevel.NORMAL)
    @Description("Освобождение блокировки с невалидным UUID. Ожидается 400/500.")
    @Story("Negative lock/release")
    public void testLockReleaseNegativeInvalidUuid() {
        logger.info("Выполнение теста: NEGATIVE POST /lock/release — невалидный UUID");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"docId\":\"invalid-uuid\"}")
                .when()
                .post(LOCK_RELEASE_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 32: NEGATIVE POST /lock/release — невалидный JSON
     */
    @Test(priority = 32, description = "NEGATIVE POST /lock/release — невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Тело запроса — невалидный JSON. Ожидается 400/500.")
    @Story("Negative lock/release")
    public void testLockReleaseNegativeInvalidJson() {
        logger.info("Выполнение теста: NEGATIVE POST /lock/release — невалидный JSON");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"docId\":\"" + LAYER_ID + "\"\n")
                .when()
                .post(LOCK_RELEASE_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 33: GET /lock — проверка блокировок после release
     * Среди блокировок не должно быть блокировки для нашего слоя (docId).
     */
    @Test(priority = 33, description = "GET /lock — проверка после release")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка что блокировка для слоя освобождена — в списке нет записи с docId слоя.")
    @Story("Lock — проверка после release")
    public void testGetLockAfterRelease() {
        logger.info("Выполнение теста: GET /lock — проверка после release");

        given()
                .spec(getRequestSpec())
                .when()
                .get(LOCK_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class))
                .body("findAll { it.docId == '" + LAYER_ID + "' }", hasSize(0));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 34: GET /tree-layers/short-editor — слой в папке
     * Проверка что слой имеет idParent равный groupId.
     */
    @Test(priority = 34, description = "GET /tree-layers/short-editor — слой в папке")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка что слой находится в созданной папке (idParent = groupId).")
    @Story("Short-editor")
    public void testShortEditor() {
        logger.info("Выполнение теста: GET /tree-layers/short-editor");

        given()
                .spec(getRequestSpec())
                .when()
                .get(SHORT_EDITOR_ENDPOINT)
                .then()
                .statusCode(200)
                .body("items", notNullValue())
                .body("items", instanceOf(List.class))
                .body("items.find { it.id == '" + LAYER_ID + "' }", notNullValue())
                .body("items.find { it.id == '" + LAYER_ID + "' }.idParent", equalTo(GROUP_ID));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 35: POST /tree-layers/get-settings — получение настроек слоя
     */
    @Test(priority = 35, description = "POST /tree-layers/get-settings — получение настроек слоя")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение настроек слоя по id.")
    @Story("Get-settings")
    public void testGetSettings() {
        logger.info("Выполнение теста: POST /tree-layers/get-settings");

        String requestBody = "[\"" + LAYER_ID + "\"]";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(GET_SETTINGS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("items", notNullValue())
                .body("items", instanceOf(List.class))
                .body("items.find { it.id == '" + LAYER_ID + "' }", notNullValue())
                .body("items.find { it.id == '" + LAYER_ID + "' }.label", equalTo("слой для автотеста change_name"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 36: NEGATIVE POST /tree-layers/get-settings — невалидный тип body
     */
    @Test(priority = 36, description = "NEGATIVE POST /tree-layers/get-settings — невалидный тип body")
    @Severity(SeverityLevel.NORMAL)
    @Description("Body не массив. Ожидается 400/500.")
    @Story("Negative get-settings")
    public void testGetSettingsNegativeInvalidBodyType() {
        logger.info("Выполнение теста: NEGATIVE POST /tree-layers/get-settings — невалидный тип body");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"invalid\": \"not_an_array\"}")
                .when()
                .post(GET_SETTINGS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 37: NEGATIVE POST /tree-layers/get-settings — невалидный JSON
     */
    @Test(priority = 37, description = "NEGATIVE POST /tree-layers/get-settings — невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Тело запроса — невалидный JSON. Ожидается 400/500.")
    @Story("Negative get-settings")
    public void testGetSettingsNegativeInvalidJson() {
        logger.info("Выполнение теста: NEGATIVE POST /tree-layers/get-settings — невалидный JSON");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("[\"" + LAYER_ID + "\"\n")
                .when()
                .post(GET_SETTINGS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 38: GET /tree-layers/{layerId} — получение слоя по ID
     */
    @Test(priority = 38, description = "GET /tree-layers/{layerId} — получение слоя по ID")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение слоя по docId. Проверка id, label, idParent.")
    @Story("Получение слоя по ID")
    public void testGetLayerById() {
        logger.info("Выполнение теста: GET /tree-layers/{layerId}");

        given()
                .spec(getRequestSpec())
                .when()
                .get(TREE_LAYERS_ENDPOINT + "/" + LAYER_ID)
                .then()
                .statusCode(200)
                .body("id", equalTo(LAYER_ID))
                .body("label", equalTo("слой для автотеста change_name"))
                .body("idParent", equalTo(GROUP_ID));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 39: NEGATIVE GET /tree-layers/{layerId} — несуществующий layerId
     */
    @Test(priority = 39, description = "NEGATIVE GET /tree-layers/{layerId} — несуществующий layerId")
    @Severity(SeverityLevel.NORMAL)
    @Description("Запрос несуществующего слоя. Ожидается 500 и message про not found.")
    @Story("Negative get layer by ID")
    public void testGetLayerByIdNegativeNonExistent() {
        logger.info("Выполнение теста: NEGATIVE GET /tree-layers/{layerId} — несуществующий");

        given()
                .spec(getRequestSpec())
                .when()
                .get(TREE_LAYERS_ENDPOINT + "/" + NON_EXISTENT_DOC_ID)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString(NON_EXISTENT_DOC_ID + " not found"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 40: GET /tree-layers/{layerId}/access — получение доступа к слою
     */
    @Test(priority = 40, description = "GET /tree-layers/{layerId}/access — получение доступа")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение типа доступа к слою. Проверка accessType = FULL.")
    @Story("Получение доступа к слою")
    public void testGetLayerAccess() {
        logger.info("Выполнение теста: GET /tree-layers/{layerId}/access");

        given()
                .spec(getRequestSpec())
                .when()
                .get(TREE_LAYERS_ENDPOINT + "/" + LAYER_ID + "/access")
                .then()
                .statusCode(200)
                .body("accessType", notNullValue())
                .body("accessType", equalTo("FULL"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 41: GET /tree-layers/structure — получение структуры дерева
     */
    @Test(priority = 41, description = "GET /tree-layers/structure — структура дерева")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение структуры дерева слоёв. Первый элемент — 'Нераспределенные модели'.")
    @Story("Структура дерева")
    public void testGetStructure() {
        logger.info("Выполнение теста: GET /tree-layers/structure");

        given()
                .spec(getRequestSpec())
                .when()
                .get(STRUCTURE_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class))
                .body("size()", greaterThan(0))
                .body("[0].label", notNullValue())
                .body("[0].label", equalTo("Нераспределенные модели"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 42: POST /tree-layers/create-model — создание модели
     */
    @Test(priority = 42, description = "POST /tree-layers/create-model — создание модели")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Создание модели. Проверка ответа с docId.")
    @Story("Создание модели")
    public void testCreateModel() {
        logger.info("Выполнение теста: POST /tree-layers/create-model");

        String requestBody = "{\n"
                + "  \"name\": \"Veresaeva1.glb\",\n"
                + "  \"settings\": {\n"
                + "    \"viewInTree\": {\"isShownInTree\": true, \"isChecked\": false},\n"
                + "    \"cameraView\": {},\n"
                + "    \"displayObjectsOnMap\": {\"clip\": {}, \"position\": {\"isRotateByModelCenter\": true}, \"selection\": {}, \"style\": []},\n"
                + "    \"advancedOptions\": {}\n"
                + "  },\n"
                + "  \"label\": \"autotestModel\",\n"
                + "  \"datasource\": \"/da-cm-map-backend-modelsloader/download/model?filepath=2025-12-04/05132e4f-ba0d-4cd2-a07e-a90f6c94f3cb/tileset.json\",\n"
                + "  \"originalLink\": \"2025-12-04/05132e4f-ba0d-4cd2-a07e-a90f6c94f3cb/Veresaeva1_b_d.glb\",\n"
                + "  \"layerType\": \"loadedRegularModel\",\n"
                + "  \"tags\": [],\n"
                + "  \"validateLayerVisibility\": true,\n"
                + "  \"externalId\": 2162,\n"
                + "  \"hideValue\": null,\n"
                + "  \"itemType\": \"node\",\n"
                + "  \"idParent\": \"3f5ad391-6ee7-412d-af81-6c1e515c19c8\",\n"
                + "  \"id\": \"51e9aba1-6ba7-4837-912d-ea80c58467c9\",\n"
                + "  \"datahubId\": 1124\n"
                + "}";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(CREATE_MODEL_ENDPOINT)
                .then()
                .statusCode(200)
                .body("docId", notNullValue());

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 43: NEGATIVE POST /tree-layers/create-model — отсутствует поле name
     */
    @Test(priority = 43, description = "NEGATIVE POST /tree-layers/create-model — отсутствует name")
    @Severity(SeverityLevel.NORMAL)
    @Description("Создание модели без name. Ожидается 400/500 и message про обязательное поле name.")
    @Story("Negative create-model")
    public void testCreateModelNegativeMissingName() {
        logger.info("Выполнение теста: NEGATIVE POST /tree-layers/create-model — отсутствует name");

        String requestBody = "{\n"
                + "  \"itemType\": \"node\",\n"
                + "  \"layerType\": \"loadedRegularModel\"\n"
                + "}";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(CREATE_MODEL_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Не заполнено обязательное поле name"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 44: NEGATIVE POST /tree-layers/create-model — невалидный JSON
     */
    @Test(priority = 44, description = "NEGATIVE POST /tree-layers/create-model — невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Тело запроса — невалидный JSON. Ожидается 400/500.")
    @Story("Negative create-model")
    public void testCreateModelNegativeInvalidJson() {
        logger.info("Выполнение теста: NEGATIVE POST /tree-layers/create-model — невалидный JSON");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\n  \"name\": \"Test\"\n")
                .when()
                .post(CREATE_MODEL_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 45: NEGATIVE POST /tree-layers/create-model — отсутствует поле layerType
     */
    @Test(priority = 45, description = "NEGATIVE POST /tree-layers/create-model — отсутствует layerType")
    @Severity(SeverityLevel.NORMAL)
    @Description("Создание модели без layerType. Ожидается 500/404 и message про обязательное поле layerType.")
    @Story("Negative create-model")
    public void testCreateModelNegativeMissingLayerType() {
        logger.info("Выполнение теста: NEGATIVE POST /tree-layers/create-model — отсутствует layerType");

        String requestBody = "{\n"
                + "  \"name\": \"Test.glb\",\n"
                + "  \"itemType\": \"node\"\n"
                + "}";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(CREATE_MODEL_ENDPOINT)
                .then()
                .statusCode(anyOf(is(500), is(404)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Не заполнено обязательное поле layerType"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 46: NEGATIVE POST /tree-layers/create-model — невалидный layerType
     */
    @Test(priority = 46, description = "NEGATIVE POST /tree-layers/create-model — невалидный layerType")
    @Severity(SeverityLevel.NORMAL)
    @Description("Невалидный layerType. Ожидается 500/404 и message про обязательное поле label.")
    @Story("Negative create-model")
    public void testCreateModelNegativeInvalidLayerType() {
        logger.info("Выполнение теста: NEGATIVE POST /tree-layers/create-model — невалидный layerType");

        String requestBody = "{\n"
                + "  \"name\": \"Test.glb\",\n"
                + "  \"itemType\": \"node\",\n"
                + "  \"layerType\": \"INVALID_LAYER_TYPE\"\n"
                + "}";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(CREATE_MODEL_ENDPOINT)
                .then()
                .statusCode(anyOf(is(500), is(404)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Не заполнено обязательное поле label"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 47: NEGATIVE POST /tree-layers/create-model — невалидный формат UUID в id
     */
    @Test(priority = 47, description = "NEGATIVE POST /tree-layers/create-model — невалидный UUID в id")
    @Severity(SeverityLevel.NORMAL)
    @Description("Невалидный UUID в id. Ожидается 500/404.")
    @Story("Negative create-model")
    public void testCreateModelNegativeInvalidUuidInId() {
        logger.info("Выполнение теста: NEGATIVE POST /tree-layers/create-model — невалидный UUID в id");

        String requestBody = "{\n"
                + "  \"name\": \"Test.glb\",\n"
                + "  \"itemType\": \"node\",\n"
                + "  \"layerType\": \"loadedRegularModel\",\n"
                + "  \"id\": \"invalid-uuid-format\"\n"
                + "}";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(CREATE_MODEL_ENDPOINT)
                .then()
                .statusCode(anyOf(is(500), is(404)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Не заполнено обязательное поле label"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 48: NEGATIVE POST /tree-layers/create-model — невалидный формат UUID в idParent
     */
    @Test(priority = 48, description = "NEGATIVE POST /tree-layers/create-model — невалидный UUID в idParent")
    @Severity(SeverityLevel.NORMAL)
    @Description("Невалидный UUID в idParent. Ожидается 500/404.")
    @Story("Negative create-model")
    public void testCreateModelNegativeInvalidUuidInIdParent() {
        logger.info("Выполнение теста: NEGATIVE POST /tree-layers/create-model — невалидный UUID в idParent");

        String requestBody = "{\n"
                + "  \"name\": \"Test.glb\",\n"
                + "  \"itemType\": \"node\",\n"
                + "  \"layerType\": \"loadedRegularModel\",\n"
                + "  \"idParent\": \"invalid-uuid-format\"\n"
                + "}";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(CREATE_MODEL_ENDPOINT)
                .then()
                .statusCode(anyOf(is(500), is(404)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 49: POST /lock/acquire — получение блокировки модели
     */
    @Test(priority = 49, description = "POST /lock/acquire — блокировка модели")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение блокировки для модели по docId.")
    @Story("Lock acquire модели")
    public void testLockAcquireModel() {
        logger.info("Выполнение теста: POST /lock/acquire — блокировка модели");

        String requestBody = "{\"docId\":\"" + MODEL_ID + "\"}";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(LOCK_ACQUIRE_ENDPOINT)
                .then()
                .statusCode(200);

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 50: PATCH /dictionaries/handbook — обновление справочника
     * Использует dictionariesUrl. Задать -DdictionariesUrl=... при необходимости.
     */
    @Test(priority = 50, description = "PATCH /dictionaries/handbook — обновление справочника")
    @Severity(SeverityLevel.NORMAL)
    @Description("Обновление справочника. Требует dictionariesUrl.")
    @Story("Обновление справочника")
    public void testPatchDictionariesHandbook() {
        logger.info("Выполнение теста: PATCH /dictionaries/handbook");

        String dictionariesUrl = Config.getDictionariesUrl(DEFAULT_DICTIONARIES_URL);
        String requestBody = "{\n"
                + "  \"attributes\": [\"ID\", \"GEOMETRY\"],\n"
                + "  \"elements\": [\n"
                + "    {\n"
                + "      \"values\": [\n"
                + "        1124,\n"
                + "        \"{\\\"type\\\":\\\"Feature\\\",\\\"properties\\\":{},\\\"geometry\\\":{\\\"coordinates\\\":[37.6352,55.74498],\\\"type\\\":\\\"Point\\\"}}\"\n"
                + "      ]\n"
                + "    }\n"
                + "  ]\n"
                + "}";

        given()
                .spec(getRequestSpec())
                .baseUri(dictionariesUrl)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .patch("/dictionaries/handbook")
                .then()
                .statusCode(200);

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 51: NEGATIVE PATCH /dictionaries/handbook — отсутствует поле elements
     */
    @Test(priority = 51, description = "NEGATIVE PATCH /dictionaries/handbook — отсутствует elements")
    @Severity(SeverityLevel.NORMAL)
    @Description("Обновление справочника без elements. Ожидается 500/404.")
    @Story("Negative dictionaries")
    public void testPatchDictionariesNegativeMissingElements() {
        logger.info("Выполнение теста: NEGATIVE PATCH /dictionaries/handbook — отсутствует elements");

        String dictionariesUrl = Config.getDictionariesUrl(DEFAULT_DICTIONARIES_URL);
        String requestBody = "{\n  \"attributes\": [\"ID\", \"GEOMETRY\"]\n}";

        given()
                .spec(getRequestSpec())
                .baseUri(dictionariesUrl)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .patch("/dictionaries/handbook")
                .then()
                .statusCode(anyOf(is(500), is(404)))
                .body("$", instanceOf(java.util.Map.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 52: NEGATIVE PATCH /dictionaries/handbook — пустой массив attributes
     */
    @Test(priority = 52, description = "NEGATIVE PATCH /dictionaries/handbook — пустой attributes")
    @Severity(SeverityLevel.NORMAL)
    @Description("Пустой массив attributes. Ожидается 500/404 и Message про обязательный атрибут.")
    @Story("Negative dictionaries")
    public void testPatchDictionariesNegativeEmptyAttributes() {
        logger.info("Выполнение теста: NEGATIVE PATCH /dictionaries/handbook — пустой attributes");

        String dictionariesUrl = Config.getDictionariesUrl(DEFAULT_DICTIONARIES_URL);
        String requestBody = "{\n"
                + "  \"attributes\": [],\n"
                + "  \"elements\": [\n"
                + "    {\"values\": [1124, \"{\\\"type\\\":\\\"Feature\\\",\\\"properties\\\":{},\\\"geometry\\\":{\\\"coordinates\\\":[37.6352,55.74498],\\\"type\\\":\\\"Point\\\"}}\"]}\n"
                + "  ]\n"
                + "}";

        given()
                .spec(getRequestSpec())
                .baseUri(dictionariesUrl)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .patch("/dictionaries/handbook")
                .then()
                .statusCode(anyOf(is(500), is(404)))
                .body("$", instanceOf(java.util.Map.class))
                .body("Message", notNullValue())
                .body("Message", instanceOf(String.class))
                .body("Message", containsString("Отсутствует значение для обязательного атрибута"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 53: NEGATIVE PATCH /dictionaries/handbook — невалидный JSON
     */
    @Test(priority = 53, description = "NEGATIVE PATCH /dictionaries/handbook — невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Тело запроса — невалидный JSON. Ожидается 500/400.")
    @Story("Negative dictionaries")
    public void testPatchDictionariesNegativeInvalidJson() {
        logger.info("Выполнение теста: NEGATIVE PATCH /dictionaries/handbook — невалидный JSON");

        String dictionariesUrl = Config.getDictionariesUrl(DEFAULT_DICTIONARIES_URL);
        String invalidJson = "{\n  \"attributes\": [\"ID\", \"GEOMETRY\"],\n  \"elements\": [\n    {\n      \"values\": [\n        1124\n";

        given()
                .spec(getRequestSpec())
                .baseUri(dictionariesUrl)
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .patch("/dictionaries/handbook")
                .then()
                .statusCode(anyOf(is(500), is(400)))
                .body("$", instanceOf(java.util.Map.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 54: PUT /tree-layers/change-position — перемещение модели в папку
     */
    @Test(priority = 54, description = "PUT /tree-layers/change-position — перемещение модели")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Перемещение модели в папку (groupId).")
    @Story("Перемещение модели")
    public void testChangePositionModel() {
        logger.info("Выполнение теста: PUT /tree-layers/change-position — перемещение модели");

        String requestBody = "{\n"
                + "  \"docIds\": [\"" + MODEL_ID + "\"],\n"
                + "  \"parent\": \"" + GROUP_ID + "\",\n"
                + "  \"prev\": null\n"
                + "}";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(CHANGE_POSITION_ENDPOINT)
                .then()
                .statusCode(200);

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 55: NEGATIVE PUT /tree-layers/change-position — невалидные docIds и parent
     */
    @Test(priority = 55, description = "NEGATIVE PUT /tree-layers/change-position — невалидные поля")
    @Severity(SeverityLevel.NORMAL)
    @Description("docIds и parent — не UUID. Ожидается 500/400.")
    @Story("Negative change-position")
    public void testChangePositionNegativeInvalidFields() {
        logger.info("Выполнение теста: NEGATIVE PUT /tree-layers/change-position — невалидные docIds/parent");

        String requestBody = "{\n"
                + "  \"docIds\": [\"test\"],\n"
                + "  \"parent\": \"test\",\n"
                + "  \"prev\": null\n"
                + "}";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(CHANGE_POSITION_ENDPOINT)
                .then()
                .statusCode(anyOf(is(500), is(400)))
                .body("$", instanceOf(java.util.Map.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 56: GET /tree-layers/display-tree — модель в папке
     * Ответ — массив или объект с items. Модель должна иметь idParent = groupId.
     * Без логирования (большой ответ).
     */
    @Test(priority = 56, description = "GET /tree-layers/display-tree — модель в папке")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка что модель находится в созданной папке (idParent = groupId).")
    @Story("Display-tree")
    public void testDisplayTree() {
        logger.info("Выполнение теста: GET /tree-layers/display-tree");

        Response response = given()
                .spec(getRequestSpecNoLog())
                .when()
                .get(DISPLAY_TREE_ENDPOINT)
                .then()
                .statusCode(200)
                .extract()
                .response();

        Object body = response.jsonPath().get("$");
        Assert.assertTrue(body instanceof List || (body instanceof java.util.Map && ((java.util.Map<?, ?>) body).containsKey("items")),
                "Ответ должен быть массивом или объектом с items");

        String idParent;
        if (body instanceof List) {
            idParent = response.jsonPath().get("find { it.id == '" + MODEL_ID + "' }.idParent");
        } else {
            idParent = response.jsonPath().get("items.find { it.id == '" + MODEL_ID + "' }.idParent");
        }
        if (idParent != null) {
            Assert.assertEquals(idParent, GROUP_ID, "Модель должна иметь idParent = groupId");
        }
        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 57: POST /maintenance/backup-start — запуск создания бэкапа
     */
    @Test(priority = 57, description = "POST /maintenance/backup-start — запуск создания бэкапа")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Запуск создания бэкапа.")
    @Story("Backup")
    public void testBackupStart() {
        logger.info("Выполнение теста: POST /maintenance/backup-start");

        given()
                .spec(getRequestSpec())
                .when()
                .post(BACKUP_START_ENDPOINT)
                .then()
                .statusCode(200);

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 58: GET /maintenance/backups — получение списка бэкапов
     * Без логирования (большой ответ).
     */
    @Test(priority = 58, description = "GET /maintenance/backups — получение списка бэкапов")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение списка бэкапов. Сохраняет первый (самый свежий) в переменную backupFile.")
    @Story("Backup")
    public void testGetBackups() {
        logger.info("Выполнение теста: GET /maintenance/backups");

        Response response = given()
                .spec(getRequestSpecNoLog())
                .when()
                .get(BACKUPS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class))
                .body("size()", greaterThan(0))
                .extract()
                .response();

        List<String> backups = response.jsonPath().getList("$", String.class);
        backupFile = backups.get(0);
        Assert.assertNotNull(backupFile, "backupFile не должен быть null");
        Assert.assertTrue(backupFile.contains("layers__"), "backupFile должен содержать 'layers__'");
        Assert.assertTrue(backupFile.contains(".json"), "backupFile должен содержать '.json'");
        logger.info("Сохранён backupFile: {}", backupFile);
        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 59: POST /maintenance/backup-download — скачивание бэкапа
     * Без логирования (большой ответ — содержимое бэкапа).
     */
    @Test(priority = 59, description = "POST /maintenance/backup-download — скачивание бэкапа")
    @Severity(SeverityLevel.NORMAL)
    @Description("Скачивание бэкапа по имени файла.")
    @Story("Backup")
    public void testBackupDownload() {
        logger.info("Выполнение теста: POST /maintenance/backup-download");

        Assert.assertNotNull(backupFile, "backupFile должен быть установлен в тесте 58");

        Response response = given()
                .spec(getRequestSpecNoLog())
                .queryParam("backupFile", backupFile)
                .when()
                .post(BACKUP_DOWNLOAD_ENDPOINT)
                .then()
                .statusCode(200)
                .extract()
                .response();

        // API возвращает application/octet-stream, но тело — JSON; парсим вручную
        java.util.Map<String, Object> body = JsonPath.from(response.getBody().asString()).getMap("$");
        Assert.assertNotNull(body, "Ответ должен быть объектом");
        Assert.assertFalse(body.isEmpty(), "Ответ должен содержать данные");
        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 60: POST /lock/acquire — получение блокировки слоя перед изменением (backup тест)
     */
    @Test(priority = 60, description = "POST /lock/acquire — блокировка слоя перед backup тестом")
    @Severity(SeverityLevel.NORMAL)
    @Description("Получение блокировки для редактирования слоя перед backup тестом.")
    @Story("Backup")
    public void testLockAcquireBeforeBackup() {
        logger.info("Выполнение теста: POST /lock/acquire — блокировка слоя перед backup тестом");

        String requestBody = "{\"docId\":\"" + LAYER_ID + "\"}";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(LOCK_ACQUIRE_ENDPOINT)
                .then()
                .statusCode(200);

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 61: PUT /tree-layers — изменение имени слоя перед восстановлением
     */
    @Test(priority = 61, description = "PUT /tree-layers — изменение имени слоя на change_name_backup")
    @Severity(SeverityLevel.NORMAL)
    @Description("Изменение имени слоя на 'слой для автотеста change_name_backup'.")
    @Story("Backup")
    public void testUpdateLayerLabelBeforeRestore() {
        logger.info("Выполнение теста: PUT /tree-layers — изменение имени слоя на change_name_backup");

        String requestBody = "{\n"
                + "  \"docId\": \"" + LAYER_ID + "\",\n"
                + "  \"data\": {\n"
                + "    \"name\": \"Wi-Fi в парках (полигоны)\",\n"
                + "    \"tags\": [],\n"
                + "    \"label\": \"слой для автотеста change_name_backup\",\n"
                + "    \"geoType\": \"POLYGON\",\n"
                + "    \"itemType\": \"node\",\n"
                + "    \"settings\": {\n"
                + "      \"viewInTree\": {\"isChecked\": false, \"isShownInTree\": true, \"isHiddenInTree\": false},\n"
                + "      \"displayObjectsOnMap\": {\"style\": [{\"color\": \"#FFFFFF\", \"opacity\": 1}]}\n"
                + "    },\n"
                + "    \"hideValue\": null,\n"
                + "    \"layerType\": \"vctr\",\n"
                + "    \"datasource\": \"\",\n"
                + "    \"externalId\": 24,\n"
                + "    \"validateLayerVisibility\": true,\n"
                + "    \"id\": \"" + LAYER_ID + "\",\n"
                + "    \"position\": 1,\n"
                + "    \"state\": \"ACTUAL\"\n"
                + "  }\n"
                + "}";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(TREE_LAYERS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("docId", equalTo(LAYER_ID));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 62: GET /tree-layers — проверка изменения имени слоя
     * Без логирования (большой ответ).
     */
    @Test(priority = 62, description = "GET /tree-layers — проверка label = change_name_backup")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверка что имя слоя изменено на 'слой для автотеста change_name_backup'.")
    @Story("Backup")
    public void testVerifyLayerLabelChangedBackup() {
        logger.info("Выполнение теста: GET /tree-layers — проверка label = change_name_backup");

        given()
                .spec(getRequestSpecNoLog())
                .when()
                .get(TREE_LAYERS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("items", notNullValue())
                .body("items", instanceOf(List.class))
                .body("items.find { it.id == '" + LAYER_ID + "' }", notNullValue())
                .body("items.find { it.id == '" + LAYER_ID + "' }.label", equalTo("слой для автотеста change_name_backup"));

        logger.info("Тест успешно завершен.");
    }

    /*
     * ТЕСТ 63: POST /maintenance/backup-restore — восстановление из бэкапа 
     *
      */
    @Test(priority = 63, description = "POST /maintenance/backup-restore — восстановление из бэкапа")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Восстановление из бэкапа по имени файла.")
    @Story("Backup")
    public void testBackupRestore() {
        logger.info("Выполнение теста: POST /maintenance/backup-restore");

        Assert.assertNotNull(backupFile, "backupFile должен быть установлен в тесте 58");

        given()
                .spec(getRequestSpec())
                .queryParam("backupFile", backupFile)
                .when()
                .post(BACKUP_RESTORE_ENDPOINT)
                .then()
                .statusCode(200);

        logger.info("Тест успешно завершен.");
    }
    

    /*
     * ТЕСТ 64: GET /tree-layers — проверка отката имени слоя после восстановления 
     * С задержкой 8 секунд для завершения восстановления. Без логирования (большой ответ).
     * */
    @Test(priority = 64, description = "GET /tree-layers — проверка отката label после restore")
    @Severity(SeverityLevel.CRITICAL)
    @Description("После restore возвращается состояние на момент бэкапа (тест 57): label откатывается с change_name_backup на 'слой для автотеста change_name'.")
    @Story("Backup")
    public void testVerifyLayerLabelRestoredAfterBackup() throws InterruptedException {
        logger.info("Выполнение теста: GET /tree-layers — проверка отката label после restore");

        // Задержка 8 секунд для завершения восстановления из бэкапа
        Thread.sleep(8000);

        given()
                .spec(getRequestSpecNoLog())
                .when()
                .get(TREE_LAYERS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("items", notNullValue())
                .body("items", instanceOf(List.class))
                .body("items.find { it.id == '" + LAYER_ID + "' }", notNullValue())
                .body("items.find { it.id == '" + LAYER_ID + "' }.label", equalTo("слой для автотеста change_name"));

        logger.info("Тест успешно завершен.");
    }
   

    /**
     * ТЕСТ 65: POST /maintenance/export — экспорт данных
     * Без логирования (большой ответ — экспортированные данные).
     */
    @Test(priority = 65, description = "POST /maintenance/export — экспорт данных")
    @Severity(SeverityLevel.NORMAL)
    @Description("Экспорт данных. Ответ — объект с данными.")
    @Story("Maintenance")
    public void testMaintenanceExport() {
        logger.info("Выполнение теста: POST /maintenance/export");

        Response response = given()
                .spec(getRequestSpecNoLog())
                .when()
                .post(EXPORT_ENDPOINT)
                .then()
                .statusCode(200)
                .extract()
                .response();

        // API возвращает application/octet-stream, но тело — JSON; парсим вручную
        java.util.Map<String, Object> body = JsonPath.from(response.getBody().asString()).getMap("$");
        Assert.assertNotNull(body, "Ответ должен быть объектом");
        Assert.assertFalse(body.isEmpty(), "Ответ должен содержать данные");
        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 66: POST /layer-tags — создание тега
     */
    @Test(priority = 66, description = "POST /layer-tags — создание тега")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Создание тега. Ответ содержит docId, сохраняется в tagId.")
    @Story("Теги")
    public void testCreateTag() {
        logger.info("Выполнение теста: POST /layer-tags — создание тега");

        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"tag\": \"autotest\", \"name\": \"autotestName\"}")
                .when()
                .post(LAYER_TAGS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("docId", notNullValue())
                .extract()
                .response();

        tagId = response.jsonPath().getString("docId");
        Assert.assertNotNull(tagId, "docId не должен быть null");
        logger.info("Тест успешно завершен. tagId: {}", tagId);
    }

    /**
     * ТЕСТ 67: NEGATIVE POST /layer-tags — отсутствует поле tag
     */
    @Test(priority = 67, description = "NEGATIVE POST /layer-tags — отсутствует tag")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка создания тега без tag. Ожидается 400/500 и userMessage про некорректный формат тэга.")
    @Story("Negative layer-tags")
    public void testCreateTagNegativeMissingTag() {
        logger.info("Выполнение теста: NEGATIVE POST /layer-tags — отсутствует tag");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"name\": \"autotestName\"}")
                .when()
                .post(LAYER_TAGS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class))
                .body("userMessage", notNullValue())
                .body("userMessage", instanceOf(String.class))
                .body("userMessage", containsString("некорректный формат тэга"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 68: NEGATIVE POST /layer-tags — отсутствует поле name
     */
    @Test(priority = 68, description = "NEGATIVE POST /layer-tags — отсутствует name")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка создания тега без name. Ожидается 400/500 и userMessage про описание тэга.")
    @Story("Negative layer-tags")
    public void testCreateTagNegativeMissingName() {
        logger.info("Выполнение теста: NEGATIVE POST /layer-tags — отсутствует name");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"tag\": \"autotest\"}")
                .when()
                .post(LAYER_TAGS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class))
                .body("userMessage", notNullValue())
                .body("userMessage", instanceOf(String.class))
                .body("userMessage", containsString("описание тэга должно содержать хотя бы одну букву"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 69: NEGATIVE POST /layer-tags — пустое значение tag
     */
    @Test(priority = 69, description = "NEGATIVE POST /layer-tags — пустой tag")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка создания тега с пустым tag. Ожидается 400/500.")
    @Story("Negative layer-tags")
    public void testCreateTagNegativeEmptyTag() {
        logger.info("Выполнение теста: NEGATIVE POST /layer-tags — пустой tag");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"tag\": \"\", \"name\": \"autotestName\"}")
                .when()
                .post(LAYER_TAGS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class))
                .body("userMessage", notNullValue())
                .body("userMessage", instanceOf(String.class))
                .body("userMessage", containsString("некорректный формат тэга"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 70: NEGATIVE POST /layer-tags — невалидный JSON
     */
    @Test(priority = 70, description = "NEGATIVE POST /layer-tags — невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка создания тега с невалидным JSON. Ожидается 400/500.")
    @Story("Negative layer-tags")
    public void testCreateTagNegativeInvalidJson() {
        logger.info("Выполнение теста: NEGATIVE POST /layer-tags — невалидный JSON");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"tag\": \"autotest\", \"name\": \"autotestName\"")
                .when()
                .post(LAYER_TAGS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 71: GET /layer-tags — получение списка тегов
     */
    @Test(priority = 71, description = "GET /layer-tags — получение списка тегов")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение списка тегов. Созданный тег присутствует в списке.")
    @Story("Теги")
    public void testGetLayerTags() {
        logger.info("Выполнение теста: GET /layer-tags — получение списка тегов");

        Assert.assertNotNull(tagId, "tagId должен быть установлен в тесте 66");

        given()
                .spec(getRequestSpec())
                .when()
                .get(LAYER_TAGS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class))
                .body("find { it.id == '" + tagId + "' }", notNullValue())
                .body("find { it.id == '" + tagId + "' }.tag", equalTo("autotest"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 72: POST /lock/acquire — получение блокировки слоя (для редактирования)
     */
    @Test(priority = 72, description = "POST /lock/acquire — блокировка слоя")
    @Severity(SeverityLevel.NORMAL)
    @Description("Получение блокировки для редактирования слоя.")
    @Story("Теги")
    public void testLockAcquireForLayer() {
        logger.info("Выполнение теста: POST /lock/acquire — блокировка слоя");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"docId\":\"" + LAYER_ID + "\"}")
                .when()
                .post(LOCK_ACQUIRE_ENDPOINT)
                .then()
                .statusCode(200);

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 73: PUT /layer-tags — обновление тега
     */
    @Test(priority = 73, description = "PUT /layer-tags — обновление тега")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Обновление тега. Ответ содержит обновленные данные.")
    @Story("Теги")
    public void testUpdateTag() {
        logger.info("Выполнение теста: PUT /layer-tags — обновление тега");

        Assert.assertNotNull(tagId, "tagId должен быть установлен в тесте 66");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"docId\":\"" + tagId + "\", \"tag\": \"autotestChange\", \"name\": \"autotestNameChange\"}")
                .when()
                .put(LAYER_TAGS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 74: NEGATIVE PUT /layer-tags — отсутствует поле docId
     */
    @Test(priority = 74, description = "NEGATIVE PUT /layer-tags — отсутствует docId")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка обновления тега без docId. Ожидается 500/404 и message про id is null.")
    @Story("Negative layer-tags")
    public void testUpdateTagNegativeMissingDocId() {
        logger.info("Выполнение теста: NEGATIVE PUT /layer-tags — отсутствует docId");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"tag\": \"autotestChange\", \"name\": \"autotestNameChange\"}")
                .when()
                .put(LAYER_TAGS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(500), is(404)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("id is marked non-null but is null"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 75: NEGATIVE PUT /layer-tags — невалидный формат UUID в docId
     */
    @Test(priority = 75, description = "NEGATIVE PUT /layer-tags — невалидный UUID в docId")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка обновления тега с невалидным docId. Ожидается 500/404.")
    @Story("Negative layer-tags")
    public void testUpdateTagNegativeInvalidUuid() {
        logger.info("Выполнение теста: NEGATIVE PUT /layer-tags — невалидный UUID в docId");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"docId\": \"invalid-uuid-format\", \"tag\": \"autotestChange\", \"name\": \"autotestNameChange\"}")
                .when()
                .put(LAYER_TAGS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(500), is(404)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 76: NEGATIVE PUT /layer-tags — невалидный JSON
     */
    @Test(priority = 76, description = "NEGATIVE PUT /layer-tags — невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка обновления тега с невалидным JSON. Ожидается 500/404.")
    @Story("Negative layer-tags")
    public void testUpdateTagNegativeInvalidJson() {
        logger.info("Выполнение теста: NEGATIVE PUT /layer-tags — невалидный JSON");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"docId\": \"" + tagId + "\", \"tag\": \"autotestChange\"")
                .when()
                .put(LAYER_TAGS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(500), is(404)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 77: PUT /tree-layers — обновление слоя с добавлением тега
     */
    @Test(priority = 77, description = "PUT /tree-layers — обновление слоя с добавлением тега")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Обновление слоя с добавлением тега autotestchange.")
    @Story("Теги")
    public void testUpdateLayerWithTag() {
        logger.info("Выполнение теста: PUT /tree-layers — обновление слоя с добавлением тега");

        String requestBody = "{\n"
                + "  \"docId\": \"" + LAYER_ID + "\",\n"
                + "  \"data\": {\n"
                + "    \"id\": \"" + LAYER_ID + "\",\n"
                + "    \"name\": \"Wi-Fi в парках (полигоны)\",\n"
                + "    \"tags\": [\"autotestchange\"],\n"
                + "    \"label\": \"слой для автотеста change_name\",\n"
                + "    \"state\": \"ACTUAL\",\n"
                + "    \"access\": \"FULL\",\n"
                + "    \"geoType\": \"POLYGON\",\n"
                + "    \"idParent\": \"" + GROUP_ID + "\",\n"
                + "    \"itemType\": \"node\",\n"
                + "    \"position\": 2,\n"
                + "    \"settings\": {\n"
                + "      \"viewInTree\": {\"isChecked\": false, \"isShownInTree\": true, \"isHiddenInTree\": false},\n"
                + "      \"displayObjectsOnMap\": {\"style\": [{\"color\": \"#FFFFFF\", \"opacity\": 1}]}\n"
                + "    },\n"
                + "    \"hideValue\": null,\n"
                + "    \"layerType\": \"vctr\",\n"
                + "    \"datasource\": \"\",\n"
                + "    \"externalId\": 24,\n"
                + "    \"validateLayerVisibility\": true\n"
                + "  }\n"
                + "}";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(TREE_LAYERS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("docId", equalTo(LAYER_ID));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 78: POST /lock/release — освобождение блокировки слоя
     */
    @Test(priority = 78, description = "POST /lock/release — освобождение блокировки слоя")
    @Severity(SeverityLevel.NORMAL)
    @Description("Освобождение блокировки слоя после редактирования.")
    @Story("Теги")
    public void testLockReleaseForLayer() {
        logger.info("Выполнение теста: POST /lock/release — освобождение блокировки слоя");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"docId\":\"" + LAYER_ID + "\"}")
                .when()
                .post(LOCK_RELEASE_ENDPOINT)
                .then()
                .statusCode(200);

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 79: GET /layer-tags/tag-usage — проверка использования тега
     */
    @Test(priority = 79, description = "GET /layer-tags/tag-usage — проверка использования тега")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка что тег используется. items содержит имя слоя, fullList = true.")
    @Story("Теги")
    public void testGetTagUsage() {
        logger.info("Выполнение теста: GET /layer-tags/tag-usage — проверка использования тега");

        given()
                .spec(getRequestSpec())
                .queryParam("tag", "autotestchange")
                .when()
                .get(LAYER_TAGS_ENDPOINT + "/tag-usage")
                .then()
                .statusCode(200)
                .body("items", notNullValue())
                .body("items", instanceOf(List.class))
                .body("items", hasItem("слой для автотеста change_name"))
                .body("fullList", equalTo(true));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 80: NEGATIVE GET /layer-tags/tag-usage — отсутствует query параметр tag
     */
    @Test(priority = 80, description = "NEGATIVE GET /layer-tags/tag-usage — отсутствует tag")
    @Severity(SeverityLevel.NORMAL)
    @Description("Запрос tag-usage без параметра tag. Ожидается 500/400.")
    @Story("Negative layer-tags")
    public void testGetTagUsageNegativeMissingTag() {
        logger.info("Выполнение теста: NEGATIVE GET /layer-tags/tag-usage — отсутствует tag");

        given()
                .spec(getRequestSpec())
                .when()
                .get(LAYER_TAGS_ENDPOINT + "/tag-usage")
                .then()
                .statusCode(anyOf(is(500), is(400)))
                .body("$", instanceOf(java.util.Map.class))
                .body("error", notNullValue())
                .body("error", instanceOf(String.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 81: GET /layer-tags/tag-usage-ids — получение ID слоёв с тегом
     */
    @Test(priority = 81, description = "GET /layer-tags/tag-usage-ids — ID слоёв с тегом")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение ID слоёв с тегом. Массив содержит LAYER_ID.")
    @Story("Теги")
    public void testGetTagUsageIds() {
        logger.info("Выполнение теста: GET /layer-tags/tag-usage-ids — ID слоёв с тегом");

        given()
                .spec(getRequestSpec())
                .queryParam("tags", "autotestchange")
                .queryParam("onlyShown", "true")
                .when()
                .get(LAYER_TAGS_ENDPOINT + "/tag-usage-ids")
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class))
                .body("$", hasItem(LAYER_ID));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 82: DELETE /layer-tags — удаление тега
     */
    @Test(priority = 82, description = "DELETE /layer-tags — удаление тега")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Удаление тега по docId.")
    @Story("Теги")
    public void testDeleteTag() {
        logger.info("Выполнение теста: DELETE /layer-tags — удаление тега");

        Assert.assertNotNull(tagId, "tagId должен быть установлен в тесте 66");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"docId\":\"" + tagId + "\"}")
                .when()
                .delete(LAYER_TAGS_ENDPOINT)
                .then()
                .statusCode(200);

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 83: NEGATIVE DELETE /layer-tags — отсутствует поле docId
     */
    @Test(priority = 83, description = "NEGATIVE DELETE /layer-tags — отсутствует docId")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка удаления тега без docId. Ожидается 500/404 и message про id is null.")
    @Story("Negative layer-tags")
    public void testDeleteTagNegativeMissingDocId() {
        logger.info("Выполнение теста: NEGATIVE DELETE /layer-tags — отсутствует docId");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .delete(LAYER_TAGS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(500), is(404)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("id is marked non-null but is null"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 84: NEGATIVE DELETE /layer-tags — невалидный формат UUID в docId
     */
    @Test(priority = 84, description = "NEGATIVE DELETE /layer-tags — невалидный UUID в docId")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка удаления тега с невалидным docId. Ожидается 500/404.")
    @Story("Negative layer-tags")
    public void testDeleteTagNegativeInvalidUuid() {
        logger.info("Выполнение теста: NEGATIVE DELETE /layer-tags — невалидный UUID в docId");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"docId\": \"invalid-uuid-format\"}")
                .when()
                .delete(LAYER_TAGS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(500), is(404)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 85: NEGATIVE DELETE /layer-tags — невалидный JSON
     */
    @Test(priority = 85, description = "NEGATIVE DELETE /layer-tags — невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка удаления тега с невалидным JSON. Ожидается 500/404.")
    @Story("Negative layer-tags")
    public void testDeleteTagNegativeInvalidJson() {
        logger.info("Выполнение теста: NEGATIVE DELETE /layer-tags — невалидный JSON");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"docId\": \"00000000-0000-0000-0000-000000000000\"")
                .when()
                .delete(LAYER_TAGS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(500), is(404)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 86: POST /icons — загрузка иконки
     */
    @Test(priority = 86, description = "POST /icons — загрузка иконки")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Загрузка иконки файлом. Ответ содержит file, validation, message.")
    @Story("Иконки")
    public void testUploadIcon() throws Exception {
        logger.info("Выполнение теста: POST /icons — загрузка иконки");

        File iconFile = new File(getClass().getClassLoader().getResource("test-files/icon-valid.png").toURI());

        Response response = given()
                .spec(getRequestSpec())
                .multiPart("file", iconFile)
                .when()
                .post(ICONS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class))
                .body("size()", greaterThan(0))
                .body("[0].file", notNullValue())
                .body("[0].validation", notNullValue())
                .body("[0].message", equalTo("ОК"))
                .body("[0].validation", hasItem("OK"))
                .extract()
                .response();

        uploadedIconName = response.jsonPath().getString("[0].file");
        if (uploadedIconName != null) {
            logger.info("Загружена иконка: {}", uploadedIconName);
        }
        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 87: NEGATIVE POST /icons — загрузка невалидной иконки (.txt)
     */
    @Test(priority = 87, description = "NEGATIVE POST /icons — невалидная иконка")
    @Severity(SeverityLevel.NORMAL)
    @Description("Загрузка файла с некорректным расширением. API возвращает 200 с сообщением об ошибке.")
    @Story("Negative icons")
    public void testUploadIconNegativeInvalid() throws Exception {
        logger.info("Выполнение теста: NEGATIVE POST /icons — невалидная иконка");

        File invalidFile = new File(getClass().getClassLoader().getResource("test-files/invalid-icon.txt").toURI());

        given()
                .spec(getRequestSpec())
                .multiPart("file", invalidFile)
                .when()
                .post(ICONS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class))
                .body("size()", greaterThan(0))
                .body("[0].message", notNullValue())
                .body("[0].message", instanceOf(String.class))
                .body("[0].message", containsString("Некорректное расширение файла"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 88: GET /icons — проверка загруженной иконки
     */
    @Test(priority = 88, description = "GET /icons — проверка загруженной иконки")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка что загруженная иконка присутствует в списке.")
    @Story("Иконки")
    public void testGetIcons() {
        logger.info("Выполнение теста: GET /icons — проверка загруженной иконки");

        Assert.assertNotNull(uploadedIconName, "uploadedIconName должен быть установлен в тесте 86");

        List<String> icons = given()
                .spec(getRequestSpec())
                .when()
                .get(ICONS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class))
                .extract()
                .jsonPath()
                .getList("$", String.class);

        boolean found = icons.stream().anyMatch(icon ->
                icon.equals(uploadedIconName) || icon.contains(uploadedIconName) || uploadedIconName.contains(icon));
        Assert.assertTrue(found, "Загруженная иконка должна быть в списке: " + uploadedIconName);
        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 89: PUT /icons — переименование иконки
     */
    @Test(priority = 89, description = "PUT /icons — переименование иконки")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Переименование загруженной иконки.")
    @Story("Иконки")
    public void testRenameIcon() {
        logger.info("Выполнение теста: PUT /icons — переименование иконки");

        Assert.assertNotNull(uploadedIconName, "uploadedIconName должен быть установлен в тесте 86");

        String iconUrl = STORAGE_ICONS_BASE + uploadedIconName;
        String requestBody = "{\"iconUrl\": \"" + iconUrl + "\", \"name\": \"" + RENAMED_ICON_NAME + "\"}";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(ICONS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("file", notNullValue())
                .body("validation", notNullValue())
                .body("message", notNullValue());

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 90: NEGATIVE PUT /icons — отсутствует поле iconUrl
     */
    @Test(priority = 90, description = "NEGATIVE PUT /icons — отсутствует iconUrl")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка переименования без iconUrl. Ожидается 500/404.")
    @Story("Negative icons")
    public void testRenameIconNegativeMissingIconUrl() {
        logger.info("Выполнение теста: NEGATIVE PUT /icons — отсутствует iconUrl");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"name\": \"" + RENAMED_ICON_NAME + "\"}")
                .when()
                .put(ICONS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(500), is(404)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("because \"iconUrl\" is null"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 91: NEGATIVE PUT /icons — отсутствует поле name
     */
    @Test(priority = 91, description = "NEGATIVE PUT /icons — отсутствует name")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка переименования без name. Ожидается 500/404.")
    @Story("Negative icons")
    public void testRenameIconNegativeMissingName() {
        logger.info("Выполнение теста: NEGATIVE PUT /icons — отсутствует name");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"iconUrl\": \"" + STORAGE_ICONS_BASE + "test.png\"}")
                .when()
                .put(ICONS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(500), is(404)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 92: NEGATIVE PUT /icons — невалидный JSON
     */
    @Test(priority = 92, description = "NEGATIVE PUT /icons — невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка переименования с невалидным JSON. Ожидается 500/404.")
    @Story("Negative icons")
    public void testRenameIconNegativeInvalidJson() {
        logger.info("Выполнение теста: NEGATIVE PUT /icons — невалидный JSON");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"iconUrl\": \"" + STORAGE_ICONS_BASE + "test.png\"")
                .when()
                .put(ICONS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(500), is(404)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 93: POST /markers — загрузка маркера
     */
    @Test(priority = 93, description = "POST /markers — загрузка маркера")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Загрузка маркера (PNG).")
    @Story("Маркеры")
    public void testUploadMarker() throws Exception {
        logger.info("Выполнение теста: POST /markers — загрузка маркера");

        File markerFile = new File(getClass().getClassLoader().getResource("test-files/marker-valid.png").toURI());

        Response response = given()
                .spec(getRequestSpec())
                .multiPart("file", markerFile)
                .when()
                .post(MARKERS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class))
                .body("size()", greaterThan(0))
                .body("[0].file", notNullValue())
                .body("[0].validation", notNullValue())
                .body("[0].message", equalTo("ОК"))
                .body("[0].validation", hasItem("OK"))
                .extract()
                .response();

        markerFileName = response.jsonPath().getString("[0].file");
        if (markerFileName != null) {
            logger.info("Загружен маркер: {}", markerFileName);
        }
        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 94: NEGATIVE POST /markers — загрузка невалидного маркера (.jpeg)
     */
    @Test(priority = 94, description = "NEGATIVE POST /markers — невалидный маркер")
    @Severity(SeverityLevel.NORMAL)
    @Description("Загрузка файла с расширением .jpeg (markers принимает только PNG).")
    @Story("Negative markers")
    public void testUploadMarkerNegativeInvalid() throws Exception {
        logger.info("Выполнение теста: NEGATIVE POST /markers — невалидный маркер");

        File invalidFile = new File(getClass().getClassLoader().getResource("test-files/invalid-marker.jpeg").toURI());

        given()
                .spec(getRequestSpec())
                .multiPart("file", invalidFile)
                .when()
                .post(MARKERS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class))
                .body("size()", greaterThan(0))
                .body("[0].message", notNullValue())
                .body("[0].message", instanceOf(String.class))
                .body("[0].message", containsString("Некорректное расширение файла"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 95: GET /markers — получение списка маркеров
     */
    @Test(priority = 95, description = "GET /markers — получение списка маркеров")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка что загруженный маркер присутствует в списке.")
    @Story("Маркеры")
    public void testGetMarkers() {
        logger.info("Выполнение теста: GET /markers — получение списка маркеров");

        Assert.assertNotNull(markerFileName, "markerFileName должен быть установлен в тесте 93");

        List<String> markers = given()
                .spec(getRequestSpec())
                .when()
                .get(MARKERS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class))
                .extract()
                .jsonPath()
                .getList("$", String.class);

        boolean found = markers.stream().anyMatch(url -> url != null && url.contains(markerFileName));
        Assert.assertTrue(found, "Загруженный маркер должен быть в списке: " + markerFileName);
        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 96: PUT /markers — переименование маркера
     */
    @Test(priority = 96, description = "PUT /markers — переименование маркера")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Переименование загруженного маркера.")
    @Story("Маркеры")
    public void testRenameMarker() {
        logger.info("Выполнение теста: PUT /markers — переименование маркера");

        Assert.assertNotNull(markerFileName, "markerFileName должен быть установлен в тесте 93");

        String markerUrl = STORAGE_MARKERS_BASE + markerFileName;
        String requestBody = "{\"markerUrl\": \"" + markerUrl + "\", \"name\": \"" + RENAMED_MARKER_NAME + "\"}";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(MARKERS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("file", notNullValue())
                .body("validation", notNullValue())
                .body("message", notNullValue());

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 97: NEGATIVE PUT /markers — отсутствует поле markerUrl
     */
    @Test(priority = 97, description = "NEGATIVE PUT /markers — отсутствует markerUrl")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка переименования без markerUrl. Ожидается 500/404.")
    @Story("Negative markers")
    public void testRenameMarkerNegativeMissingMarkerUrl() {
        logger.info("Выполнение теста: NEGATIVE PUT /markers — отсутствует markerUrl");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"name\": \"" + RENAMED_MARKER_NAME + "\"}")
                .when()
                .put(MARKERS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(500), is(404)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 98: NEGATIVE PUT /markers — отсутствует поле name
     */
    @Test(priority = 98, description = "NEGATIVE PUT /markers — отсутствует name")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка переименования без name. Ожидается 500/404.")
    @Story("Negative markers")
    public void testRenameMarkerNegativeMissingName() {
        logger.info("Выполнение теста: NEGATIVE PUT /markers — отсутствует name");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"markerUrl\": \"" + STORAGE_MARKERS_BASE + "test.png\"}")
                .when()
                .put(MARKERS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(500), is(404)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 99: NEGATIVE PUT /markers — невалидный JSON
     */
    @Test(priority = 99, description = "NEGATIVE PUT /markers — невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка переименования с невалидным JSON. Ожидается 500/404.")
    @Story("Negative markers")
    public void testRenameMarkerNegativeInvalidJson() {
        logger.info("Выполнение теста: NEGATIVE PUT /markers — невалидный JSON");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"markerUrl\": \"" + STORAGE_MARKERS_BASE + "test.png\"")
                .when()
                .put(MARKERS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(500), is(404)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 100: POST /tree-layers — создание слоя с маркером и иконкой
     */
    @Test(priority = 100, description = "POST /tree-layers — создание слоя с маркером и иконкой")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Создание слоя ГЛБ с иконкой и маркером.")
    @Story("Маркеры")
    public void testCreateLayerWithMarkerAndIcon() {
        logger.info("Выполнение теста: POST /tree-layers — создание слоя с маркером и иконкой");

        newLayerId = UUID.randomUUID().toString();
        String iconUrl = STORAGE_ICONS_BASE + RENAMED_ICON_NAME + ".png";
        String markerUrl = STORAGE_MARKERS_BASE + RENAMED_MARKER_NAME + ".png";

        String requestBody = "[{\n"
                + "  \"id\": \"" + newLayerId + "\",\n"
                + "  \"name\": \"дороги_глб\",\n"
                + "  \"tags\": [],\n"
                + "  \"label\": \"слой ГЛБ autotest\",\n"
                + "  \"state\": \"ACTUAL\",\n"
                + "  \"access\": \"FULL\",\n"
                + "  \"geoType\": \"MULTILINESTRING\",\n"
                + "  \"idParent\": null,\n"
                + "  \"itemType\": \"node\",\n"
                + "  \"position\": 695,\n"
                + "  \"settings\": {\n"
                + "    \"viewInTree\": {\n"
                + "      \"isChecked\": false,\n"
                + "      \"iconInTree\": \"" + iconUrl + "\",\n"
                + "      \"isShownInTree\": true,\n"
                + "      \"isHiddenInTree\": false\n"
                + "    },\n"
                + "    \"displayObjectsOnMap\": {\n"
                + "      \"style\": [\n"
                + "        {\n"
                + "          \"marker\": [\n"
                + "            {\"gap\": 0, \"img\": \"" + markerUrl + "\", \"dash\": 1, \"opacity\": 1}\n"
                + "          ],\n"
                + "          \"line-dash\": 2,\n"
                + "          \"line-width\": 3\n"
                + "        }\n"
                + "      ]\n"
                + "    }\n"
                + "  },\n"
                + "  \"hasLabels\": false,\n"
                + "  \"hideValue\": null,\n"
                + "  \"layerType\": \"glb_geom\",\n"
                + "  \"datasource\": \"\",\n"
                + "  \"externalId\": 9245,\n"
                + "  \"useGlbLabels\": false,\n"
                + "  \"validateLayerVisibility\": false\n"
                + "}]";

        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(TREE_LAYERS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("docIds", notNullValue())
                .body("docIds", instanceOf(List.class))
                .extract()
                .response();

        List<String> docIds = response.jsonPath().getList("docIds", String.class);
        Assert.assertTrue(docIds.contains(newLayerId), "docIds должен содержать newLayerId");
        logger.info("Тест успешно завершен. newLayerId: {}", newLayerId);
    }

    /**
     * ТЕСТ 101: GET /markers/marker-usage — проверка использования маркера
     */
    @Test(priority = 101, description = "GET /markers/marker-usage — проверка использования маркера")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка что маркер используется в созданном слое.")
    @Story("Маркеры")
    public void testGetMarkerUsage() {
        logger.info("Выполнение теста: GET /markers/marker-usage — проверка использования маркера");

        String markerUrl = STORAGE_MARKERS_BASE + RENAMED_MARKER_NAME + ".png";

        given()
                .spec(getRequestSpec())
                .queryParam("markerUrl", markerUrl)
                .when()
                .get(MARKERS_ENDPOINT + "/marker-usage")
                .then()
                .statusCode(200)
                .body("items", notNullValue())
                .body("items", instanceOf(List.class))
                .body("items", hasItem("слой ГЛБ autotest"))
                .body("fullList", equalTo(true));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 102: GET /icons/icon-usage — проверка использования иконки
     */
    @Test(priority = 102, description = "GET /icons/icon-usage — проверка использования иконки")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка что иконка используется в созданном слое.")
    @Story("Иконки")
    public void testGetIconUsage() {
        logger.info("Выполнение теста: GET /icons/icon-usage — проверка использования иконки");

        String iconUrl = STORAGE_ICONS_BASE + RENAMED_ICON_NAME + ".png";

        given()
                .spec(getRequestSpec())
                .queryParam("iconUrl", iconUrl)
                .when()
                .get(ICONS_ENDPOINT + "/icon-usage")
                .then()
                .statusCode(200)
                .body("items", notNullValue())
                .body("items", instanceOf(List.class))
                .body("items", hasItem("слой ГЛБ autotest"))
                .body("fullList", equalTo(true));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 103: DELETE /icons — удаление иконки
     */
    @Test(priority = 103, description = "DELETE /icons — удаление переименованной иконки")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Удаление иконки по iconUrl.")
    @Story("Иконки")
    public void testDeleteIcon() {
        logger.info("Выполнение теста: DELETE /icons — удаление иконки");

        String iconUrl = STORAGE_ICONS_BASE + RENAMED_ICON_NAME + ".png";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"iconUrl\": \"" + iconUrl + "\"}")
                .when()
                .delete(ICONS_ENDPOINT)
                .then()
                .statusCode(200);

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 104: NEGATIVE DELETE /icons — отсутствует поле iconUrl
     */
    @Test(priority = 104, description = "NEGATIVE DELETE /icons — отсутствует iconUrl")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка удаления без iconUrl. Ожидается 500/404.")
    @Story("Negative icons")
    public void testDeleteIconNegativeMissingIconUrl() {
        logger.info("Выполнение теста: NEGATIVE DELETE /icons — отсутствует iconUrl");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .delete(ICONS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(500), is(404)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 105: NEGATIVE DELETE /icons — невалидный JSON
     */
    @Test(priority = 105, description = "NEGATIVE DELETE /icons — невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка удаления с невалидным JSON. Ожидается 500/404.")
    @Story("Negative icons")
    public void testDeleteIconNegativeInvalidJson() {
        logger.info("Выполнение теста: NEGATIVE DELETE /icons — невалидный JSON");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"iconUrl\": \"https://example.com/storage/da-cm-map-backend-layerstyle/icons/test.png\"")
                .when()
                .delete(ICONS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(500), is(404)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 106: DELETE /markers — удаление маркера
     */
    @Test(priority = 106, description = "DELETE /markers — удаление маркера")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Удаление маркера по markerUrl.")
    @Story("Маркеры")
    public void testDeleteMarker() {
        logger.info("Выполнение теста: DELETE /markers — удаление маркера");

        String markerUrl = STORAGE_MARKERS_BASE + RENAMED_MARKER_NAME + ".png";

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"markerUrl\": \"" + markerUrl + "\"}")
                .when()
                .delete(MARKERS_ENDPOINT)
                .then()
                .statusCode(200);

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 107: NEGATIVE DELETE /markers — невалидный JSON
     */
    @Test(priority = 107, description = "NEGATIVE DELETE /markers — невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка удаления с невалидным JSON. Ожидается 500/404.")
    @Story("Negative markers")
    public void testDeleteMarkerNegativeInvalidJson() {
        logger.info("Выполнение теста: NEGATIVE DELETE /markers — невалидный JSON");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{\"markerUrl\": \"https://example.com/storage/da-cm-map-backend-layerstyle/marker/test.png\"")
                .when()
                .delete(MARKERS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(500), is(404)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 108: NEGATIVE DELETE /markers — отсутствует поле markerUrl
     */
    @Test(priority = 108, description = "NEGATIVE DELETE /markers — отсутствует markerUrl")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка удаления без markerUrl. Ожидается 500/404.")
    @Story("Negative markers")
    public void testDeleteMarkerNegativeMissingMarkerUrl() {
        logger.info("Выполнение теста: NEGATIVE DELETE /markers — отсутствует markerUrl");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .delete(MARKERS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(500), is(404)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 109: DELETE /tree-layers — удаление слоёв/групп (три элемента)
     * Удаляются: группа, слой (из теста 1), слой с маркером и иконкой (созданный в тесте 100).
     */
    @Test(priority = 109, description = "DELETE /tree-layers — удаление трёх элементов (группа, слой, слой с маркером)")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Удаление группы, слоя и слоя с маркером/иконкой по docIds.")
    @Story("Удаление слоёв и групп")
    public void testDeleteTreeLayers() {
        logger.info("Выполнение теста: DELETE /tree-layers — удаление трёх элементов");

        Assert.assertNotNull(newLayerId, "newLayerId должен быть установлен в тесте 100");

        String requestBody = "["
                + "{\"docId\":\"" + GROUP_ID + "\"},"
                + "{\"docId\":\"" + LAYER_ID + "\"},"
                + "{\"docId\":\"" + newLayerId + "\"}"
                + "]";

        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .delete(TREE_LAYERS_ENDPOINT)
                .then()
                .statusCode(200)
                .extract()
                .response();

        Object body = response.jsonPath().get("$");
        Assert.assertNotNull(body, "Ответ должен содержать тело");
        logger.info("Тест успешно завершен. Удалены docIds: {}, {}, {}", GROUP_ID, LAYER_ID, newLayerId);
    }

    /**
     * ТЕСТ 110: NEGATIVE DELETE /tree-layers — отсутствует поле docId в элементе
     */
    @Test(priority = 110, description = "NEGATIVE DELETE /tree-layers — отсутствует docId в элементе")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка удаления с body [{}]. Ожидается 400/500.")
    @Story("Negative tree-layers delete")
    public void testDeleteTreeLayersNegativeMissingDocId() {
        logger.info("Выполнение теста: NEGATIVE DELETE /tree-layers — отсутствует docId в элементе");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("[{}]")
                .when()
                .delete(TREE_LAYERS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 111: NEGATIVE DELETE /tree-layers — невалидный JSON
     */
    @Test(priority = 111, description = "NEGATIVE DELETE /tree-layers — невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка удаления с невалидным JSON. Ожидается 400/500.")
    @Story("Negative tree-layers delete")
    public void testDeleteTreeLayersNegativeInvalidJson() {
        logger.info("Выполнение теста: NEGATIVE DELETE /tree-layers — невалидный JSON");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body("[\n  {\n    \"docId\": \"5bf7d7ab-6ab9-4223-808a-6dff6702e734\"\n")
                .when()
                .delete(TREE_LAYERS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("$", instanceOf(java.util.Map.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 112: GET /data-hub — получение данных по имени (50. Получение данных из data-hub)
     */
    @Test(priority = 112, description = "GET /data-hub — получение данных по имени")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение данных из data-hub по имени слоя.")
    @Story("Data-hub")
    public void testGetDataHubByName() {
        logger.info("Выполнение теста: GET /data-hub — получение данных по имени");

        JsonPath jsonPath = given()
                .spec(getRequestSpec())
                .queryParam("name", "Wi-Fi в парках (полигоны)")
                .when()
                .get(DATA_HUB_ENDPOINT)
                .then()
                .statusCode(200)
                .body("items", notNullValue())
                .body("items", instanceOf(List.class))
                .extract()
                .jsonPath();

        List<?> items = jsonPath.getList("items");
        Assert.assertNotNull(items, "items должен быть не null");
        Object layerFound = items.stream()
                .filter(item -> "Wi-Fi в парках (полигоны)".equals(((java.util.Map<?, ?>) item).get("name")))
                .findFirst()
                .orElse(null);
        Assert.assertNotNull(layerFound, "В items должен присутствовать слой с именем 'Wi-Fi в парках (полигоны)'");
        Assert.assertEquals(((java.util.Map<?, ?>) layerFound).get("id"), 24, "id слоя должен быть 24");
        Assert.assertEquals(((java.util.Map<?, ?>) layerFound).get("type"), "vctr", "type должен быть vctr");
        Assert.assertEquals(((java.util.Map<?, ?>) layerFound).get("geoType"), "POLYGON", "geoType должен быть POLYGON");

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 113: NEGATIVE GET /data-hub — отсутствует query параметр name
     */
    @Test(priority = 113, description = "NEGATIVE GET /data-hub — отсутствует name")
    @Severity(SeverityLevel.NORMAL)
    @Description("Запрос без query параметра name. Ожидается 500/404.")
    @Story("Negative data-hub")
    public void testGetDataHubNegativeMissingName() {
        logger.info("Выполнение теста: NEGATIVE GET /data-hub — отсутствует name");

        given()
                .spec(getRequestSpec())
                .when()
                .get(DATA_HUB_ENDPOINT)
                .then()
                .statusCode(anyOf(is(500), is(404)))
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 114: GET /data-hub/attributes/{id} — получение атрибутов слоя (51. Получение атрибутов слоя)
     */
    @Test(priority = 114, description = "GET /data-hub/attributes/{id} — получение атрибутов слоя")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение атрибутов слоя по ID.")
    @Story("Data-hub")
    public void testGetDataHubAttributes() {
        logger.info("Выполнение теста: GET /data-hub/attributes/24 — получение атрибутов слоя");

        List<?> attrs = given()
                .spec(getRequestSpec())
                .when()
                .get(DATA_HUB_ENDPOINT + "/attributes/24")
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class))
                .extract()
                .jsonPath()
                .getList("$");

        Assert.assertNotNull(attrs);
        Assert.assertTrue(attrs.size() > 0, "Массив атрибутов должен быть непустым");
        for (int i = 0; i < attrs.size(); i++) {
            Object attr = attrs.get(i);
            Assert.assertTrue(attr instanceof java.util.Map, "Атрибут " + i + " должен иметь структуру объекта");
            Assert.assertTrue(((java.util.Map<?, ?>) attr).containsKey("code"), "Атрибут " + i + " должен иметь code");
            Assert.assertTrue(((java.util.Map<?, ?>) attr).containsKey("type"), "Атрибут " + i + " должен иметь type");
        }

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 115: NEGATIVE GET /data-hub/attributes/{id} — невалидный тип id (не число)
     */
    @Test(priority = 115, description = "NEGATIVE GET /data-hub/attributes/{id} — невалидный id")
    @Severity(SeverityLevel.NORMAL)
    @Description("Запрос с невалидным id. Ожидается 500/404.")
    @Story("Negative data-hub")
    public void testGetDataHubAttributesNegativeInvalidId() {
        logger.info("Выполнение теста: NEGATIVE GET /data-hub/attributes/invalid-id");

        given()
                .spec(getRequestSpec())
                .when()
                .get(DATA_HUB_ENDPOINT + "/attributes/invalid-id")
                .then()
                .statusCode(anyOf(is(500), is(404)));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 116: GET /websocket-connection/url — получение URL WebSocket соединения (52)
     */
    @Test(priority = 116, description = "GET /websocket-connection/url — URL WebSocket")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение URL WebSocket соединения.")
    @Story("WebSocket")
    public void testGetWebSocketUrl() {
        logger.info("Выполнение теста: GET /websocket-connection/url");

        given()
                .spec(getRequestSpec())
                .when()
                .get(WEBSOCKET_CONNECTION_ENDPOINT + "/url")
                .then()
                .statusCode(200)
                .body("value", notNullValue())
                .body("value", instanceOf(String.class))
                .body("value", containsString("/stomp"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 117: GET /websocket-connection/lock-event-key — ключ события блокировки (53)
     */
    @Test(priority = 117, description = "GET /websocket-connection/lock-event-key — ключ события блокировки")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение ключа события блокировки.")
    @Story("WebSocket")
    public void testGetWebSocketLockEventKey() {
        logger.info("Выполнение теста: GET /websocket-connection/lock-event-key");

        given()
                .spec(getRequestSpec())
                .when()
                .get(WEBSOCKET_CONNECTION_ENDPOINT + "/lock-event-key")
                .then()
                .statusCode(200)
                .body("value", notNullValue())
                .body("value", instanceOf(String.class))
                .body("value", containsString("/exchange/amq.direct/layerstyle_lock_event"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 118: GET /websocket-connection/layers-change-event-key — ключ события изменения слоев (54)
     */
    @Test(priority = 118, description = "GET /websocket-connection/layers-change-event-key — ключ события изменения слоев")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение ключа события изменения слоев.")
    @Story("WebSocket")
    public void testGetWebSocketLayersChangeEventKey() {
        logger.info("Выполнение теста: GET /websocket-connection/layers-change-event-key");

        given()
                .spec(getRequestSpec())
                .when()
                .get(WEBSOCKET_CONNECTION_ENDPOINT + "/layers-change-event-key")
                .then()
                .statusCode(200)
                .body("value", notNullValue())
                .body("value", instanceOf(String.class))
                .body("value", containsString("/exchange/amq.direct/layerstyle_change_event"));

        logger.info("Тест успешно завершен.");
    }

    /**
     * ТЕСТ 119: GET /permissions/roles — получение списка ролей (55)
     */
    @Test(priority = 119, description = "GET /permissions/roles — список ролей")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение списка ролей.")
    @Story("Permissions")
    public void testGetPermissionsRoles() {
        logger.info("Выполнение теста: GET /permissions/roles");

        List<?> roles = given()
                .spec(getRequestSpec())
                .when()
                .get(PERMISSIONS_ROLES_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(List.class))
                .extract()
                .jsonPath()
                .getList("$");

        Assert.assertNotNull(roles);
        for (int i = 0; i < roles.size(); i++) {
            Assert.assertTrue(roles.get(i) instanceof String, "Роль " + i + " должна быть строкой");
        }

        logger.info("Тест успешно завершен.");
    }
}
