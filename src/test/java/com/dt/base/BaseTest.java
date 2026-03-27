package com.dt.base;

// Импорты для работы с REST Assured - библиотека для тестирования REST API
import io.qameta.allure.restassured.AllureRestAssured; // Фильтр для интеграции REST Assured с Allure (для отчетов)
import io.restassured.RestAssured; // Основной класс REST Assured для настройки глобальных параметров
import io.restassured.builder.RequestSpecBuilder; // Строитель для создания спецификации запроса (шаблона запроса)
import io.restassured.config.LogConfig; // Конфигурация логирования
import io.restassured.config.RestAssuredConfig; // Конфигурация RestAssured
import io.restassured.http.Cookie; // Класс для работы с одной кукой
import io.restassured.http.Cookies; // Класс для работы с несколькими куками
import io.restassured.specification.RequestSpecification; // Интерфейс спецификации запроса (содержит настройки запроса)
// Импорты для логирования
import org.slf4j.Logger; // Интерфейс для логирования
import org.slf4j.LoggerFactory; // Фабрика для создания логгера

import java.io.PrintStream;
import java.io.OutputStream;

import java.util.ArrayList; // Список для хранения кук
import java.util.List; // Интерфейс списка

/**
 * БАЗОВЫЙ КЛАСС ДЛЯ ВСЕХ ТЕСТОВ API
 *
 * ЗАЧЕМ НУЖЕН ЭТОТ КЛАСС:
 * - Это родительский класс, от которого наследуются все тестовые классы
 * - Содержит общую функциональность для работы с REST API (настройки, куки, логирование)
 * - Избегаем дублирования кода - все общие настройки в одном месте
 * - Упрощает поддержку - если нужно изменить что-то в запросах, меняем только здесь
 *
 * КАК РАБОТАЕТ:
 * - Все тестовые классы наследуются от BaseTest (extends BaseTest)
 * - Наследуют все методы и переменные этого класса
 * - Могут использовать методы для установки URL, кук и т.д.
 */
public class BaseTest {

    /**
     * ЛОГГЕР - для записи информации о работе тестов
     *
     * ЗАЧЕМ НУЖЕН:
     * - Записывает информацию о выполнении тестов (что происходит, ошибки и т.д.)
     * - Помогает отлаживать тесты - видим что происходит на каждом шаге
     * - static final - одна общая переменная для всех экземпляров класса
     * - protected - доступна в классах-наследниках (в тестах)
     */
    protected static final Logger logger = LoggerFactory.getLogger(BaseTest.class);

    /**
     * СПЕЦИФИКАЦИЯ ЗАПРОСА - шаблон для всех HTTP запросов
     *
     * ЗАЧЕМ НУЖНА:
     * - Содержит общие настройки для всех запросов (куки, заголовки, фильтры)
     * - Не нужно каждый раз указывать куки в каждом тесте - они уже в спецификации
     * - protected - доступна в классах-наследниках
     *
     * ЧТО В НЕЙ ХРАНИТСЯ:
     * - Куки для аутентификации
     * - Фильтр Allure (для автоматического добавления запросов в отчеты)
     */
    protected RequestSpecification requestSpec;

    /**
     * КУКИ - для аутентификации в API
     *
     * ЗАЧЕМ НУЖНЫ:
     * - Многие API требуют куки для доступа (сессия, токен и т.д.)
     * - Храним куки здесь, чтобы использовать во всех тестах
     * - protected - доступна в классах-наследниках
     */
    protected Cookies cookies;

    /**
     * КОНСТРУКТОР - вызывается автоматически при создании объекта класса
     *
     * ЗАЧЕМ НУЖЕН:
     * - Инициализирует базовые настройки при создании тестового класса
     * - Настраивает логирование запросов (если тест упал - увидим запрос и ответ)
     * - Создает базовую спецификацию запроса с фильтром Allure
     *
     * КОГДА ВЫЗЫВАЕТСЯ:
     * - Автоматически при создании объекта тестового класса
     * - TestNG создает объект перед запуском тестов
     */
    public BaseTest() {
        // Логирование при падении теста — для остальных тестов (не использующих getRequestSpecNoLog)
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        // Создаем спецификацию запроса с фильтрами
        // ЗАЧЕМ: фильтры автоматически добавляют информацию о запросах в отчеты Allure
        // и маскируют чувствительные данные (куки, токены) в логах
        requestSpec = new RequestSpecBuilder()
                .addFilter(new AllureRestAssured()) // Фильтр для Allure отчетов
                .addFilter(new SensitiveDataFilter()) // Фильтр для маскировки чувствительных данных
                .build(); // Строим спецификацию
    }

    /**
     * УСТАНОВКА БАЗОВОГО URL
     *
     * ЗАЧЕМ НУЖЕН:
     * - Устанавливает базовый адрес API (например, https://example.com)
     * - После этого в тестах указываем только путь (например, /customers)
     * - protected - доступен только в этом классе и наследниках (не извне)
     *
     * КАК ИСПОЛЬЗОВАТЬ:
     * - В тестах вызываем: setBaseUrl("https://example.com")
     * - Потом делаем запрос: .post("/customers") - автоматически добавится базовый URL
     *
     * @param baseUrl базовый URL API (например, "https://example.com")
     */
    protected void setBaseUrl(String baseUrl) {
        // Устанавливаем базовый URL глобально для всех запросов REST Assured
        RestAssured.baseURI = baseUrl;
        // Логируем что URL установлен
        logger.info("Base URL установлен: {}", baseUrl);
    }

    /**
     * УСТАНОВКА КУК (нескольких сразу)
     *
     * ЗАЧЕМ НУЖЕН:
     * - Устанавливает несколько кук сразу (если их много)
     * - Обновляет спецификацию запроса, чтобы куки автоматически добавлялись к каждому запросу
     *
     * КАК ИСПОЛЬЗОВАТЬ:
     * - Если получили куки из ответа API, передаем их сюда
     * - Все последующие запросы будут автоматически содержать эти куки
     *
     * @param cookies объект Cookies с несколькими куками
     */
    protected void setCookies(Cookies cookies) {
        // Сохраняем куки в переменную класса
        this.cookies = cookies;

        // Пересоздаем спецификацию запроса с новыми куками
        // ЗАЧЕМ: чтобы куки автоматически добавлялись к каждому запросу
        requestSpec = new RequestSpecBuilder()
                .addFilter(new AllureRestAssured()) // Фильтр Allure
                .addFilter(new SensitiveDataFilter()) // Фильтр для маскировки чувствительных данных
                .addCookies(cookies) // Добавляем куки в спецификацию
                .build();

        // Маскируем куки в логах для безопасности
        logger.info("Cookies установлены: {}", SensitiveDataFilter.maskCookiesForLogging(cookies));
    }

    /**
     * УСТАНОВКА ОДНОЙ КУКИ
     *
     * ЗАЧЕМ НУЖЕН:
     * - Удобный способ установить одну куку по имени и значению
     * - Если уже есть куки - добавляет новую к существующим
     * - Если кук нет - создает новую коллекцию
     *
     * КАК ИСПОЛЬЗОВАТЬ:
     * - setCookie("cookie", "значение_куки")
     * - setCookie("sessionId", "abc123")
     *
     * @param cookieName имя куки (например, "cookie" или "sessionId")
     * @param cookieValue значение куки (то что нужно передать)
     */
    protected void setCookie(String cookieName, String cookieValue) {
        // Создаем объект Cookie из имени и значения
        Cookie cookie = new Cookie.Builder(cookieName, cookieValue).build();

        // Создаем список для хранения всех кук
        List<Cookie> cookieList = new ArrayList<>();

        // Если уже есть куки - добавляем их в список
        if (cookies != null) {
            cookieList.addAll(cookies.asList()); // Преобразуем Cookies в список и добавляем
        }

        // Добавляем новую куку в список
        cookieList.add(cookie);

        // Создаем новый объект Cookies из списка
        cookies = new Cookies(cookieList);

        // Пересоздаем спецификацию запроса с обновленными куками
        requestSpec = new RequestSpecBuilder()
                .addFilter(new AllureRestAssured())
                .addFilter(new SensitiveDataFilter()) // Фильтр для маскировки чувствительных данных
                .addCookies(cookies) // Добавляем все куки (старые + новая)
                .build();

        // Маскируем значение куки в логах для безопасности
        logger.info("Cookie установлена: {} = {}", cookieName, SensitiveDataFilter.maskForLogging(cookieValue));
    }

    /**
     * ПОЛУЧЕНИЕ СПЕЦИФИКАЦИИ ЗАПРОСА
     *
     * ЗАЧЕМ НУЖЕН:
     * - Возвращает готовую спецификацию запроса с куками и настройками
     * - Используется в тестах: .spec(getRequestSpec())
     * - Все настройки (куки, фильтры) автоматически применяются к запросу
     *
     * КАК ИСПОЛЬЗОВАТЬ В ТЕСТАХ:
     * given()
     *     .spec(getRequestSpec()) // Применяем все настройки (куки и т.д.)
     *     .when()
     *     .post("/customers")
     *
     * @return RequestSpecification - готовая спецификация с настройками
     */
    protected RequestSpecification getRequestSpec() {
        return requestSpec;
    }

    /**
     * ПОЛУЧЕНИЕ СПЕЦИФИКАЦИИ ЗАПРОСА БЕЗ ЛОГИРОВАНИЯ
     *
     * ЗАЧЕМ НУЖЕН:
     * - Для тестов с большими ответами (бэкапы, экспорт), где логирование засоряет консоль
     * - Полностью отключает логирование запросов и ответов
     * - Не включает AllureRestAssured фильтр
     *
     * @return RequestSpecification - спецификация без логирования
     */
    protected RequestSpecification getRequestSpecNoLog() {
        // Создаем "пустой" PrintStream, который ничего не выводит
        PrintStream nullPrintStream = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                // Ничего не делаем - все выводы игнорируются
            }
        });
        
        // Конфигурируем RestAssured без логирования
        RestAssuredConfig noLogConfig = RestAssuredConfig.config()
                .logConfig(LogConfig.logConfig()
                        .defaultStream(nullPrintStream)
                        .enablePrettyPrinting(false));
        
        RequestSpecBuilder builder = new RequestSpecBuilder();
        builder.setConfig(noLogConfig);
        if (cookies != null) {
            builder.addCookies(cookies);
        }
        return builder.build();
    }

    /**
     * ПОЛУЧЕНИЕ ТЕКУЩИХ КУК
     *
     * ЗАЧЕМ НУЖЕН:
     * - Возвращает все установленные куки
     * - Может понадобиться для проверки или передачи в другой запрос
     *
     * @return Cookies - объект со всеми куками
     */
    protected Cookies getCookies() {
        return cookies;
    }

    /**
     * ОЧИСТКА КУК
     *
     * ЗАЧЕМ НУЖЕН:
     * - Удаляет все установленные куки
     * - Может понадобиться для тестов, где нужно работать без аутентификации
     * - Или для сброса состояния между тестами
     */
    protected void clearCookies() {
        // Удаляем куки
        this.cookies = null;

        // Пересоздаем спецификацию без кук
        requestSpec = new RequestSpecBuilder()
                .addFilter(new AllureRestAssured())
                .build(); // Без .addCookies() - значит кук нет

        logger.info("Cookies очищены");
    }
}
