package com.dt.tests;

// Импорт базового класса - наследуемся от него
import com.dt.base.BaseTest;
// Импорт класса для работы с конфигурацией (чтение переменных окружения)
import com.dt.base.Config;
// Импорт модели Customer - для работы с данными клиента
import com.dt.model.Customer;
// Импорты моделей Widget - для работы с данными виджетов
import com.dt.model.widget.Widget;
import com.dt.model.widget.WidgetData;
// Импорты моделей MethodConfig - для работы с данными конфигураций методов
import com.dt.model.methodconfig.MethodConfig;
// Импорты моделей CustomerConfig - для работы с данными конфигураций клиентов
import com.dt.model.customerconfig.CustomerConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// Импорты Allure - для создания красивых отчетов о тестах
import io.qameta.allure.*; // Все аннотации Allure (@Epic, @Feature, @Story и т.д.)
// Импорты REST Assured - для работы с HTTP запросами
import io.restassured.http.ContentType; // Тип контента (JSON, XML и т.д.)
import io.restassured.response.Response; // Ответ от сервера
// Импорты TestNG - фреймворк для запуска тестов
import org.testng.annotations.BeforeClass; // Аннотация для метода, который выполняется один раз перед всеми тестами
import org.testng.annotations.Test; // Аннотация для обозначения тестового метода

// Статические импорты - можно использовать методы без указания класса
import static io.restassured.RestAssured.given; // Для создания HTTP запросов
import static org.hamcrest.Matchers.*; // Для проверок (assertions) в ответах

/**
 * КЛАСС С ТЕСТАМИ ДЛЯ API CUSTOMER (КЛИЕНТ)
 * 
 * ЗАЧЕМ НУЖЕН ЭТОТ КЛАСС:
 * - Содержит все тесты для API работы с клиентами (создание, проверки и т.д.)
 * - Наследуется от BaseTest - получает все методы для работы с API
 * - Тесты выполняются в строгом порядке (priority 1, 2, 3...)
 * 
 * КАК РАБОТАЕТ:
 * - TestNG находит методы с аннотацией @Test
 * - Выполняет их в порядке priority (1, 2, 3...)
 * - @BeforeClass выполняется один раз перед всеми тестами
 * 
 * АННОТАЦИИ ALLURE:
 * - @Epic - большая группа тестов (например, "Customer CRUD API Testing")
 * - @Feature - функциональность (например, "Customer Management")
 * - Эти аннотации используются для группировки тестов в отчетах Allure
 */
@Epic("Customer CRUD API Testing") // Группировка в отчетах Allure - большой раздел
@Feature("Customer Management") // Группировка в отчетах Allure - функциональность
public class CustomerApiTest extends BaseTest {
    // extends BaseTest - наследуемся от BaseTest
    // Получаем все методы: setBaseUrl(), setCookie(), getRequestSpec() и т.д.

    /**
     * БАЗОВЫЙ URL API ПО УМОЛЧАНИЮ
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Используется как значение по умолчанию, если не установлена переменная окружения
     * - Может быть переопределен через переменную окружения baseUrl или системную переменную -DbaseUrl
     * - private static final - константа, доступна только в этом классе, не меняется
     * - final - значение нельзя изменить после инициализации
     */
    // Публичный репозиторий: не храним здесь URL внутренних стендов.
    // Для запуска задайте baseUrl через -DbaseUrl или переменную окружения baseUrl.
    private static final String DEFAULT_BASE_URL = "https://example.com";
    
    /**
     * ENDPOINT (ПУТЬ) ДЛЯ РАБОТЫ С КЛИЕНТАМИ
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Путь к API для работы с клиентами
     * - Используется в запросах: BASE_URL + CUSTOMERS_ENDPOINT = полный URL
     */
    private static final String CUSTOMERS_ENDPOINT = "/da-cm-map-backend-manager-intersect-object/customers";
    
    /**
     * ENDPOINT (ПУТЬ) ДЛЯ РАБОТЫ С ВИДЖЕТАМИ
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Путь к API для работы с виджетами
     * - Используется в запросах: BASE_URL + WIDGETS_ENDPOINT = полный URL
     */
    private static final String WIDGETS_ENDPOINT = "/da-cm-map-backend-manager-intersect-object/widgets";
    
    /**
     * ENDPOINT (ПУТЬ) ДЛЯ РАБОТЫ С КОНФИГУРАЦИЯМИ МЕТОДОВ
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Путь к API для работы с конфигурациями методов
     * - Используется в запросах: BASE_URL + METHOD_CONFIGS_ENDPOINT = полный URL
     */
    private static final String METHOD_CONFIGS_ENDPOINT = "/da-cm-map-backend-manager-intersect-object/methodConfigs";
    
    /**
     * ENDPOINT (ПУТЬ) ДЛЯ РАБОТЫ С КОНФИГУРАЦИЯМИ КЛИЕНТОВ
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Путь к API для работы с конфигурациями клиентов
     * - Используется в запросах: BASE_URL + CUSTOMER_CONFIGS_ENDPOINT = полный URL
     */
    private static final String CUSTOMER_CONFIGS_ENDPOINT = "/da-cm-map-backend-manager-intersect-object/customerConfigs";
    
    /**
     * ENDPOINT (ПУТЬ) ДЛЯ РАБОТЫ С LAYER DETAILS
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Путь к API для работы с layerDetails
     * - Используется в запросах: BASE_URL + LAYER_DETAILS_ENDPOINT = полный URL
     */
    private static final String LAYER_DETAILS_ENDPOINT = "/da-cm-map-backend-manager-intersect-object/layerDetails";
    
    /**
     * ID СОЗДАННОГО КЛИЕНТА
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Сохраняем ID клиента после создания в первом тесте
     * - Может использоваться в других тестах (обновление, удаление)
     * - static - общая переменная для всех методов класса
     */
    private static Integer customerId;
    
    /**
     * ID СОЗДАННОГО ВИДЖЕТА
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Сохраняем ID виджета после создания
     * - Может использоваться в других тестах
     */
    private static Integer widgetId;
    
    /**
     * ID СОЗДАННОЙ КОНФИГУРАЦИИ МЕТОДА
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Сохраняем ID конфигурации метода после создания
     * - Может использоваться в других тестах
     */
    private static Integer methodConfigId;
    
    /**
     * ID СОЗДАННОЙ КОНФИГУРАЦИИ КЛИЕНТА
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Сохраняем ID конфигурации клиента после создания
     * - Может использоваться в других тестах
     */
    private static Integer customerConfigId;
    
    /**
     * ИМЯ КЛИЕНТА ДЛЯ ТЕСТОВ
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Используется во всех тестах для создания клиента
     * - final - значение не меняется
     */
    private static final String CUSTOMER_NAME = "autotestCustomer";
    
    /**
     * КОД КЛИЕНТА ДЛЯ ТЕСТОВ
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Используется во всех тестах для создания клиента
     * - Должен быть уникальным (иначе будет ошибка дубликата)
     */
    private static final String CUSTOMER_CODE = "autotest";
    
    /**
     * КОД ВИДЖЕТА ДЛЯ ТЕСТОВ
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Используется в тестах для создания виджетов
     * - Должен быть уникальным (иначе будет ошибка дубликата)
     */
    private static final String WIDGET_CODE = "autotestWidget";
    
    /**
     * ТИП МЕТОДА ДЛЯ ТЕСТОВ
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Используется в тестах для создания конфигураций методов
     * - Значение "OBJECTS_BY_COORDINATE"
     */
    private static final String METHOD_TYPE = "OBJECTS_BY_COORDINATE";
    
    /**
     * ID СЛОЯ ДЛЯ ТЕСТОВ
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Используется в тестах для создания конфигураций клиентов
     * - Значение 24
     */
    private static final Integer LAYER_ID = 24;

    /**
     * МЕТОД ИНИЦИАЛИЗАЦИИ - ВЫПОЛНЯЕТСЯ ОДИН РАЗ ПЕРЕД ВСЕМИ ТЕСТАМИ
     * 
     * @BeforeClass - аннотация TestNG
     * ЗАЧЕМ НУЖНА:
     * - Метод выполняется один раз перед запуском всех тестов класса
     * - Используется для общей настройки (URL, куки, подключения и т.д.)
     * - Если нужно что-то сделать один раз для всех тестов - делаем здесь
     * 
     * КОГДА ВЫЗЫВАЕТСЯ:
     * - TestNG вызывает этот метод автоматически перед первым тестом
     * - Выполняется только один раз, даже если тестов много
     */
    @BeforeClass
    public void setUp() {
        // Устанавливаем базовый URL для всех запросов
        // ЗАЧЕМ: после этого в тестах указываем только путь, базовый URL добавится автоматически
        // Используем Config.getBaseUrl() для чтения из переменных окружения или системных переменных
        // Если переменная не установлена - используется значение по умолчанию
        String baseUrl = Config.getBaseUrl(DEFAULT_BASE_URL);
        setBaseUrl(baseUrl);
        
        // Получаем куку из переменной окружения или системной переменной
        // ЗАЧЕМ: не храним куку в коде (безопасность), передаем при запуске тестов
        String cookieValue = Config.getCookie();
        
        // Если кука найдена - устанавливаем её
        if (cookieValue != null && !cookieValue.isEmpty()) {
            // setCookie() - метод из BaseTest, устанавливает куку в спецификацию запроса
            setCookie("cookie", cookieValue);
            logger.info("Cookie установлена из переменной окружения");
        } else {
            // Если кука не найдена - предупреждаем (тесты могут не работать)
            logger.warn("Cookie не установлена! Установите переменную окружения 'cookie' или системную переменную -Dcookie=value");
        }
        
        logger.info("Инициализация тестового класса. Base URL: {}", baseUrl);
    }

    /**
     * ТЕСТ 1: СОЗДАНИЕ КЛИЕНТА (POSITIVE TEST)
     * 
     * @Test - аннотация TestNG, обозначает что это тестовый метод
     * priority = 1 - порядок выполнения (первый тест)
     * description - описание теста (видно в отчетах)
     * 
     * @Severity - важность теста для Allure отчетов
     * CRITICAL - критический тест (основная функциональность)
     * 
     * @Description - подробное описание теста для Allure
     * 
     * @Story - история/сценарий для группировки в Allure
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API позволяет создать нового клиента
     * - Проверяет что ответ содержит ID созданного клиента
     * - Сохраняет ID для возможного использования в других тестах
     */
    @Test(priority = 1, description = "POST create customer")
    @Severity(SeverityLevel.CRITICAL) // Важность: критический
    @Description("Создание нового клиента. Проверка статуса 200, наличия id в ответе и что id является числом.")
    @Story("Create Customer") // Группировка в Allure
    public void testCreateCustomer() {
        logger.info("Выполнение теста: POST create customer");
        
        // Создаем объект Customer с данными для отправки
        // ЗАЧЕМ: REST Assured автоматически преобразует объект в JSON
        Customer customer = new Customer(CUSTOMER_NAME, CUSTOMER_CODE);
        
        // Создаем и отправляем HTTP POST запрос
        Response response = given() // Начинаем создание запроса
                .spec(getRequestSpec()) // Применяем спецификацию (куки, фильтры и т.д.)
                .contentType(ContentType.JSON) // Указываем что отправляем JSON
                .body(customer) // Тело запроса (объект Customer -> JSON автоматически)
                .when() // Когда готовы - выполняем запрос
                .post(CUSTOMERS_ENDPOINT) // POST запрос на endpoint /customers
                .then() // Начинаем проверки ответа
                .statusCode(200) // Проверяем что статус код = 200 (успех)
                .body("$", instanceOf(java.util.Map.class)) // Проверяем что ответ - объект (Map в Java)
                .body("id", notNullValue()) // Проверяем что поле "id" существует и не null
                .body("id", instanceOf(Integer.class)) // Проверяем что "id" - число (Integer)
                .extract() // Извлекаем ответ для дальнейшей работы
                .response(); // Получаем объект Response
        
        // Сохраняем ID созданного клиента
        // ЗАЧЕМ: может понадобиться в других тестах (обновление, удаление)
        // response.jsonPath() - доступ к данным JSON ответа
        // .getInt("id") - получаем значение поля "id" как число
        customerId = response.jsonPath().getInt("id");
        logger.info("Customer создан успешно. ID: {}", customerId);
    }

    /**
     * ТЕСТ 2: ПОПЫТКА СОЗДАТЬ ДУБЛИКАТ КЛИЕНТА (NEGATIVE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API не позволяет создать двух клиентов с одинаковым кодом
     * - Проверяет что возвращается правильная ошибка (500 и сообщение)
     * - Это негативный тест - проверяем обработку ошибок
     */
    @Test(priority = 2, description = "NEGATIVE POST double_customer")
    @Severity(SeverityLevel.NORMAL) // Важность: обычный (не критический)
    @Description("Попытка создать дубликат клиента. Проверка статуса 500 и наличия сообщения об ошибке дубликата.")
    @Story("Negative Tests - Duplicate Customer")
    public void testCreateDuplicateCustomer() {
        logger.info("Выполнение теста: NEGATIVE POST double_customer");
        
        // Создаем клиента с теми же данными что и в первом тесте
        // ЗАЧЕМ: код должен быть уникальным, поэтому должна быть ошибка
        Customer customer = new Customer(CUSTOMER_NAME, CUSTOMER_CODE);
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(customer)
                .when()
                .post(CUSTOMERS_ENDPOINT)
                .then()
                .statusCode(500) // Ожидаем ошибку 500 (вместо 200)
                .body("$", instanceOf(java.util.Map.class)) // Ответ - объект
                .body("message", notNullValue()) // Поле "message" существует
                .body("message", instanceOf(String.class)) // "message" - строка
                .body("message", containsString("ERROR: duplicate key value violates unique constraint")) // Сообщение содержит текст об ошибке дубликата
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 3: СОЗДАНИЕ КЛИЕНТА БЕЗ ПОЛЯ NAME (NEGATIVE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию API - поле "name" обязательное
     * - Проверяет что API возвращает ошибку при отсутствии обязательного поля
     */
    @Test(priority = 3, description = "NEGATIVE POST create customer - отсутствует поле name")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка создать клиента без поля name. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Missing Name Field")
    public void testCreateCustomerWithoutName() {
        logger.info("Выполнение теста: NEGATIVE POST create customer - отсутствует поле name");
        
        // Создаем JSON строку без поля "name"
        // ЗАЧЕМ: проверяем что API требует обязательное поле
        // Используем строку, а не объект Customer, потому что в объекте поле name есть
        // ПРИМЕЧАНИЕ: можно писать JSON в одну строку или с переносами (\n) - для API это не важно
        // \n - символ новой строки, используется только для читаемости кода
        String requestBody = "{\"code\": \"autotest\"}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody) // Отправляем JSON строку напрямую
                .when()
                .post(CUSTOMERS_ENDPOINT)
                .then()
                .statusCode(500) // Ожидаем ошибку
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue()) // Должно быть сообщение об ошибке
                .body("message", instanceOf(String.class))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 4: СОЗДАНИЕ КЛИЕНТА БЕЗ ПОЛЯ CODE (NEGATIVE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что поле "code" обязательное
     * - Проверяет валидацию API
     */
    @Test(priority = 4, description = "NEGATIVE POST create customer - отсутствует поле code")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка создать клиента без поля code. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Missing Code Field")
    public void testCreateCustomerWithoutCode() {
        logger.info("Выполнение теста: NEGATIVE POST create customer - отсутствует поле code");
        
        // JSON строка без поля "code"
        // Можно писать в одну строку - для API это не важно
        String requestBody = "{\"name\": \"autotestCustomer\"}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(CUSTOMERS_ENDPOINT)
                .then()
                .statusCode(500) // Ожидаем ошибку
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 5: СОЗДАНИЕ КЛИЕНТА С НЕВАЛИДНЫМ JSON (NEGATIVE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет обработку невалидного JSON (неправильный формат)
     * - Проверяет что API корректно обрабатывает ошибки парсинга JSON
     */
    @Test(priority = 5, description = "NEGATIVE POST create customer - невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка создать клиента с невалидным JSON. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid JSON")
    public void testCreateCustomerWithInvalidJson() {
        logger.info("Выполнение теста: NEGATIVE POST create customer - невалидный JSON");
        
        // Невалидный JSON - отсутствует закрывающая скобка }
        // ЗАЧЕМ: проверяем что API обрабатывает ошибки парсинга
        // Можно писать в одну строку - для API это не важно
        String invalidJson = "{\"name\": \"autotestCustomer\", \"code\": \"autotest\"";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(invalidJson) // Отправляем невалидный JSON
                .when()
                .post(CUSTOMERS_ENDPOINT)
                .then()
                .statusCode(500) // Ожидаем ошибку
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue()) // Должно быть сообщение об ошибке
                .body("message", instanceOf(String.class))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 6: СОЗДАНИЕ ВИДЖЕТА (POSITIVE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API позволяет создать новый виджет
     * - Проверяет что ответ содержит ID созданного виджета
     * - Сохраняет widgetId для возможного использования
     */
    @Test(priority = 6, description = "POST /widgets")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Создание нового виджета. Проверка статуса 200 и наличия id в ответе.")
    @Story("Create Widget")
    public void testCreateWidget() {
        logger.info("Выполнение теста: POST /widgets");
        
        // Создаем данные для виджета (jsonData)
        List<WidgetData> jsonData = new ArrayList<>();
        jsonData.add(new WidgetData("01 Планируемый", "Планируемый", "#0B338A", "#DEEDFF"));
        jsonData.add(new WidgetData("02 В проектировании", "В проектировании", "#0B338A", "#DEEDFF"));
        
        // Создаем объект Widget с данными
        Widget widget = new Widget(CUSTOMER_CODE, "string", WIDGET_CODE, "Тестовый блок", jsonData);
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(widget)
                .when()
                .post(WIDGETS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("id", notNullValue())
                .body("id", instanceOf(Integer.class))
                .extract()
                .response();
        
        // Сохраняем ID созданного виджета
        widgetId = response.jsonPath().getInt("id");
        logger.info("Widget создан успешно. ID: {}", widgetId);
    }

    /**
     * ТЕСТ 7: ПОПЫТКА СОЗДАТЬ ДУБЛИКАТ ВИДЖЕТА (NEGATIVE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API не позволяет создать два виджета с одинаковым widgetCode
     * - Проверяет что возвращается правильная ошибка (500 и сообщение)
     */
    @Test(priority = 7, description = "NEGATIVE POST /double_widgets")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка создать дубликат виджета. Проверка статуса 500 и наличия сообщения об ошибке дубликата.")
    @Story("Negative Tests - Duplicate Widget")
    public void testCreateDuplicateWidget() {
        logger.info("Выполнение теста: NEGATIVE POST /double_widgets");
        
        // Создаем виджет с теми же данными что и в предыдущем тесте
        List<WidgetData> jsonData = new ArrayList<>();
        jsonData.add(new WidgetData("01 Планируемый", "Планируемый", "#0B338A", "#DEEDFF"));
        jsonData.add(new WidgetData("02 В проектировании", "В проектировании", "#0B338A", "#DEEDFF"));
        
        Widget widget = new Widget(CUSTOMER_CODE, "string", WIDGET_CODE, "Тестовый блок", jsonData);
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(widget)
                .when()
                .post(WIDGETS_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("ERROR: duplicate key value violates unique constraint"))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 8: СОЗДАНИЕ ВИДЖЕТА БЕЗ ПОЛЯ customerCode (NEGATIVE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию API - поле "customerCode" обязательное
     * - Проверяет что API возвращает ошибку 404 при отсутствии обязательного поля
     */
    @Test(priority = 8, description = "NEGATIVE POST /widgets - отсутствует поле customerCode")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка создать виджет без поля customerCode. Проверка статуса 404 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Missing CustomerCode Field")
    public void testCreateWidgetWithoutCustomerCode() {
        logger.info("Выполнение теста: NEGATIVE POST /widgets - отсутствует поле customerCode");
        
        // JSON строка без поля "customerCode"
        String requestBody = "{\"type\": \"string\", \"widgetCode\": \"autotestWidget\", \"name\": \"Тестовый блок\", \"jsonData\": []}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(WIDGETS_ENDPOINT)
                .then()
                .statusCode(404)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 9: СОЗДАНИЕ ВИДЖЕТА БЕЗ ПОЛЯ name (NEGATIVE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что поле "name" обязательное
     * - Проверяет валидацию API
     */
    @Test(priority = 9, description = "NEGATIVE POST /widgets - отсутствует поле name")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка создать виджет без поля name. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Missing Name Field")
    public void testCreateWidgetWithoutName() {
        logger.info("Выполнение теста: NEGATIVE POST /widgets - отсутствует поле name");
        
        // JSON строка без поля "name"
        String requestBody = "{\"customerCode\": \"autotest\", \"type\": \"string\", \"widgetCode\": \"autotestWidget\", \"jsonData\": []}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(WIDGETS_ENDPOINT)
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
     * ТЕСТ 10: СОЗДАНИЕ ВИДЖЕТА С НЕСУЩЕСТВУЮЩИМ customerCode (NEGATIVE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API не позволяет создать виджет для несуществующего клиента
     * - Проверяет что возвращается ошибка 404 или 400
     */
    @Test(priority = 10, description = "NEGATIVE POST /widgets - несуществующий customerCode")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка создать виджет с несуществующим customerCode. Проверка статуса 404 или 400 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Non-existent CustomerCode")
    public void testCreateWidgetWithNonExistentCustomerCode() {
        logger.info("Выполнение теста: NEGATIVE POST /widgets - несуществующий customerCode");
        
        // JSON строка с несуществующим customerCode
        String requestBody = "{\"customerCode\": \"nonExistentCustomer123\", \"type\": \"string\", \"widgetCode\": \"testWidget\", \"name\": \"Тестовый блок\", \"jsonData\": []}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(WIDGETS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(404), is(400))) // Ожидаем либо 404, либо 400
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 11: ПОЛУЧЕНИЕ СПИСКА ВИДЖЕТОВ ПО customerCode (GET TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API возвращает список виджетов для клиента
     * - Проверяет что созданный виджет присутствует в списке
     * - Проверяет структуру данных виджета (name, jsonData.length)
     */
    @Test(priority = 11, description = "GET /widgets/{{customerCode}}")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение списка виджетов по customerCode. Проверка статуса 200, наличия созданного виджета и его данных.")
    @Story("Get Widgets")
    public void testGetWidgetsByCustomerCode() {
        logger.info("Выполнение теста: GET /widgets/{{customerCode}}");
        
        // Формируем endpoint с customerCode
        String getEndpoint = WIDGETS_ENDPOINT + "/" + CUSTOMER_CODE;
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .get(getEndpoint) // GET запрос на /widgets/{customerCode}
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class)) // Ответ - массив
                .extract()
                .response();
        
        // Получаем список виджетов
        List<Object> widgets = response.jsonPath().getList("$");
        
        // Проверяем что массив не пустой (должен содержать хотя бы один виджет)
        assert widgets.size() > 0 : "Список виджетов не должен быть пустым";
        
        // Ищем созданный виджет по widgetCode (поле "code" в ответе)
        // В Postman проверка: widgets.find(w => w.code === "autotestWidget")
        boolean widgetFound = false;
        for (int i = 0; i < widgets.size(); i++) {
            String widgetCode = response.jsonPath().getString("[" + i + "].code");
            if (WIDGET_CODE.equals(widgetCode)) {
                widgetFound = true;
                // Проверяем данные виджета
                // В Postman: pm.expect(widget.name).to.eql("Тестовый блок")
                String name = response.jsonPath().getString("[" + i + "].name");
                // В Postman: pm.expect(widget.jsonData.length).to.eql(2)
                List<Object> jsonData = response.jsonPath().getList("[" + i + "].jsonData");
                
                // Проверяем что name = "Тестовый блок"
                assert "Тестовый блок".equals(name) : 
                    "Имя виджета должно быть 'Тестовый блок', получено: " + name;
                // Проверяем что jsonData содержит 2 элемента
                assert jsonData != null && jsonData.size() == 2 : 
                    "jsonData должен содержать 2 элемента, получено: " + (jsonData != null ? jsonData.size() : 0);
                
                logger.info("Найден виджет с кодом: {}, имя: {}, элементов jsonData: {}", 
                    widgetCode, name, jsonData != null ? jsonData.size() : 0);
                break;
            }
        }
        
        // В Postman: pm.expect(widget).to.exist
        assert widgetFound : "Виджет с кодом " + WIDGET_CODE + " не найден в списке";
        
        logger.info("Получено виджетов: {}", widgets.size());
        logger.info("Созданный виджет найден в списке и проверен");
    }

    /**
     * ТЕСТ 12: ОБНОВЛЕНИЕ ВИДЖЕТА (PUT TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API позволяет обновить существующий виджет
     * - Проверяет что ID в ответе совпадает с ID обновляемого виджета
     */
    @Test(priority = 12, description = "PUT /widgets/{{customerCode}}/{{widgetCode}}")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Обновление виджета. Проверка статуса 200 и совпадения ID.")
    @Story("Update Widget")
    public void testUpdateWidget() {
        logger.info("Выполнение теста: PUT /widgets/{{customerCode}}/{{widgetCode}}");
        
        // Проверяем что widgetId был создан
        if (widgetId == null) {
            logger.error("widgetId не установлен! Нельзя обновить виджет.");
            throw new IllegalStateException("widgetId должен быть установлен перед обновлением");
        }
        
        // Формируем endpoint с customerCode и widgetCode
        String updateEndpoint = WIDGETS_ENDPOINT + "/" + CUSTOMER_CODE + "/" + WIDGET_CODE;
        
        // Тело запроса для обновления
        // В PUT запросе jsonData может быть массивом строк, а не объектов
        String requestBody = "{\"type\": \"Tab\", \"name\": \"ТЕСТ\", \"jsonData\": [\"тестовые данные\"]}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(updateEndpoint) // PUT запрос на /widgets/{customerCode}/{widgetCode}
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("id", notNullValue())
                .body("id", equalTo(widgetId)) // ID должен совпадать с обновляемым виджетом
                .extract()
                .response();
        
        logger.info("Widget успешно обновлен. ID: {}", response.jsonPath().getInt("id"));
    }

    /**
     * ТЕСТ 13: ОБНОВЛЕНИЕ ВИДЖЕТА С НЕСУЩЕСТВУЮЩИМ customerCode (NEGATIVE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API не позволяет обновить виджет для несуществующего клиента
     * - Проверяет что возвращается ошибка 404
     */
    @Test(priority = 13, description = "NEGATIVE PUT /widgets/{{customerCode}}/{{widgetCode}} - несуществующий customerCode")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка обновить виджет с несуществующим customerCode. Проверка статуса 404 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Non-existent CustomerCode in PUT")
    public void testUpdateWidgetWithNonExistentCustomerCode() {
        logger.info("Выполнение теста: NEGATIVE PUT /widgets - несуществующий customerCode");
        
        // Формируем endpoint с несуществующим customerCode
        String updateEndpoint = WIDGETS_ENDPOINT + "/nonExistentCustomer123/" + WIDGET_CODE;
        
        String requestBody = "{\"type\": \"Tab\", \"name\": \"ТЕСТ\", \"jsonData\": [\"тестовые данные\"]}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(updateEndpoint)
                .then()
                .statusCode(404)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 14: ОБНОВЛЕНИЕ НЕСУЩЕСТВУЮЩЕГО ВИДЖЕТА (NEGATIVE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API не позволяет обновить несуществующий виджет
     * - Проверяет что возвращается ошибка 404
     */
    @Test(priority = 14, description = "NEGATIVE PUT /widgets/{{customerCode}}/{{widgetCode}} - несуществующий widgetCode")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка обновить несуществующий виджет. Проверка статуса 404 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Non-existent WidgetCode")
    public void testUpdateNonExistentWidget() {
        logger.info("Выполнение теста: NEGATIVE PUT /widgets - несуществующий widgetCode");
        
        // Формируем endpoint с несуществующим widgetCode
        String updateEndpoint = WIDGETS_ENDPOINT + "/" + CUSTOMER_CODE + "/nonExistentWidget123";
        
        String requestBody = "{\"type\": \"Tab\", \"name\": \"ТЕСТ\", \"jsonData\": [\"тестовые данные\"]}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(updateEndpoint)
                .then()
                .statusCode(404)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 15: ОБНОВЛЕНИЕ ВИДЖЕТА С ПУСТЫМ BODY (NEGATIVE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API не позволяет обновить виджет с пустым телом запроса
     * - Проверяет что возвращается ошибка 500
     */
    @Test(priority = 15, description = "NEGATIVE PUT /widgets/{{customerCode}}/{{widgetCode}} - пустой body")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка обновить виджет с пустым body. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Empty Body in PUT")
    public void testUpdateWidgetWithEmptyBody() {
        logger.info("Выполнение теста: NEGATIVE PUT /widgets - пустой body");
        
        // Формируем endpoint
        String updateEndpoint = WIDGETS_ENDPOINT + "/" + CUSTOMER_CODE + "/" + WIDGET_CODE;
        
        // Пустое тело запроса
        String requestBody = "{}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(updateEndpoint)
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
     * ТЕСТ 16: ОБНОВЛЕНИЕ ВИДЖЕТА С НЕВАЛИДНЫМ JSON (NEGATIVE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет обработку невалидного JSON при обновлении
     * - Проверяет что API корректно обрабатывает ошибки парсинга JSON
     */
    @Test(priority = 16, description = "NEGATIVE PUT /widgets/{{customerCode}}/{{widgetCode}} - невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка обновить виджет с невалидным JSON. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Invalid JSON in PUT")
    public void testUpdateWidgetWithInvalidJson() {
        logger.info("Выполнение теста: NEGATIVE PUT /widgets - невалидный JSON");
        
        // Формируем endpoint
        String updateEndpoint = WIDGETS_ENDPOINT + "/" + CUSTOMER_CODE + "/" + WIDGET_CODE;
        
        // Невалидный JSON - отсутствует закрывающая скобка
        String invalidJson = "{\"type\": \"Tab\", \"name\": \"ТЕСТ\"";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .put(updateEndpoint)
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
     * ТЕСТ 17: ПОЛУЧЕНИЕ КОНКРЕТНОГО ВИДЖЕТА (GET TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API возвращает конкретный виджет по customerCode и widgetCode
     * - Проверяет что виджет содержит обновленные данные (после PUT)
     * - Проверяет name = "ТЕСТ" и jsonData[0] = "тестовые данные"
     */
    @Test(priority = 17, description = "GET /widgets/{{customerCode}}/{{widgetCode}}")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение конкретного виджета. Проверка статуса 200 и обновленных данных (name='ТЕСТ', jsonData[0]='тестовые данные').")
    @Story("Get Widget by Code")
    public void testGetWidgetByCode() {
        logger.info("Выполнение теста: GET /widgets/{{customerCode}}/{{widgetCode}}");
        
        // Формируем endpoint с customerCode и widgetCode
        String getEndpoint = WIDGETS_ENDPOINT + "/" + CUSTOMER_CODE + "/" + WIDGET_CODE;
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .get(getEndpoint) // GET запрос на /widgets/{customerCode}/{widgetCode}
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class)) // Ответ - объект
                .body("name", equalTo("ТЕСТ")) // Проверяем что name = "ТЕСТ" (обновленное значение)
                .extract()
                .response();
        
        // Проверяем jsonData[0] = "тестовые данные"
        // В Postman: pm.expect(widget.jsonData[0]).to.eql("тестовые данные")
        List<Object> jsonData = response.jsonPath().getList("jsonData");
        assert jsonData != null && jsonData.size() > 0 : "jsonData не должен быть пустым";
        assert "тестовые данные".equals(jsonData.get(0).toString()) : 
            "jsonData[0] должен быть 'тестовые данные', получено: " + jsonData.get(0);
        
        logger.info("Виджет получен успешно. name: {}, jsonData[0]: {}", 
            response.jsonPath().getString("name"), jsonData.get(0));
    }

    /**
     * ТЕСТ 18: ПОЛУЧЕНИЕ НЕСУЩЕСТВУЮЩЕГО ВИДЖЕТА (NEGATIVE GET TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API возвращает ошибку 404 для несуществующего виджета
     * - Проверяет что сообщение об ошибке содержит нужный текст
     */
    @Test(priority = 18, description = "NEGATIVE GET /widgets/{{customerCode}}/{{widgetCode}}")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить несуществующий виджет. Проверка статуса 404 и сообщения об ошибке.")
    @Story("Negative Tests - Non-existent Widget in GET")
    public void testGetNonExistentWidget() {
        logger.info("Выполнение теста: NEGATIVE GET /widgets - несуществующий widgetCode");
        
        // Формируем endpoint с несуществующим widgetCode ("negative")
        String getEndpoint = WIDGETS_ENDPOINT + "/" + CUSTOMER_CODE + "/negative";
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .get(getEndpoint)
                .then()
                .statusCode(404)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("there is no widget for code negative and")) // Проверяем текст ошибки
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 19: ПОЛУЧЕНИЕ ВИДЖЕТА С НЕСУЩЕСТВУЮЩИМ customerCode (NEGATIVE GET TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API не позволяет получить виджет для несуществующего клиента
     * - Проверяет что возвращается ошибка 404
     */
    @Test(priority = 19, description = "NEGATIVE GET /widgets/{{customerCode}}/{{widgetCode}} - несуществующий customerCode")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить виджет с несуществующим customerCode. Проверка статуса 404 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Non-existent CustomerCode in GET")
    public void testGetWidgetWithNonExistentCustomerCode() {
        logger.info("Выполнение теста: NEGATIVE GET /widgets - несуществующий customerCode");
        
        // Формируем endpoint с несуществующим customerCode
        String getEndpoint = WIDGETS_ENDPOINT + "/nonExistentCustomer123/" + WIDGET_CODE;
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .get(getEndpoint)
                .then()
                .statusCode(404)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 20: УДАЛЕНИЕ ВИДЖЕТА (DELETE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Удаляет созданный виджет
     * - Проверяет что удаление прошло успешно (статус 200)
     * - Выполняется перед удалением клиента
     */
    @Test(priority = 20, description = "DELETE /widgets/{{customerCode}}/{{widgetCode}}")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Удаление виджета. Проверка статуса 200.")
    @Story("Delete Widget")
    public void testDeleteWidget() {
        logger.info("Выполнение теста: DELETE /widgets/{{customerCode}}/{{widgetCode}}");
        
        // Формируем endpoint с customerCode и widgetCode
        String deleteEndpoint = WIDGETS_ENDPOINT + "/" + CUSTOMER_CODE + "/" + WIDGET_CODE;
        
        given()
                .spec(getRequestSpec())
                .when()
                .delete(deleteEndpoint) // DELETE запрос на /widgets/{customerCode}/{widgetCode}
                .then()
                .statusCode(200); // Ожидаем успешное удаление
        
        logger.info("Widget успешно удален. customerCode: {}, widgetCode: {}", CUSTOMER_CODE, WIDGET_CODE);
    }

    /**
     * ТЕСТ 21: ПОВТОРНОЕ УДАЛЕНИЕ ВИДЖЕТА (NEGATIVE DELETE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API не позволяет удалить уже удаленный виджет
     * - Проверяет что возвращается ошибка 404
     * - Проверяет текст сообщения об ошибке
     */
    @Test(priority = 21, description = "NEGATIVE repeated DELETE /widgets/{{customerCode}}/{{widgetCode}}")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка повторно удалить уже удаленный виджет. Проверка статуса 404 и сообщения об ошибке.")
    @Story("Negative Tests - Repeated Delete Widget")
    public void testRepeatedDeleteWidget() {
        logger.info("Выполнение теста: NEGATIVE repeated DELETE /widgets");
        
        // Формируем endpoint (тот же что и в предыдущем тесте)
        String deleteEndpoint = WIDGETS_ENDPOINT + "/" + CUSTOMER_CODE + "/" + WIDGET_CODE;
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .delete(deleteEndpoint) // Пытаемся удалить уже удаленный виджет
                .then()
                .statusCode(404) // Ожидаем ошибку
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("there is no widget for code autotestWidget and customer autotest")) // Проверяем текст ошибки
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 22: УДАЛЕНИЕ ВИДЖЕТА С НЕСУЩЕСТВУЮЩИМ customerCode (NEGATIVE DELETE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API не позволяет удалить виджет для несуществующего клиента
     * - Проверяет что возвращается ошибка 404
     */
    @Test(priority = 22, description = "NEGATIVE DELETE /widgets/{{customerCode}}/{{widgetCode}} - несуществующий customerCode")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка удалить виджет с несуществующим customerCode. Проверка статуса 404 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Non-existent CustomerCode in DELETE")
    public void testDeleteWidgetWithNonExistentCustomerCode() {
        logger.info("Выполнение теста: NEGATIVE DELETE /widgets - несуществующий customerCode");
        
        // Формируем endpoint с несуществующим customerCode
        String deleteEndpoint = WIDGETS_ENDPOINT + "/nonExistentCustomer123/" + WIDGET_CODE;
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .delete(deleteEndpoint)
                .then()
                .statusCode(404)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 23: УДАЛЕНИЕ НЕСУЩЕСТВУЮЩЕГО ВИДЖЕТА (NEGATIVE DELETE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API не позволяет удалить несуществующий виджет
     * - Проверяет что возвращается ошибка 404
     */
    @Test(priority = 23, description = "NEGATIVE DELETE /widgets/{{customerCode}}/{{widgetCode}} - несуществующий widgetCode")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка удалить несуществующий виджет. Проверка статуса 404 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Non-existent WidgetCode in DELETE")
    public void testDeleteNonExistentWidget() {
        logger.info("Выполнение теста: NEGATIVE DELETE /widgets - несуществующий widgetCode");
        
        // Формируем endpoint с несуществующим widgetCode
        String deleteEndpoint = WIDGETS_ENDPOINT + "/" + CUSTOMER_CODE + "/nonExistentWidget123";
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .delete(deleteEndpoint)
                .then()
                .statusCode(404)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 24: СОЗДАНИЕ КОНФИГУРАЦИИ МЕТОДА (POSITIVE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API позволяет создать новую конфигурацию метода
     * - Проверяет что ответ содержит ID созданной конфигурации
     * - Сохраняет methodConfigId для возможного использования
     */
    @Test(priority = 24, description = "POST /methodConfigs")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Создание новой конфигурации метода. Проверка статуса 200, наличия id в ответе и что id не пустой.")
    @Story("Create MethodConfig")
    public void testCreateMethodConfig() {
        logger.info("Выполнение теста: POST /methodConfigs");
        
        // Создаем queryParams (объект с параметрами)
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("radius", 50);
        
        // Создаем объект MethodConfig с данными
        MethodConfig methodConfig = new MethodConfig(
            CUSTOMER_CODE, 
            "OBJECTS_BY_COORDINATE", 
            "autotestName", 
            queryParams
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(methodConfig)
                .when()
                .post(METHOD_CONFIGS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("id", notNullValue())
                .extract()
                .response();
        
        // Сохраняем ID созданной конфигурации
        methodConfigId = response.jsonPath().getInt("id");
        logger.info("MethodConfig создан успешно. ID: {}", methodConfigId);
    }

    /**
     * ТЕСТ 25: ПОПЫТКА СОЗДАТЬ ДУБЛИКАТ КОНФИГУРАЦИИ МЕТОДА (NEGATIVE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API не позволяет создать дубликат конфигурации метода
     * - Проверяет что возвращается правильная ошибка (500 и сообщение)
     */
    @Test(priority = 25, description = "NEGATIVE POST /double_methodConfigs")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка создать дубликат конфигурации метода. Проверка статуса 500 и наличия сообщения об ошибке дубликата.")
    @Story("Negative Tests - Duplicate MethodConfig")
    public void testCreateDuplicateMethodConfig() {
        logger.info("Выполнение теста: NEGATIVE POST /double_methodConfigs");
        
        // Создаем конфигурацию с теми же данными что и в предыдущем тесте
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("radius", 50);
        
        MethodConfig methodConfig = new MethodConfig(
            CUSTOMER_CODE, 
            "OBJECTS_BY_COORDINATE", 
            "autotestName", 
            queryParams
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(methodConfig)
                .when()
                .post(METHOD_CONFIGS_ENDPOINT)
                .then()
                .statusCode(500)
                .extract()
                .response();
        
        // Проверяем что сообщение содержит нужный текст
        // В Postman: pm.expect(pm.response.text()).to.include("OBJECTS_BY_COORDINATE) already exists.")
        String responseText = response.asString();
        assert responseText.contains("OBJECTS_BY_COORDINATE) already exists.") : 
            "Сообщение об ошибке должно содержать 'OBJECTS_BY_COORDINATE) already exists.'";
        
        logger.info("Ожидаемая ошибка получена: {}", responseText);
    }

    /**
     * ТЕСТ 26: СОЗДАНИЕ КОНФИГУРАЦИИ МЕТОДА БЕЗ ПОЛЯ customerCode (NEGATIVE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию API - поле "customerCode" обязательное
     * - Проверяет что API возвращает ошибку 404 при отсутствии обязательного поля
     */
    @Test(priority = 26, description = "NEGATIVE POST /methodConfigs - отсутствует поле customerCode")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка создать конфигурацию метода без поля customerCode. Проверка статуса 404 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Missing CustomerCode Field")
    public void testCreateMethodConfigWithoutCustomerCode() {
        logger.info("Выполнение теста: NEGATIVE POST /methodConfigs - отсутствует поле customerCode");
        
        // JSON строка без поля "customerCode"
        String requestBody = "{\"methodType\": \"OBJECTS_BY_COORDINATE\", \"name\": \"autotestName\", \"queryParams\": {\"radius\": 50}}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(METHOD_CONFIGS_ENDPOINT)
                .then()
                .statusCode(404)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 27: СОЗДАНИЕ КОНФИГУРАЦИИ МЕТОДА БЕЗ ПОЛЯ methodType (NEGATIVE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что поле "methodType" обязательное
     * - Проверяет валидацию API
     */
    @Test(priority = 27, description = "NEGATIVE POST /methodConfigs - отсутствует поле methodType")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка создать конфигурацию метода без поля methodType. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Missing MethodType Field")
    public void testCreateMethodConfigWithoutMethodType() {
        logger.info("Выполнение теста: NEGATIVE POST /methodConfigs - отсутствует поле methodType");
        
        // JSON строка без поля "methodType"
        String requestBody = "{\"customerCode\": \"autotest\", \"name\": \"autotestName\", \"queryParams\": {\"radius\": 50}}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(METHOD_CONFIGS_ENDPOINT)
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
     * ТЕСТ 28: СОЗДАНИЕ КОНФИГУРАЦИИ МЕТОДА БЕЗ ПОЛЯ name (NEGATIVE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что поле "name" обязательное
     * - Проверяет валидацию API
     */
    @Test(priority = 28, description = "NEGATIVE POST /methodConfigs - отсутствует поле name")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка создать конфигурацию метода без поля name. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Missing Name Field")
    public void testCreateMethodConfigWithoutName() {
        logger.info("Выполнение теста: NEGATIVE POST /methodConfigs - отсутствует поле name");
        
        // JSON строка без поля "name"
        String requestBody = "{\"customerCode\": \"autotest\", \"methodType\": \"OBJECTS_BY_COORDINATE\", \"queryParams\": {\"radius\": 50}}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(METHOD_CONFIGS_ENDPOINT)
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
     * ТЕСТ 29: ПОЛУЧЕНИЕ СПИСКА КОНФИГУРАЦИЙ МЕТОДОВ ПО customerCode (GET TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API возвращает список конфигураций методов для клиента
     * - Проверяет что созданная конфигурация присутствует в списке
     * - Проверяет структуру данных (customerCode, name, queryParams.radius)
     */
    @Test(priority = 29, description = "GET /methodConfigs/{customerCode}")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение списка конфигураций методов по customerCode. Проверка статуса 200, наличия созданной конфигурации и её данных.")
    @Story("Get MethodConfigs")
    public void testGetMethodConfigsByCustomerCode() {
        logger.info("Выполнение теста: GET /methodConfigs/{customerCode}");
        
        // Формируем endpoint с customerCode
        String getEndpoint = METHOD_CONFIGS_ENDPOINT + "/" + CUSTOMER_CODE;
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .get(getEndpoint) // GET запрос на /methodConfigs/{customerCode}
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class)) // Ответ - массив
                .extract()
                .response();
        
        // Получаем список конфигураций
        List<Object> methodConfigs = response.jsonPath().getList("$");
        
        // Проверяем что массив не пустой
        assert methodConfigs.size() > 0 : "Список конфигураций не должен быть пустым";
        
        // Получаем первую конфигурацию (индекс 0)
        // В Postman: const jsonData = pm.response.json()[0]
        // Проверяем customerCode (в ответе это customer.code)
        String customerCode = response.jsonPath().getString("[0].customer.code");
        // В Postman: pm.expect(jsonData.customer.code).to.eql("autotest")
        assert CUSTOMER_CODE.equals(customerCode) : 
            "customer.code должен быть '" + CUSTOMER_CODE + "', получено: " + customerCode;
        
        // Проверяем name
        String name = response.jsonPath().getString("[0].name");
        // В Postman: pm.expect(jsonData.name).to.eql("autotestName")
        assert "autotestName".equals(name) : 
            "name должен быть 'autotestName', получено: " + name;
        
        // Проверяем queryParams.radius
        Integer radius = response.jsonPath().getInt("[0].queryParams.radius");
        // В Postman: pm.expect(jsonData.queryParams.radius).to.eql(50)
        assert radius != null && radius == 50 : 
            "queryParams.radius должен быть 50, получено: " + radius;
        
        logger.info("Получено конфигураций: {}", methodConfigs.size());
        logger.info("Созданная конфигурация найдена в списке. customerCode: {}, name: {}, radius: {}", 
            customerCode, name, radius);
    }

    /**
     * ТЕСТ 30: ОБНОВЛЕНИЕ КОНФИГУРАЦИИ МЕТОДА (PUT TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API позволяет обновить существующую конфигурацию метода
     * - Проверяет что ID в ответе совпадает с ID обновляемой конфигурации
     */
    @Test(priority = 30, description = "PUT /methodConfigs/{customerCode}/{methodType}")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Обновление конфигурации метода. Проверка статуса 200 и совпадения ID.")
    @Story("Update MethodConfig")
    public void testUpdateMethodConfig() {
        logger.info("Выполнение теста: PUT /methodConfigs/{customerCode}/{methodType}");
        
        // Проверяем что methodConfigId был создан
        if (methodConfigId == null) {
            logger.error("methodConfigId не установлен! Нельзя обновить конфигурацию.");
            throw new IllegalStateException("methodConfigId должен быть установлен перед обновлением");
        }
        
        // Формируем endpoint с customerCode и methodType
        String updateEndpoint = METHOD_CONFIGS_ENDPOINT + "/" + CUSTOMER_CODE + "/" + METHOD_TYPE;
        
        // Тело запроса для обновления (только queryParams)
        String requestBody = "{\"queryParams\": {\"radius\": 150}}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(updateEndpoint) // PUT запрос на /methodConfigs/{customerCode}/{methodType}
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("id", notNullValue())
                .body("id", equalTo(methodConfigId)) // ID должен совпадать с обновляемой конфигурацией
                .extract()
                .response();
        
        logger.info("MethodConfig успешно обновлен. ID: {}", response.jsonPath().getInt("id"));
    }

    /**
     * ТЕСТ 31: ОБНОВЛЕНИЕ КОНФИГУРАЦИИ С НЕСУЩЕСТВУЮЩИМ customerCode (NEGATIVE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API не позволяет обновить конфигурацию для несуществующего клиента
     * - Проверяет что возвращается ошибка 404
     */
    @Test(priority = 31, description = "NEGATIVE PUT /methodConfigs/{customerCode}/{methodType} - несуществующий customerCode")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка обновить конфигурацию с несуществующим customerCode. Проверка статуса 404 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Non-existent CustomerCode in PUT")
    public void testUpdateMethodConfigWithNonExistentCustomerCode() {
        logger.info("Выполнение теста: NEGATIVE PUT /methodConfigs - несуществующий customerCode");
        
        // Формируем endpoint с несуществующим customerCode
        String updateEndpoint = METHOD_CONFIGS_ENDPOINT + "/nonExistentCustomer123/" + METHOD_TYPE;
        
        String requestBody = "{\"queryParams\": {\"radius\": 150}}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(updateEndpoint)
                .then()
                .statusCode(404)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 32: ОБНОВЛЕНИЕ КОНФИГУРАЦИИ С НЕСУЩЕСТВУЮЩИМ methodType (NEGATIVE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API не позволяет обновить конфигурацию с несуществующим methodType
     * - Проверяет что возвращается ошибка 500
     */
    @Test(priority = 32, description = "NEGATIVE PUT /methodConfigs/{customerCode}/{methodType} - несуществующий methodType")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка обновить конфигурацию с несуществующим methodType. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Non-existent MethodType in PUT")
    public void testUpdateMethodConfigWithNonExistentMethodType() {
        logger.info("Выполнение теста: NEGATIVE PUT /methodConfigs - несуществующий methodType");
        
        // Формируем endpoint с несуществующим methodType
        String updateEndpoint = METHOD_CONFIGS_ENDPOINT + "/" + CUSTOMER_CODE + "/NON_EXISTENT_METHOD";
        
        String requestBody = "{\"queryParams\": {\"radius\": 150}}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(updateEndpoint)
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
     * ТЕСТ 33: ПОЛУЧЕНИЕ КОНКРЕТНОЙ КОНФИГУРАЦИИ МЕТОДА (GET TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API возвращает конкретную конфигурацию по customerCode и methodType
     * - Проверяет что конфигурация содержит обновленные данные (radius = 150)
     * - Проверяет что layers - пустой массив
     */
    @Test(priority = 33, description = "GET /methodConfigs/{customerCode}/{methodType}")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение конкретной конфигурации метода. Проверка статуса 200, обновленных данных (radius=150) и пустого массива layers.")
    @Story("Get MethodConfig by Type")
    public void testGetMethodConfigByType() {
        logger.info("Выполнение теста: GET /methodConfigs/{customerCode}/{methodType}");
        
        // Формируем endpoint с customerCode и methodType
        String getEndpoint = METHOD_CONFIGS_ENDPOINT + "/" + CUSTOMER_CODE + "/" + METHOD_TYPE;
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .get(getEndpoint) // GET запрос на /methodConfigs/{customerCode}/{methodType}
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class)) // Ответ - объект
                .extract()
                .response();
        
        // Проверяем queryParams.radius = 150 (обновленное значение)
        // В Postman: pm.expect(jsonData.queryParams.radius).to.eql(150)
        Integer radius = response.jsonPath().getInt("queryParams.radius");
        assert radius != null && radius == 150 : 
            "queryParams.radius должен быть 150, получено: " + radius;
        
        // Проверяем что layers - пустой массив
        // В Postman: pm.expect(jsonData.layers).to.be.an("array").that.is.empty
        List<Object> layers = response.jsonPath().getList("layers");
        assert layers != null && layers.isEmpty() : 
            "layers должен быть пустым массивом";
        
        logger.info("Конфигурация получена успешно. radius: {}, layers пустой: {}", radius, layers.isEmpty());
    }

    /**
     * ТЕСТ 34: ПОЛУЧЕНИЕ КОНФИГУРАЦИИ С НЕСУЩЕСТВУЮЩИМ methodType (NEGATIVE GET TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API возвращает ошибку 500 для несуществующего methodType
     * - Проверяет что сообщение об ошибке присутствует
     */
    @Test(priority = 34, description = "NEGATIVE GET /methodConfigs/{customerCode}/{methodType} - несуществующий methodType")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить конфигурацию с несуществующим methodType. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Non-existent MethodType in GET")
    public void testGetMethodConfigWithNonExistentMethodType() {
        logger.info("Выполнение теста: NEGATIVE GET /methodConfigs - несуществующий methodType");
        
        // Формируем endpoint с несуществующим methodType
        String getEndpoint = METHOD_CONFIGS_ENDPOINT + "/" + CUSTOMER_CODE + "/NON_EXISTENT_METHOD";
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .get(getEndpoint)
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
     * ТЕСТ 35: УДАЛЕНИЕ КОНФИГУРАЦИИ МЕТОДА (DELETE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Удаляет созданную конфигурацию метода
     * - Проверяет что удаление прошло успешно (статус 200)
     * - Выполняется перед удалением клиента
     */
    @Test(priority = 35, description = "DELETE /methodConfigs/{customerCode}/{methodType}")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Удаление конфигурации метода. Проверка статуса 200.")
    @Story("Delete MethodConfig")
    public void testDeleteMethodConfig() {
        logger.info("Выполнение теста: DELETE /methodConfigs/{customerCode}/{methodType}");
        
        // Формируем endpoint с customerCode и methodType
        String deleteEndpoint = METHOD_CONFIGS_ENDPOINT + "/" + CUSTOMER_CODE + "/" + METHOD_TYPE;
        
        given()
                .spec(getRequestSpec())
                .when()
                .delete(deleteEndpoint) // DELETE запрос на /methodConfigs/{customerCode}/{methodType}
                .then()
                .statusCode(200); // Ожидаем успешное удаление
        
        logger.info("MethodConfig успешно удален. customerCode: {}, methodType: {}", CUSTOMER_CODE, METHOD_TYPE);
    }

    /**
     * ТЕСТ 36: ПОВТОРНОЕ УДАЛЕНИЕ КОНФИГУРАЦИИ МЕТОДА (NEGATIVE DELETE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API не позволяет удалить уже удаленную конфигурацию
     * - Проверяет что возвращается ошибка 404
     */
    @Test(priority = 36, description = "NEGATIVE repeated DELETE /methodConfigs/{customerCode}/{methodType}")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка повторно удалить уже удаленную конфигурацию. Проверка статуса 404 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Repeated Delete MethodConfig")
    public void testRepeatedDeleteMethodConfig() {
        logger.info("Выполнение теста: NEGATIVE repeated DELETE /methodConfigs");
        
        // Формируем endpoint (тот же что и в предыдущем тесте)
        String deleteEndpoint = METHOD_CONFIGS_ENDPOINT + "/" + CUSTOMER_CODE + "/" + METHOD_TYPE;
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .delete(deleteEndpoint) // Пытаемся удалить уже удаленную конфигурацию
                .then()
                .statusCode(404) // Ожидаем ошибку
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 37: УДАЛЕНИЕ КОНФИГУРАЦИИ С НЕСУЩЕСТВУЮЩИМ customerCode (NEGATIVE DELETE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API не позволяет удалить конфигурацию для несуществующего клиента
     * - Проверяет что возвращается ошибка 404
     */
    @Test(priority = 37, description = "NEGATIVE DELETE /methodConfigs/{customerCode}/{methodType} - несуществующий customerCode")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка удалить конфигурацию с несуществующим customerCode. Проверка статуса 404 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Non-existent CustomerCode in DELETE")
    public void testDeleteMethodConfigWithNonExistentCustomerCode() {
        logger.info("Выполнение теста: NEGATIVE DELETE /methodConfigs - несуществующий customerCode");
        
        // Формируем endpoint с несуществующим customerCode
        String deleteEndpoint = METHOD_CONFIGS_ENDPOINT + "/nonExistentCustomer123/" + METHOD_TYPE;
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .delete(deleteEndpoint)
                .then()
                .statusCode(404)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 38: УДАЛЕНИЕ КОНФИГУРАЦИИ С НЕСУЩЕСТВУЮЩИМ methodType (NEGATIVE DELETE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API не позволяет удалить конфигурацию с несуществующим methodType
     * - Проверяет что возвращается ошибка 500
     */
    @Test(priority = 38, description = "NEGATIVE DELETE /methodConfigs/{customerCode}/{methodType} - несуществующий methodType")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка удалить конфигурацию с несуществующим methodType. Проверка статуса 500 и наличия сообщения об ошибке.")
    @Story("Negative Tests - Non-existent MethodType in DELETE")
    public void testDeleteMethodConfigWithNonExistentMethodType() {
        logger.info("Выполнение теста: NEGATIVE DELETE /methodConfigs - несуществующий methodType");
        
        // Формируем endpoint с несуществующим methodType
        String deleteEndpoint = METHOD_CONFIGS_ENDPOINT + "/" + CUSTOMER_CODE + "/nonExistentCustomer123";
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .delete(deleteEndpoint)
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
     * ТЕСТ 39: СОЗДАНИЕ КОНФИГУРАЦИИ КЛИЕНТА (POSITIVE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API позволяет создать новую конфигурацию клиента
     * - Проверяет что ответ содержит ID созданной конфигурации
     * - Сохраняет customerConfigId для возможного использования
     */
    @Test(priority = 39, description = "POST /customerConfigs")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Создание новой конфигурации клиента. Проверка статуса 200, наличия id в ответе и что id является числом.")
    @Story("Create CustomerConfig")
    public void testCreateCustomerConfig() {
        logger.info("Выполнение теста: POST /customerConfigs");
        
        // Создаем список properties
        List<String> properties = new ArrayList<>();
        properties.add("id");
        
        // Создаем объект CustomerConfig с данными
        CustomerConfig customerConfig = new CustomerConfig(
            CUSTOMER_CODE,
            LAYER_ID,
            "тестовый Wi-Fi",
            properties,
            "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            0,
            0,
            0
        );
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(customerConfig)
                .when()
                .post(CUSTOMER_CONFIGS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("id", notNullValue())
                .body("id", instanceOf(Integer.class))
                .extract()
                .response();
        
        // Сохраняем ID созданной конфигурации
        customerConfigId = response.jsonPath().getInt("id");
        logger.info("CustomerConfig создан успешно. ID: {}", customerConfigId);
    }

    /**
     * ТЕСТ 40: СОЗДАНИЕ КОНФИГУРАЦИИ КЛИЕНТА БЕЗ ПОЛЯ customerCode (NEGATIVE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию API - поле "customerCode" обязательное
     * - Проверяет что API возвращает ошибку 404 при отсутствии обязательного поля
     * - Проверяет что сообщение содержит "customer null not found"
     */
    @Test(priority = 40, description = "NEGATIVE POST /customerConfigs - отсутствует поле customerCode")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка создать конфигурацию клиента без поля customerCode. Проверка статуса 404 и сообщения об ошибке.")
    @Story("Negative Tests - Missing CustomerCode Field")
    public void testCreateCustomerConfigWithoutCustomerCode() {
        logger.info("Выполнение теста: NEGATIVE POST /customerConfigs - отсутствует поле customerCode");
        
        // JSON строка без поля "customerCode"
        String requestBody = "{\"layerId\": 24, \"alias\": \"тестовый Wi-Fi\", \"properties\": [\"id\"], \"treeLayerId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"order\": 0, \"lowerOverlapPercent\": 0, \"upperOverlapPercent\": 0}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(CUSTOMER_CONFIGS_ENDPOINT)
                .then()
                .statusCode(404)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("customer null not found")) // Проверяем текст ошибки
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 41: СОЗДАНИЕ КОНФИГУРАЦИИ КЛИЕНТА БЕЗ ПОЛЯ layerId (NEGATIVE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что поле "layerId" обязательное
     * - Проверяет валидацию API
     * - Проверяет что сообщение содержит "null value in column \"layer_id\""
     */
    @Test(priority = 41, description = "NEGATIVE POST /customerConfigs - отсутствует поле layerId")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка создать конфигурацию клиента без поля layerId. Проверка статуса 500 и сообщения об ошибке.")
    @Story("Negative Tests - Missing LayerId Field")
    public void testCreateCustomerConfigWithoutLayerId() {
        logger.info("Выполнение теста: NEGATIVE POST /customerConfigs - отсутствует поле layerId");
        
        // JSON строка без поля "layerId"
        String requestBody = "{\"customerCode\": \"autotest\", \"alias\": \"тестовый Wi-Fi\", \"properties\": [\"id\"], \"treeLayerId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"order\": 0, \"lowerOverlapPercent\": 0, \"upperOverlapPercent\": 0}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(CUSTOMER_CONFIGS_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("null value in column \"layer_id\"")) // Проверяем текст ошибки
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 42: СОЗДАНИЕ КОНФИГУРАЦИИ КЛИЕНТА С НЕСУЩЕСТВУЮЩИМ customerCode (NEGATIVE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API не позволяет создать конфигурацию для несуществующего клиента
     * - Проверяет что возвращается ошибка 404 или 400
     * - Проверяет что сообщение содержит "customer nonExistentCustomer123 not found"
     */
    @Test(priority = 42, description = "NEGATIVE POST /customerConfigs - несуществующий customerCode")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка создать конфигурацию клиента с несуществующим customerCode. Проверка статуса 404 или 400 и сообщения об ошибке.")
    @Story("Negative Tests - Non-existent CustomerCode")
    public void testCreateCustomerConfigWithNonExistentCustomerCode() {
        logger.info("Выполнение теста: NEGATIVE POST /customerConfigs - несуществующий customerCode");
        
        // JSON строка с несуществующим customerCode
        String requestBody = "{\"customerCode\": \"nonExistentCustomer123\", \"layerId\": 24, \"alias\": \"тестовый Wi-Fi\", \"properties\": [\"id\"], \"treeLayerId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"order\": 0, \"lowerOverlapPercent\": 0, \"upperOverlapPercent\": 0}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(CUSTOMER_CONFIGS_ENDPOINT)
                .then()
                .statusCode(anyOf(is(404), is(400))) // Ожидаем либо 404, либо 400
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("customer nonExistentCustomer123 not found")) // Проверяем текст ошибки
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 43: СОЗДАНИЕ КОНФИГУРАЦИИ КЛИЕНТА С НЕВАЛИДНЫМ UUID (NEGATIVE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет валидацию UUID в поле treeLayerId
     * - Проверяет что API возвращает ошибку 500 при невалидном UUID
     * - Проверяет что сообщение содержит "UUID has to be represented by standard 36-char representation"
     */
    @Test(priority = 43, description = "NEGATIVE POST /customerConfigs - невалидный UUID в treeLayerId")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка создать конфигурацию клиента с невалидным UUID в treeLayerId. Проверка статуса 500 и сообщения об ошибке.")
    @Story("Negative Tests - Invalid UUID")
    public void testCreateCustomerConfigWithInvalidUUID() {
        logger.info("Выполнение теста: NEGATIVE POST /customerConfigs - невалидный UUID в treeLayerId");
        
        // JSON строка с невалидным UUID
        String requestBody = "{\"customerCode\": \"autotest\", \"layerId\": 24, \"alias\": \"тестовый Wi-Fi\", \"properties\": [\"id\"], \"treeLayerId\": \"invalid-uuid\", \"order\": 0, \"lowerOverlapPercent\": 0, \"upperOverlapPercent\": 0}";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(CUSTOMER_CONFIGS_ENDPOINT)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("UUID has to be represented by standard 36-char representation")) // Проверяем текст ошибки
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 44: ПОЛУЧЕНИЕ КОНФИГУРАЦИИ КЛИЕНТА (GET TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API возвращает конкретную конфигурацию по customerCode и layerId
     * - Проверяет структуру данных конфигурации (id, customer, layerDetails, alias, properties, order)
     */
    @Test(priority = 44, description = "GET /customerConfigs/{customerCode}/{layerId}")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение конфигурации клиента. Проверка статуса 200 и структуры данных.")
    @Story("Get CustomerConfig")
    public void testGetCustomerConfig() {
        logger.info("Выполнение теста: GET /customerConfigs/{customerCode}/{layerId}");
        
        // Формируем endpoint с customerCode и layerId
        String getEndpoint = CUSTOMER_CONFIGS_ENDPOINT + "/" + CUSTOMER_CODE + "/" + LAYER_ID;
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .get(getEndpoint) // GET запрос на /customerConfigs/{customerCode}/{layerId}
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class)) // Ответ - объект
                .body("id", notNullValue()) // Проверяем наличие id
                .body("customer", notNullValue()) // Проверяем наличие customer
                .body("layerDetails", notNullValue()) // Проверяем наличие layerDetails
                .body("alias", equalTo("тестовый Wi-Fi")) // Проверяем alias
                .body("properties", hasItem("id")) // Проверяем что properties содержит "id"
                .extract()
                .response();
        
        // Проверяем order отдельно, так как API может вернуть 0.0 (Float) вместо 0 (Integer)
       
        // В Postman: pm.expect(jsonData.order).to.eql(0)
        Number order = response.jsonPath().get("order");
        assert order != null && order.doubleValue() == 0.0 : 
            "order должен быть 0, получено: " + order;
        
        logger.info("CustomerConfig получен успешно. ID: {}, alias: {}, order: {}", 
            response.jsonPath().getInt("id"), response.jsonPath().getString("alias"), order);
    }

    /**
     * ТЕСТ 45: ПОЛУЧЕНИЕ КОНФИГУРАЦИИ С НЕСУЩЕСТВУЮЩИМ customerCode (NEGATIVE GET TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API возвращает ошибку 404 для несуществующего customerCode
     * - Проверяет что сообщение содержит нужный текст
     */
    @Test(priority = 45, description = "NEGATIVE GET /customerConfigs/{customerCode}/{layerId} - несуществующий customerCode")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка получить конфигурацию с несуществующим customerCode. Проверка статуса 404 и сообщения об ошибке.")
    @Story("Negative Tests - Non-existent CustomerCode in GET")
    public void testGetCustomerConfigWithNonExistentCustomerCode() {
        logger.info("Выполнение теста: NEGATIVE GET /customerConfigs - несуществующий customerCode");
        
        // Формируем endpoint с несуществующим customerCode
        String getEndpoint = CUSTOMER_CONFIGS_ENDPOINT + "/nonExistentCustomer123/" + LAYER_ID;
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .get(getEndpoint)
                .then()
                .statusCode(404)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("there is no config for layer 24 and customer nonExistentCustomer123")) // Проверяем текст ошибки
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 46: ОБНОВЛЕНИЕ КОНФИГУРАЦИИ КЛИЕНТА (PUT TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API позволяет обновить существующую конфигурацию клиента
     * - Проверяет что ответ содержит тот же ID что был при создании
     * - Проверяет что изменения применены (alias, order, properties)
     */
    @Test(priority = 46, description = "PUT /customerConfigs/{customerCode}/{layerId}")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Обновление конфигурации клиента. Проверка статуса 200 и что id совпадает с созданным.")
    @Story("Update CustomerConfig")
    public void testUpdateCustomerConfig() {
        logger.info("Выполнение теста: PUT /customerConfigs/{customerCode}/{layerId}");
        
        // Проверяем что customerConfigId был создан в тесте 39
        if (customerConfigId == null) {
            logger.error("customerConfigId не установлен! Нельзя обновить конфигурацию.");
            throw new IllegalStateException("customerConfigId должен быть установлен перед обновлением");
        }
        
        // Формируем endpoint с customerCode и layerId
        String putEndpoint = CUSTOMER_CONFIGS_ENDPOINT + "/" + CUSTOMER_CODE + "/" + LAYER_ID;
        
        // Создаем объект для обновления (без customerCode и layerId, так как они в URL)
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("alias", "string");
        updateData.put("properties", new ArrayList<>());
        updateData.put("treeLayerId", "3fa85f64-5717-4562-b3fc-2c963f66afa6");
        updateData.put("order", 1);
        updateData.put("lowerOverlapPercent", 0);
        updateData.put("upperOverlapPercent", 0);
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(updateData)
                .when()
                .put(putEndpoint) // PUT запрос на /customerConfigs/{customerCode}/{layerId}
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("id", notNullValue())
                .body("id", equalTo(customerConfigId)) // Проверяем что ID совпадает с созданным
                .extract()
                .response();
        
        logger.info("CustomerConfig обновлен успешно. ID: {}", response.jsonPath().getInt("id"));
    }

    /**
     * ТЕСТ 47: ОБНОВЛЕНИЕ КОНФИГУРАЦИИ С НЕСУЩЕСТВУЮЩИМ customerCode (NEGATIVE PUT TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API возвращает ошибку 404 для несуществующего customerCode
     * - Проверяет что сообщение содержит нужный текст
     */
    @Test(priority = 47, description = "NEGATIVE PUT /customerConfigs/{customerCode}/{layerId} - несуществующий customerCode")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка обновить конфигурацию с несуществующим customerCode. Проверка статуса 404 и сообщения об ошибке.")
    @Story("Negative Tests - Non-existent CustomerCode in PUT")
    public void testUpdateCustomerConfigWithNonExistentCustomerCode() {
        logger.info("Выполнение теста: NEGATIVE PUT /customerConfigs/{customerCode}/{layerId} - несуществующий customerCode");
        
        // Формируем endpoint с несуществующим customerCode
        String putEndpoint = CUSTOMER_CONFIGS_ENDPOINT + "/nonExistentCustomer123/" + LAYER_ID;
        
        // Создаем объект для обновления
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("alias", "string");
        updateData.put("properties", new ArrayList<>());
        updateData.put("treeLayerId", "3fa85f64-5717-4562-b3fc-2c963f66afa6");
        updateData.put("order", 1);
        updateData.put("lowerOverlapPercent", 0);
        updateData.put("upperOverlapPercent", 0);
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(updateData)
                .when()
                .put(putEndpoint)
                .then()
                .statusCode(404)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("there is no config for layer 24 and customer nonExistentCustomer123")) // Проверяем текст ошибки
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 48: ЭКСПОРТ ВСЕХ КОНФИГУРАЦИЙ КЛИЕНТОВ (GET EXPORT TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API возвращает массив всех конфигураций
     * - Проверяет что изменения для слоя применены (alias, order, properties)
     */
    @Test(priority = 48, description = "GET /customerConfigs/export")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Экспорт всех конфигураций клиентов. Проверка статуса 200, что ответ - массив, и что изменения применены.")
    @Story("Export CustomerConfigs")
    public void testExportCustomerConfigs() {
        logger.info("Выполнение теста: GET /customerConfigs/export");
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .get(CUSTOMER_CONFIGS_ENDPOINT + "/export") // GET запрос на /customerConfigs/export
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class)) // Ответ - массив
                .extract()
                .response();
        
        // Проверяем что массив содержит конфигурацию для нашего клиента и слоя
        List<Map<String, Object>> configs = response.jsonPath().getList("$");
        boolean found = false;
        for (Map<String, Object> config : configs) {
            if (CUSTOMER_CODE.equals(config.get("customerCode")) && 
                LAYER_ID.equals(((Number) config.get("layerId")).intValue())) {
                found = true;
                assert "string".equals(config.get("alias")) : 
                    "alias должен быть 'string', получено: " + config.get("alias");
                assert ((Number) config.get("order")).intValue() == 1 : 
                    "order должен быть 1, получено: " + config.get("order");
                assert config.get("properties") instanceof List : 
                    "properties должен быть массивом";
                List<?> properties = (List<?>) config.get("properties");
                assert properties.isEmpty() : 
                    "properties должен быть пустым массивом";
                break;
            }
        }
        
        assert found : "Конфигурация для customerCode=" + CUSTOMER_CODE + " и layerId=" + LAYER_ID + " не найдена";
        
        logger.info("Экспорт выполнен успешно. Найдена конфигурация с изменениями: alias=string, order=1, properties=[]");
    }

    /**
     * ТЕСТ 49: ПОЛУЧЕНИЕ КОНФИГУРАЦИЙ ПО LAYER ID (GET BY LAYER ID TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API возвращает конфигурации для конкретного layerId
     * - Проверяет статус 200
     */
    @Test(priority = 49, description = "GET /customerConfigs/getByLayerId/{layerId}")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение конфигураций по layerId. Проверка статуса 200.")
    @Story("Get CustomerConfigs by LayerId")
    public void testGetCustomerConfigsByLayerId() {
        logger.info("Выполнение теста: GET /customerConfigs/getByLayerId/{layerId}");
        
        // Формируем endpoint с layerId
        String getEndpoint = CUSTOMER_CONFIGS_ENDPOINT + "/getByLayerId/" + LAYER_ID;
        
        given()
                .spec(getRequestSpec())
                .when()
                .get(getEndpoint) // GET запрос на /customerConfigs/getByLayerId/{layerId}
                .then()
                .statusCode(200);
        
        logger.info("Конфигурации по layerId получены успешно. layerId: {}", LAYER_ID);
    }

    /**
     * ТЕСТ 50: УДАЛЕНИЕ КОНФИГУРАЦИИ КЛИЕНТА (DELETE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Удаляет созданную конфигурацию клиента
     * - Проверяет что удаление прошло успешно (статус 200)
     */
    @Test(priority = 50, description = "DELETE /customerConfigs/{customerCode}/{layerId}")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Удаление конфигурации клиента. Проверка статуса 200.")
    @Story("Delete CustomerConfig")
    public void testDeleteCustomerConfig() {
        logger.info("Выполнение теста: DELETE /customerConfigs/{customerCode}/{layerId}");
        
        // Формируем endpoint с customerCode и layerId
        String deleteEndpoint = CUSTOMER_CONFIGS_ENDPOINT + "/" + CUSTOMER_CODE + "/" + LAYER_ID;
        
        given()
                .spec(getRequestSpec())
                .when()
                .delete(deleteEndpoint) // DELETE запрос на /customerConfigs/{customerCode}/{layerId}
                .then()
                .statusCode(200); // Ожидаем успешное удаление
        
        logger.info("CustomerConfig успешно удален. customerCode: {}, layerId: {}", CUSTOMER_CODE, LAYER_ID);
    }

    /**
     * ТЕСТ 51: ПОВТОРНОЕ УДАЛЕНИЕ КОНФИГУРАЦИИ (NEGATIVE DELETE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API возвращает ошибку 404 при попытке удалить уже удаленную конфигурацию
     * - Проверяет что сообщение содержит нужный текст
     */
    @Test(priority = 51, description = "NEGATIVE DELETE /customerConfigs/{customerCode}/{layerId} - повторное удаление")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка повторно удалить конфигурацию. Проверка статуса 404 и сообщения об ошибке.")
    @Story("Negative Tests - Repeated Delete")
    public void testDeleteCustomerConfigRepeated() {
        logger.info("Выполнение теста: NEGATIVE DELETE /customerConfigs/{customerCode}/{layerId} - повторное удаление");
        
        // Формируем endpoint с customerCode и layerId
        String deleteEndpoint = CUSTOMER_CONFIGS_ENDPOINT + "/" + CUSTOMER_CODE + "/" + LAYER_ID;
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .delete(deleteEndpoint)
                .then()
                .statusCode(404)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("there is no config for layer 24 and customer autotest")) // Проверяем текст ошибки
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 52: УДАЛЕНИЕ КОНФИГУРАЦИИ С НЕСУЩЕСТВУЮЩИМ customerCode (NEGATIVE DELETE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API возвращает ошибку 404 для несуществующего customerCode
     * - Проверяет что сообщение содержит нужный текст
     */
    @Test(priority = 52, description = "NEGATIVE DELETE /customerConfigs/{customerCode}/{layerId} - несуществующий customerCode")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка удалить конфигурацию с несуществующим customerCode. Проверка статуса 404 и сообщения об ошибке.")
    @Story("Negative Tests - Non-existent CustomerCode in DELETE")
    public void testDeleteCustomerConfigWithNonExistentCustomerCode() {
        logger.info("Выполнение теста: NEGATIVE DELETE /customerConfigs/{customerCode}/{layerId} - несуществующий customerCode");
        
        // Формируем endpoint с несуществующим customerCode
        String deleteEndpoint = CUSTOMER_CONFIGS_ENDPOINT + "/nonExistentCustomer123/" + LAYER_ID;
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .delete(deleteEndpoint)
                .then()
                .statusCode(404)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("there is no config for layer 24 and customer nonExistentCustomer123")) // Проверяем текст ошибки
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 53: ПОЛУЧЕНИЕ КОНФИГУРАЦИЙ ПО CUSTOMER CODE (ПУСТОЙ МАССИВ ПОСЛЕ УДАЛЕНИЯ)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API возвращает пустой массив после удаления конфигурации
     * - Проверяет статус 200
     */
    @Test(priority = 53, description = "GET /customerConfigs/getByCustomerCode/{customerCode} (пустой массив)")
    @Severity(SeverityLevel.NORMAL)
    @Description("Получение конфигураций по customerCode после удаления. Проверка статуса 200 и что массив пустой.")
    @Story("Get CustomerConfigs by CustomerCode - Empty Array")
    public void testGetCustomerConfigsByCustomerCodeEmpty() {
        logger.info("Выполнение теста: GET /customerConfigs/getByCustomerCode/{customerCode} (пустой массив)");
        
        // Формируем endpoint с customerCode
        String getEndpoint = CUSTOMER_CONFIGS_ENDPOINT + "/getByCustomerCode/" + CUSTOMER_CODE;
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .get(getEndpoint) // GET запрос на /customerConfigs/getByCustomerCode/{customerCode}
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class)) // Ответ - массив
                .extract()
                .response();
        
        // Проверяем что массив пустой
        List<?> configs = response.jsonPath().getList("$");
        assert configs.isEmpty() : "Массив должен быть пустым после удаления, но содержит " + configs.size() + " элементов";
        
        logger.info("Массив пустой после удаления. Количество элементов: {}", configs.size());
    }

    /**
     * ТЕСТ 54: ВОССТАНОВЛЕНИЕ КОНФИГУРАЦИЙ КЛИЕНТОВ (POST RESTORE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API позволяет восстановить конфигурации клиентов
     * - Проверяет статус 200
     */
    @Test(priority = 54, description = "POST /customerConfigs/restore")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Восстановление конфигураций клиентов. Проверка статуса 200.")
    @Story("Restore CustomerConfigs")
    public void testRestoreCustomerConfigs() {
        logger.info("Выполнение теста: POST /customerConfigs/restore");
        
        // Создаем список конфигураций для восстановления
        List<Map<String, Object>> restoreData = new ArrayList<>();
        Map<String, Object> config = new HashMap<>();
        config.put("customerCode", CUSTOMER_CODE);
        config.put("layerId", LAYER_ID);
        config.put("alias", "restore");
        List<String> properties = new ArrayList<>();
        properties.add("id");
        config.put("properties", properties);
        config.put("treeLayerId", "3fa85f64-5717-4562-b3fc-2c963f66afa6");
        config.put("order", 0);
        config.put("lowerOverlapPercent", 0);
        config.put("upperOverlapPercent", 0);
        restoreData.add(config);
        
        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(restoreData)
                .when()
                .post(CUSTOMER_CONFIGS_ENDPOINT + "/restore") // POST запрос на /customerConfigs/restore
                .then()
                .statusCode(200);
        
        logger.info("Конфигурации успешно восстановлены");
    }

    /**
     * ТЕСТ 55: ВОССТАНОВЛЕНИЕ С НЕВАЛИДНЫМ JSON (NEGATIVE POST RESTORE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API возвращает ошибку 500 при невалидном JSON
     * - Проверяет что сообщение содержит "Unexpected end-of-input"
     */
    @Test(priority = 55, description = "NEGATIVE POST /customerConfigs/restore - невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка восстановить конфигурации с невалидным JSON. Проверка статуса 500 и сообщения об ошибке.")
    @Story("Negative Tests - Invalid JSON in Restore")
    public void testRestoreCustomerConfigsWithInvalidJSON() {
        logger.info("Выполнение теста: NEGATIVE POST /customerConfigs/restore - невалидный JSON");
        
        // Невалидный JSON (незакрытый объект)
        String invalidJSON = "[\n  {\n    \"customerCode\": \"autotest\",\n    \"layerId\": 24\n";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(invalidJSON)
                .when()
                .post(CUSTOMER_CONFIGS_ENDPOINT + "/restore")
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("Unexpected end-of-input: expected close marker for Object")) // Проверяем текст ошибки
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 56: ПОЛУЧЕНИЕ КОНФИГУРАЦИЙ ПО CUSTOMER CODE (ПОСЛЕ RESTORE)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API возвращает восстановленные конфигурации
     * - Проверяет что массив не пустой и содержит восстановленную конфигурацию
     * - Проверяет структуру восстановленной конфигурации
     */
    @Test(priority = 56, description = "GET /customerConfigs/getByCustomerCode/{customerCode} (после restore)")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение конфигураций по customerCode после restore. Проверка статуса 200, что массив не пустой и содержит восстановленную конфигурацию.")
    @Story("Get CustomerConfigs by CustomerCode - After Restore")
    @SuppressWarnings("unchecked")
    public void testGetCustomerConfigsByCustomerCodeAfterRestore() {
        logger.info("Выполнение теста: GET /customerConfigs/getByCustomerCode/{customerCode} (после restore)");
        
        // Формируем endpoint с customerCode
        String getEndpoint = CUSTOMER_CONFIGS_ENDPOINT + "/getByCustomerCode/" + CUSTOMER_CODE;
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .get(getEndpoint) // GET запрос на /customerConfigs/getByCustomerCode/{customerCode}
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class)) // Ответ - массив
                .extract()
                .response();
        
        // Проверяем что массив не пустой
        List<Map<String, Object>> configs = response.jsonPath().getList("$");
        assert !configs.isEmpty() : "Массив не должен быть пустым после restore";
        
        // Ищем восстановленную конфигурацию
        boolean found = false;
        for (Map<String, Object> config : configs) {
            Map<String, Object> customer = (Map<String, Object>) config.get("customer");
            Map<String, Object> layerDetails = (Map<String, Object>) config.get("layerDetails");
            
            if (customer != null && CUSTOMER_CODE.equals(customer.get("code")) &&
                layerDetails != null && LAYER_ID.equals(((Number) layerDetails.get("layerId")).intValue())) {
                found = true;
                assert "restore".equals(config.get("alias")) : 
                    "alias должен быть 'restore', получено: " + config.get("alias");
                assert config.get("properties") instanceof List : 
                    "properties должен быть массивом";
                List<?> properties = (List<?>) config.get("properties");
                assert properties.contains("id") : 
                    "properties должен содержать 'id'";
                assert ((Number) config.get("order")).intValue() == 0 : 
                    "order должен быть 0, получено: " + config.get("order");
                break;
            }
        }
        
        assert found : "Восстановленная конфигурация для customerCode=" + CUSTOMER_CODE + " и layerId=" + LAYER_ID + " не найдена";
        
        logger.info("Восстановленная конфигурация найдена. Количество конфигураций: {}", configs.size());
    }

    /**
     * ТЕСТ 57: ПОЛУЧЕНИЕ LAYER DETAILS OUT OF SYNC (GET TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API возвращает массив layerDetails со статусом outOfSync
     * - Проверяет статус 200
     * - Если массив не пустой, проверяет что есть элементы со статусом NOT_UPLOADED
     */
    @Test(priority = 57, description = "GET /layerDetails/outOfSync")
    @Severity(SeverityLevel.NORMAL)
    @Description("Получение layerDetails со статусом outOfSync. Проверка статуса 200 и структуры ответа.")
    @Story("Get LayerDetails OutOfSync")
    public void testGetLayerDetailsOutOfSync() {
        logger.info("Выполнение теста: GET /layerDetails/outOfSync");
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .get("/da-cm-map-backend-manager-intersect-object/layerDetails/outOfSync") // GET запрос на /layerDetails/outOfSync
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class)) // Ответ - массив
                .extract()
                .response();
        
        // Проверяем структуру ответа
        List<Map<String, Object>> layerDetails = response.jsonPath().getList("$");
        
        // Если массив не пустой, проверяем что есть элементы со статусом NOT_UPLOADED
        if (!layerDetails.isEmpty()) {
            boolean foundNotUploaded = false;
            for (Map<String, Object> detail : layerDetails) {
                if (detail != null && "NOT_UPLOADED".equals(detail.get("status"))) {
                    foundNotUploaded = true;
                    assert "NOT_UPLOADED".equals(detail.get("status")) : 
                        "Статус должен быть NOT_UPLOADED";
                    break;
                }
            }
            if (foundNotUploaded) {
                logger.info("Найден элемент со статусом NOT_UPLOADED. Всего элементов: {}", layerDetails.size());
            } else {
                logger.info("Элементы со статусом NOT_UPLOADED не найдены. Всего элементов: {}", layerDetails.size());
            }
        } else {
            logger.info("Массив layerDetails пустой");
        }
    }

    /**
     * ТЕСТ 58: ОБНОВЛЕНИЕ ПРОЦЕНТА LAYER DETAILS (PATCH TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API позволяет обновить процент загрузки layerDetails
     * - Проверяет статус 200
     */
    @Test(priority = 58, description = "PATCH /layerDetails/{layerId}/progress")
    @Severity(SeverityLevel.NORMAL)
    @Description("Обновление процента загрузки layerDetails. Проверка статуса 200.")
    @Story("Update LayerDetails Progress")
    public void testPatchLayerDetailsProgress() {
        logger.info("Выполнение теста: PATCH /layerDetails/{layerId}/progress");
        
        // Формируем endpoint с layerId
        String patchEndpoint = LAYER_DETAILS_ENDPOINT + "/" + LAYER_ID + "/progress";
        
        // Создаем объект для обновления процента
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("percent", 95);
        
        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(updateData)
                .when()
                .patch(patchEndpoint) // PATCH запрос на /layerDetails/{layerId}/progress
                .then()
                .statusCode(200);
        
        logger.info("Процент layerDetails успешно обновлен. layerId: {}, percent: 95", LAYER_ID);
    }

    /**
     * ТЕСТ 59: ПОЛУЧЕНИЕ СПИСКА LAYER DETAILS С ПРОВЕРКОЙ ПРОЦЕНТА (GET TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API возвращает список layerDetails
     * - Проверяет что есть слой с именем содержащим "Wi-Fi в парках (полигоны)" и процентом 95
     */
    @Test(priority = 59, description = "GET /layerDetails?page=0&size=1000")
    @Severity(SeverityLevel.NORMAL)
    @Description("Получение списка layerDetails с проверкой обновленного процента. Проверка статуса 200 и наличия слоя с процентом 95.")
    @Story("Get LayerDetails List")
    public void testGetLayerDetailsList() {
        logger.info("Выполнение теста: GET /layerDetails?page=0&size=1000");
        
        Response response = given()
                .spec(getRequestSpec())
                .queryParam("page", 0)
                .queryParam("size", 1000)
                .when()
                .get(LAYER_DETAILS_ENDPOINT) // GET запрос на /layerDetails?page=0&size=1000
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("content", notNullValue())
                .body("content", instanceOf(java.util.List.class))
                .extract()
                .response();
        
        // Проверяем что есть слой с именем содержащим "Wi-Fi в парках (полигоны)" и процентом 95
        List<Map<String, Object>> content = response.jsonPath().getList("content");
        boolean found = false;
        for (Map<String, Object> layer : content) {
            String name = (String) layer.get("name");
            Number percent = (Number) layer.get("percent");
            if (name != null && name.contains("Wi-Fi в парках (полигоны)") && 
                percent != null && percent.intValue() == 95) {
                found = true;
                break;
            }
        }
        
        assert found : "Слой с именем содержащим 'Wi-Fi в парках (полигоны)' и процентом 95 не найден";
        
        logger.info("Слой с процентом 95 найден. Всего элементов: {}", content.size());
    }

    /**
     * ТЕСТ 60: ОТКАТ ПРОЦЕНТА LAYER DETAILS (PATCH REVERT TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Возвращает процент загрузки layerDetails обратно на 100
     * - Проверяет статус 200
     */
    @Test(priority = 60, description = "PATCH /layerDetails/{layerId}/progress revert")
    @Severity(SeverityLevel.NORMAL)
    @Description("Откат процента загрузки layerDetails обратно на 100. Проверка статуса 200.")
    @Story("Revert LayerDetails Progress")
    public void testPatchLayerDetailsProgressRevert() {
        logger.info("Выполнение теста: PATCH /layerDetails/{layerId}/progress revert");
        
        // Формируем endpoint с layerId
        String patchEndpoint = LAYER_DETAILS_ENDPOINT + "/" + LAYER_ID + "/progress";
        
        // Создаем объект для отката процента
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("percent", 100);
        
        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(updateData)
                .when()
                .patch(patchEndpoint) // PATCH запрос на /layerDetails/{layerId}/progress
                .then()
                .statusCode(200);
        
        logger.info("Процент layerDetails успешно откачен. layerId: {}, percent: 100", LAYER_ID);
    }

    /**
     * ТЕСТ 61: ОБНОВЛЕНИЕ ПРОЦЕНТА С НЕВЕРНЫМ LAYER ID (NEGATIVE PATCH TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API возвращает ошибку 404 для несуществующего layerId
     * - Проверяет что сообщение содержит "there is no layer details for layer id 999,999,999"
     */
    @Test(priority = 61, description = "NEGATIVE PATCH /layerDetails/{layerId}/progress - неверный layerID")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка обновить процент для несуществующего layerId. Проверка статуса 404 и сообщения об ошибке.")
    @Story("Negative Tests - Invalid LayerId in PATCH")
    public void testPatchLayerDetailsProgressWithInvalidLayerId() {
        logger.info("Выполнение теста: NEGATIVE PATCH /layerDetails/{layerId}/progress - неверный layerID");
        
        // Формируем endpoint с несуществующим layerId
        String patchEndpoint = LAYER_DETAILS_ENDPOINT + "/999999999/progress";
        
        // Создаем объект для обновления процента
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("percent", 100);
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(updateData)
                .when()
                .patch(patchEndpoint)
                .then()
                .statusCode(404)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("there is no layer details for layer id 999,999,999")) // Проверяем текст ошибки
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 62: ОБНОВЛЕНИЕ LAYER DETAILS (PUT TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API позволяет обновить layerDetails
     * - Проверяет статус 200
     * 
     * ПРИМЕЧАНИЕ: В Postman название теста "GET", но выполняется PUT запрос
     */
    @Test(priority = 62, description = "PUT /layerDetails/{layerId}")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Обновление layerDetails. Проверка статуса 200.")
    @Story("Update LayerDetails")
    public void testPutLayerDetails() {
        logger.info("Выполнение теста: PUT /layerDetails/{layerId}");
        
        // Формируем endpoint с layerId
        String putEndpoint = LAYER_DETAILS_ENDPOINT + "/" + LAYER_ID;
        
        // Создаем объект для обновления layerDetails
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("id", 48);
        updateData.put("layerId", 24);
        updateData.put("versionId", 103269);
        updateData.put("name", "Wi-Fi в парках (полигоны)_autotest");
        updateData.put("type", "vctr");
        updateData.put("code", "park_wifi_polygon");
        updateData.put("objectsCount", 1220);
        updateData.put("status", "UPLOADED");
        updateData.put("percent", 100);
        updateData.put("created", "2025-12-04T19:12:28.030784");
        updateData.put("lastUpdated", "2025-12-05T16:46:09.104779");
        updateData.put("lastSyncAttempt", "2025-12-05T16:46:09.104779");
        updateData.put("message", "OK");
        
        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(updateData)
                .when()
                .put(putEndpoint) // PUT запрос на /layerDetails/{layerId}
                .then()
                .statusCode(200);
        
        logger.info("LayerDetails успешно обновлен. layerId: {}", LAYER_ID);
    }

    /**
     * ТЕСТ 63: ОБНОВЛЕНИЕ LAYER DETAILS С СЛОМАННЫМ ТЕЛОМ ЗАПРОСА (NEGATIVE PUT TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API возвращает ошибку 500 при невалидном JSON
     * - Проверяет что сообщение содержит "JSON parse error: Unexpected end-of-input"
     * 
    
     */
    @Test(priority = 63, description = "NEGATIVE PUT /layerDetails/{layerId} - сломанное тело запроса")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка обновить layerDetails с невалидным JSON. Проверка статуса 500 и сообщения об ошибке.")
    @Story("Negative Tests - Invalid JSON in PUT")
    public void testPutLayerDetailsWithInvalidJSON() {
        logger.info("Выполнение теста: NEGATIVE PUT /layerDetails/{layerId} - сломанное тело запроса");
        
        // Формируем endpoint с layerId
        String putEndpoint = LAYER_DETAILS_ENDPOINT + "/" + LAYER_ID;
        
        // Невалидный JSON (незакрытый объект)
        String invalidJSON = "{\n  \"id\": 48,\n  \"layerId\": 24,\n  \"versionId\": 103269,\n  \"name\": \"Wi-Fi в парках (полигоны)_autotest\",\n  \"type\": \"vctr\",\n  \"code\": \"park_wifi_polygon\",\n  \"objectsCount\": 1220,\n  \"status\": \"UPLOADED\",\n  \"percent\": 100,\n  \"created\": \"2025-12-04T19:12:28.030784\",\n  \"lastUpdated\": \"2025-12-05T16:46:09.104779\",\n  \"lastSyncAttempt\": \"2025-12-05T16:46:09.104779\",\n  \"message\": \"OK\"\n";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(invalidJSON)
                .when()
                .put(putEndpoint)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("JSON parse error: Unexpected end-of-input: expected close marker for Object")) // Проверяем текст ошибки
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 64: ОБНОВЛЕНИЕ LAYER DETAILS С НЕСУЩЕСТВУЮЩИМ LAYER ID (NEGATIVE PUT TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API возвращает ошибку 404 для несуществующего layerId
     * - Проверяет что сообщение содержит "there is no layer details for layer id 999,999,999"
     * 
     */
    @Test(priority = 64, description = "NEGATIVE PUT /layerDetails/{layerId} - несуществующий layerID")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка обновить layerDetails с несуществующим layerId. Проверка статуса 404 и сообщения об ошибке.")
    @Story("Negative Tests - Non-existent LayerId in PUT")
    public void testPutLayerDetailsWithNonExistentLayerId() {
        logger.info("Выполнение теста: NEGATIVE PUT /layerDetails/{layerId} - несуществующий layerID");
        
        // Формируем endpoint с несуществующим layerId
        String putEndpoint = LAYER_DETAILS_ENDPOINT + "/999999999";
        
        // Создаем объект для обновления layerDetails
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("id", 48);
        updateData.put("layerId", 24);
        updateData.put("versionId", 103269);
        updateData.put("name", "Wi-Fi в парках (полигоны)_autotest");
        updateData.put("type", "vctr");
        updateData.put("code", "park_wifi_polygon");
        updateData.put("objectsCount", 1220);
        updateData.put("status", "UPLOADED");
        updateData.put("percent", 100);
        updateData.put("created", "2025-12-04T19:12:28.030784");
        updateData.put("lastUpdated", "2025-12-05T16:46:09.104779");
        updateData.put("lastSyncAttempt", "2025-12-05T16:46:09.104779");
        updateData.put("message", "OK");
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(updateData)
                .when()
                .put(putEndpoint)
                .then()
                .statusCode(404)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("there is no layer details for layer id 999,999,999")) // Проверяем текст ошибки
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 65: ПОЛУЧЕНИЕ LAYER DETAILS ПО LAYER ID (GET TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API возвращает layerDetails по layerId
     * - Проверяет что имя содержит "Wi-Fi в парках (полигоны)_autotest" и процент равен 100
     */
    @Test(priority = 65, description = "GET /layerDetails/{layerId}")
    @Severity(SeverityLevel.NORMAL)
    @Description("Получение layerDetails по layerId. Проверка статуса 200, имени и процента.")
    @Story("Get LayerDetails by LayerId")
    public void testGetLayerDetailsByLayerId() {
        logger.info("Выполнение теста: GET /layerDetails/{layerId}");
        
        // Формируем endpoint с layerId
        String getEndpoint = LAYER_DETAILS_ENDPOINT + "/" + LAYER_ID;
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .get(getEndpoint) // GET запрос на /layerDetails/{layerId}
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("name", notNullValue())
                .body("percent", notNullValue())
                .extract()
                .response();
        
        // Проверяем что имя содержит нужный текст и процент равен 100
        String name = response.jsonPath().getString("name");
        Number percent = response.jsonPath().get("percent");
        
        assert name != null && name.contains("Wi-Fi в парках (полигоны)_autotest") : 
            "Имя должно содержать 'Wi-Fi в парках (полигоны)_autotest', получено: " + name;
        assert percent != null && percent.intValue() == 100 : 
            "Процент должен быть 100, получено: " + percent;
        
        logger.info("LayerDetails получен успешно. name: {}, percent: {}", name, percent);
    }

    /**
     * ТЕСТ 66: ОТКАТ ИМЕНИ LAYER DETAILS (PUT REVERT TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Возвращает имя layerDetails обратно на оригинальное значение
     * - Проверяет статус 200
     */
    @Test(priority = 66, description = "PUT /layerDetails/{layerId} revert name")
    @Severity(SeverityLevel.NORMAL)
    @Description("Откат имени layerDetails обратно на оригинальное значение. Проверка статуса 200.")
    @Story("Revert LayerDetails Name")
    public void testPutLayerDetailsRevertName() {
        logger.info("Выполнение теста: PUT /layerDetails/{layerId} revert name");
        
        // Формируем endpoint с layerId
        String putEndpoint = LAYER_DETAILS_ENDPOINT + "/" + LAYER_ID;
        
        // Создаем объект для отката имени
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("id", 48);
        updateData.put("layerId", 24);
        updateData.put("versionId", 103269);
        updateData.put("name", "Wi-Fi в парках (полигоны)");
        updateData.put("type", "vctr");
        updateData.put("code", "park_wifi_polygon");
        updateData.put("objectsCount", 1220);
        updateData.put("status", "UPLOADED");
        updateData.put("percent", 100);
        updateData.put("created", "2025-12-04T19:12:28.030784");
        updateData.put("lastUpdated", "2025-12-05T16:46:09.104779");
        updateData.put("lastSyncAttempt", "2025-12-05T16:46:09.104779");
        updateData.put("message", "OK");
        
        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(updateData)
                .when()
                .put(putEndpoint) // PUT запрос на /layerDetails/{layerId}
                .then()
                .statusCode(200);
        
        logger.info("Имя layerDetails успешно откачено. layerId: {}", LAYER_ID);
    }

    /**
     * ТЕСТ 67: ОБНОВЛЕНИЕ КЛИЕНТА (PUT TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API позволяет обновить имя клиента
     * - Проверяет что ответ содержит тот же ID что был при создании
     */
    @Test(priority = 67, description = "PUT update customer")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Обновление клиента. Проверка статуса 200 и что id совпадает с созданным.")
    @Story("Update Customer")
    public void testUpdateCustomer() {
        logger.info("Выполнение теста: PUT update customer");
        
        // Проверяем что customerId был создан в первом тесте
        if (customerId == null) {
            logger.error("customerId не установлен! Нельзя обновить клиента.");
            throw new IllegalStateException("customerId должен быть установлен перед обновлением");
        }
        
        // Формируем endpoint с ID клиента
        String putEndpoint = CUSTOMERS_ENDPOINT + "/" + customerId;
        
        // Создаем объект для обновления
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", "autotestCustomerPUT");
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(updateData)
                .when()
                .put(putEndpoint) // PUT запрос на /customers/{id}
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("id", notNullValue())
                .body("id", equalTo(customerId)) // Проверяем что ID совпадает с созданным
                .extract()
                .response();
        
        logger.info("Customer успешно обновлен. ID: {}, новое имя: {}", response.jsonPath().getInt("id"), "autotestCustomerPUT");
    }

    /**
     * ТЕСТ 68: ОБНОВЛЕНИЕ КЛИЕНТА С НЕСУЩЕСТВУЮЩИМ ID (NEGATIVE PUT TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API возвращает ошибку 500 для несуществующего customerId
     * - Проверяет что сообщение содержит "999,999 not found"
     */
    @Test(priority = 68, description = "NEGATIVE PUT update customer - несуществующий customerId")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка обновить клиента с несуществующим customerId. Проверка статуса 500 и сообщения об ошибке.")
    @Story("Negative Tests - Non-existent CustomerId in PUT")
    public void testUpdateCustomerWithNonExistentId() {
        logger.info("Выполнение теста: NEGATIVE PUT update customer - несуществующий customerId");
        
        // Формируем endpoint с несуществующим customerId
        String putEndpoint = CUSTOMERS_ENDPOINT + "/999999";
        
        // Создаем объект для обновления
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", "autotestCustomerPUT");
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(updateData)
                .when()
                .put(putEndpoint)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("999,999 not found")) // Проверяем текст ошибки
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 69: ОБНОВЛЕНИЕ КЛИЕНТА С ПУСТЫМ BODY (NEGATIVE PUT TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API возвращает ошибку 500 при пустом body
     * - Проверяет что сообщение содержит "[ERROR: null value in column"
     */
    @Test(priority = 69, description = "NEGATIVE PUT update customer - пустой body")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка обновить клиента с пустым body. Проверка статуса 500 и сообщения об ошибке.")
    @Story("Negative Tests - Empty Body in PUT")
    public void testUpdateCustomerWithEmptyBody() {
        logger.info("Выполнение теста: NEGATIVE PUT update customer - пустой body");
        
        // Проверяем что customerId был создан в первом тесте
        if (customerId == null) {
            logger.error("customerId не установлен! Нельзя обновить клиента.");
            throw new IllegalStateException("customerId должен быть установлен перед обновлением");
        }
        
        // Формируем endpoint с ID клиента
        String putEndpoint = CUSTOMERS_ENDPOINT + "/" + customerId;
        
        // Пустой объект
        Map<String, Object> updateData = new HashMap<>();
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(updateData)
                .when()
                .put(putEndpoint)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("[ERROR: null value in column")) // Проверяем текст ошибки
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 70: ОБНОВЛЕНИЕ КЛИЕНТА С НЕВАЛИДНЫМ JSON (NEGATIVE PUT TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API возвращает ошибку 500 при невалидном JSON
     * - Проверяет что сообщение содержит "JSON parse error: Unexpected end-of-input"
     */
    @Test(priority = 70, description = "NEGATIVE PUT update customer - невалидный JSON")
    @Severity(SeverityLevel.NORMAL)
    @Description("Попытка обновить клиента с невалидным JSON. Проверка статуса 500 и сообщения об ошибке.")
    @Story("Negative Tests - Invalid JSON in PUT")
    public void testUpdateCustomerWithInvalidJSON() {
        logger.info("Выполнение теста: NEGATIVE PUT update customer - невалидный JSON");
        
        // Проверяем что customerId был создан в первом тесте
        if (customerId == null) {
            logger.error("customerId не установлен! Нельзя обновить клиента.");
            throw new IllegalStateException("customerId должен быть установлен перед обновлением");
        }
        
        // Формируем endpoint с ID клиента
        String putEndpoint = CUSTOMERS_ENDPOINT + "/" + customerId;
        
        // Невалидный JSON (незакрытый объект)
        String invalidJSON = "{\n  \"name\": \"autotestCustomerPUT\"\n";
        
        Response response = given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .body(invalidJSON)
                .when()
                .put(putEndpoint)
                .then()
                .statusCode(500)
                .body("$", instanceOf(java.util.Map.class))
                .body("message", notNullValue())
                .body("message", instanceOf(String.class))
                .body("message", containsString("JSON parse error: Unexpected end-of-input")) // Проверяем текст ошибки
                .extract()
                .response();
        
        logger.info("Ожидаемая ошибка получена: {}", response.jsonPath().getString("message"));
    }

    /**
     * ТЕСТ 71: ПОЛУЧЕНИЕ СПИСКА КЛИЕНТОВ С ПРОВЕРКОЙ ОБНОВЛЕННОГО ИМЕНИ (GET TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Проверяет что API возвращает список клиентов
     * - Проверяет что имя клиента обновлено на "autotestCustomerPUT"
     */
    @Test(priority = 71, description = "GET customers list and verify name updated")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Получение списка клиентов с проверкой обновленного имени. Проверка статуса 200 и что имя обновлено.")
    @Story("Get Customers List - Verify Name Updated")
    public void testGetCustomersListAndVerifyNameUpdated() {
        logger.info("Выполнение теста: GET customers list and verify name updated");
        
        // Проверяем что customerId был создан в первом тесте
        if (customerId == null) {
            logger.error("customerId не установлен! Нельзя проверить клиента.");
            throw new IllegalStateException("customerId должен быть установлен перед проверкой");
        }
        
        Response response = given()
                .spec(getRequestSpec())
                .when()
                .get(CUSTOMERS_ENDPOINT) // GET запрос на /customers
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.Map.class))
                .body("content", notNullValue())
                .body("content", instanceOf(java.util.List.class))
                .extract()
                .response();
        
        // Ищем нашего клиента по ID
        List<Map<String, Object>> content = response.jsonPath().getList("content");
        boolean found = false;
        for (Map<String, Object> customer : content) {
            Number id = (Number) customer.get("id");
            if (id != null && id.intValue() == customerId) {
                found = true;
                String name = (String) customer.get("name");
                assert "autotestCustomerPUT".equals(name) : 
                    "Имя должно быть 'autotestCustomerPUT', получено: " + name;
                break;
            }
        }
        
        assert found : "Клиент с ID=" + customerId + " не найден в списке";
        
        logger.info("Имя клиента успешно обновлено. ID: {}, имя: autotestCustomerPUT", customerId);
    }

    /**
     * ТЕСТ 72: УДАЛЕНИЕ КЛИЕНТА (DELETE TEST)
     * 
     * ЗАЧЕМ ЭТОТ ТЕСТ:
     * - Удаляет созданного клиента (очистка после тестов)
     * - Проверяет что удаление прошло успешно (статус 200)
     * - Выполняется в самом конце (priority 72)
     * 
     * ВАЖНО: Этот тест должен быть последним, так как удаляет клиента
     * Все тесты для widgets, methodConfigs, customerConfigs, layerDetails и обновления customer должны быть ПЕРЕД этим тестом
     */
    @Test(priority = 72, description = "DELETE customer")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Удаление созданного клиента. Проверка статуса 200.")
    @Story("Delete Customer")
    public void testDeleteCustomer() {
        logger.info("Выполнение теста: DELETE customer");
        
        // Проверяем что customerId был создан в первом тесте
        if (customerId == null) {
            logger.error("customerId не установлен! Нельзя удалить клиента.");
            throw new IllegalStateException("customerId должен быть установлен перед удалением");
        }
        
        // Формируем endpoint с ID клиента
        String deleteEndpoint = CUSTOMERS_ENDPOINT + "/" + customerId;
        
        given()
                .spec(getRequestSpec())
                .contentType(ContentType.JSON)
                .when()
                .delete(deleteEndpoint) // DELETE запрос на /customers/{id}
                .then()
                .statusCode(200); // Ожидаем успешное удаление
        
        logger.info("Customer успешно удален. ID: {}", customerId);
        
        // Опционально: очищаем переменную (можно раскомментировать если нужно)
        // customerId = null;
    }

    /**
     * МЕТОД ДЛЯ ПОЛУЧЕНИЯ ID СОЗДАННОГО КЛИЕНТА
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Возвращает ID клиента, созданного в первом тесте
     * - Может использоваться в других тестах или классах
     * - static - можно вызвать без создания объекта класса
     * 
     * @return customerId - ID созданного клиента или null если еще не создан
     */
    public static Integer getCustomerId() {
        return customerId;
    }
}
