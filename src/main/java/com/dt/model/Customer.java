package com.dt.model;

// Импорты Jackson - библиотека для работы с JSON
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // Игнорировать неизвестные поля в JSON
import com.fasterxml.jackson.annotation.JsonProperty; // Указать имя поля в JSON

/**
 * МОДЕЛЬ КЛАССА CUSTOMER (КЛИЕНТ)
 * 
 * ЗАЧЕМ НУЖЕН ЭТОТ КЛАСС:
 * - Представляет данные о клиенте в виде Java объекта
 * - REST Assured автоматически преобразует этот объект в JSON при отправке запроса
 * - REST Assured автоматически преобразует JSON ответа в этот объект
 * - Упрощает работу с данными - работаем с объектами, а не со строками JSON
 * 
 * КАК РАБОТАЕТ:
 * - При отправке POST запроса: Customer объект -> JSON строка
 * - При получении ответа: JSON строка -> Customer объект
 * - Jackson библиотека делает преобразование автоматически
 * 
 * ПРИМЕР ИСПОЛЬЗОВАНИЯ:
 * Customer customer = new Customer("Имя", "код");
 * // Отправляем объект, REST Assured автоматически преобразует в JSON:
 * // {"name": "Имя", "code": "код"}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
// ЗАЧЕМ ЭТА АННОТАЦИЯ:
// - Говорит Jackson игнорировать поля из JSON, которых нет в этом классе
// - Если API вернет поле "email", а в классе его нет - не будет ошибки
// - Полезно когда API может возвращать дополнительные поля
public class Customer {
    
    /**
     * ID КЛИЕНТА
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Уникальный идентификатор клиента в базе данных
     * - Приходит в ответе после создания клиента
     * - Используется для дальнейших операций (обновление, удаление)
     * 
     * @JsonProperty("id") - говорит Jackson что поле "id" в JSON соответствует этой переменной
     * private - доступна только внутри класса (инкапсуляция)
     * Integer - тип данных (может быть null, в отличие от int)
     */
    @JsonProperty("id")
    private Integer id;
    
    /**
     * ИМЯ КЛИЕНТА
     * 
     * ЗАЧЕМ НУЖНО:
     * - Название/имя клиента
     * - Обязательное поле при создании клиента
     * 
     * @JsonProperty("name") - поле "name" в JSON соответствует этой переменной
     */
    @JsonProperty("name")
    private String name;
    
    /**
     * КОД КЛИЕНТА
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Уникальный код клиента
     * - Обязательное поле при создании клиента
     * - Используется для идентификации (нельзя создать двух клиентов с одним кодом)
     * 
     * @JsonProperty("code") - поле "code" в JSON соответствует этой переменной
     */
    @JsonProperty("code")
    private String code;

    /**
     * КОНСТРУКТОР БЕЗ ПАРАМЕТРОВ (пустой конструктор)
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Jackson требует пустой конструктор для создания объекта из JSON
     * - При получении ответа от API Jackson создает объект через этот конструктор
     * - Затем заполняет поля через setter методы
     * 
     * КОГДА ВЫЗЫВАЕТСЯ:
     * - Автоматически Jackson при преобразовании JSON -> Customer
     */
    public Customer() {
        // Пустой конструктор - Jackson заполнит поля через setters
    }

    /**
     * КОНСТРУКТОР С ПАРАМЕТРАМИ
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Удобный способ создать объект Customer с данными сразу
     * - Используется в тестах для создания клиента перед отправкой
     * 
     * ПРИМЕР:
     * Customer customer = new Customer("autotestCustomer", "autotest");
     * // Создан объект с name="autotestCustomer" и code="autotest"
     * 
     * @param name имя клиента
     * @param code код клиента
     */
    public Customer(String name, String code) {
        // this.name - поле класса (this.name)
        // name - параметр метода
        // Присваиваем значение параметра полю класса
        this.name = name;
        this.code = code;
        // id не устанавливаем - он придет от сервера после создания
    }

    /**
     * GETTER ДЛЯ ID
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Возвращает значение поля id
     * - Используется для получения id после создания клиента
     * - Jackson также использует getter при преобразовании объекта в JSON
     * 
     * GETTER/SETTER паттерн:
     * - Поля private (недоступны извне)
     * - Доступ через методы get/set (контролируемый доступ)
     * 
     * @return id клиента
     */
    public Integer getId() {
        return id;
    }

    /**
     * SETTER ДЛЯ ID
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Устанавливает значение поля id
     * - Jackson использует setter при преобразовании JSON -> Customer
     * - Вызывается автоматически когда Jackson видит поле "id" в JSON
     * 
     * @param id идентификатор клиента
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * GETTER ДЛЯ NAME
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Возвращает имя клиента
     * - Используется для чтения значения поля
     * 
     * @return имя клиента
     */
    public String getName() {
        return name;
    }

    /**
     * SETTER ДЛЯ NAME
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Устанавливает имя клиента
     * - Jackson использует при преобразовании JSON -> Customer
     * 
     * @param name имя клиента
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * GETTER ДЛЯ CODE
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Возвращает код клиента
     * 
     * @return код клиента
     */
    public String getCode() {
        return code;
    }

    /**
     * SETTER ДЛЯ CODE
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Устанавливает код клиента
     * - Jackson использует при преобразовании JSON -> Customer
     * 
     * @param code код клиента
     */
    public void setCode(String code) {
        this.code = code;
    }
}
