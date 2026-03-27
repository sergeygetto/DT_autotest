# DT Autotest Project

Проект для автоматизации тестирования API с использованием Java 11, Maven, TestNG, REST Assured и Allure.

## ⚠️ Важная информация о безопасности

**Проект включает защиту от утечки чувствительных данных:**
- Все куки и токены автоматически маскируются в логах и отчетах Allure
- Используется `SensitiveDataFilter` для фильтрации чувствительных данных
- Значения кук заменяются на `***MASKED***` в логах
- Это критично для безопасности - предотвращает компрометацию учетных записей

## Технологический стек

- **Java 11**
- **Maven**
- **TestNG 7.10.2**
- **REST Assured 5.5.0**
- **Allure 2.29.0**
- **Jackson 2.18.0**
- **SLF4J 2.0.9**

## Структура проекта

```
DT_autotest/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── dt/
│   │               ├── base/
│   │               │   ├── BaseTest.java          # Базовый класс для работы с REST Assured и куками
│   │               │   ├── Config.java            # Класс для работы с конфигурацией
│   │               │   └── SensitiveDataFilter.java  # Фильтр для маскировки чувствительных данных
│   │               └── model/
│   │                   ├── Customer.java          # Модель Customer
│   │                   ├── widget/                # Модели для Widgets
│   │                   ├── methodconfig/          # Модели для MethodConfigs
│   │                   └── customerconfig/        # Модели для CustomerConfigs
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── dt/
│       │           └── tests/
│       │               ├── CustomerApiTest.java       # Customer, Widget, MethodConfig, CustomerConfig, LayerDetails
│       │               ├── MetricsApiTest.java         # Метрики и сессии
│       │               ├── QueryIntersectTestBase.java # База для Query-Intersect
│       │               ├── QueryIntersectApiTest.java # getObjectsBy*, methodConfigs, userRequestHistory
│       │               └── LayerStyleApiTest.java     # tree-layers, lock, icons, markers, backup, layer-tags
│       └── resources/
│           ├── testng.xml                     # Конфигурация TestNG
│           └── allure.properties             # Конфигурация Allure
├── pom.xml                                    # Maven конфигурация
└── README.md
```

## Установка и запуск

### 1. Установка зависимостей

```bash
mvn clean install
```

### 2. Запуск тестов

**Важно:** Перед запуском тестов необходимо установить куку через переменную окружения или системную переменную.

#### Настройка переменных

**Кука (обязательно):**
```bash
# Windows PowerShell
$env:cookie="your-cookie-value"

# Windows CMD
set cookie=your-cookie-value

# Linux/Mac
export cookie="your-cookie-value"

# Или через системную переменную Maven
mvn test -Dcookie="your-cookie-value"
```

**Base URL (опционально, по умолчанию: `https://example.com`):**
```bash
# Windows PowerShell
$env:baseUrl="https://your-api-url.com"

# Windows CMD
set baseUrl=https://your-api-url.com

# Linux/Mac
export baseUrl="https://your-api-url.com"

# Или через системную переменную Maven
mvn test -DbaseUrl="https://your-api-url.com"
```

#### Запуск всех тестов
```bash
mvn test
```

#### Запуск через testng.xml (выборочные тесты)
```bash
mvn test -Dsurefire.suiteXmlFiles=src/test/resources/testng.xml
```

**Примечание:** `testng.xml` содержит устаревшую конфигурацию с частью тестов. Для запуска всех тестов используйте `mvn test`.

**Скрипты (PowerShell):** в корне проекта есть `run-layer-style-tests.ps1` (запуск только LayerStyleApiTest с cookie из `$env:cookie`), `run-tests-with-cookie.ps1`, `run-all-tests-two-cookies.ps1` — для запуска с двумя куками (основная и `cookie_autotest`).

### 3. Генерация Allure отчета

```bash
mvn allure:serve
```

## Использование

### Базовый класс (BaseTest)

Базовый класс `BaseTest` предоставляет следующие методы:

- `setBaseUrl(String baseUrl)` - установка базового URL
- `setCookies(Cookies cookies)` - установка кук
- `setCookie(String cookieName, String cookieValue)` - установка одной куки
- `getRequestSpec()` - получение спецификации запроса с куками
- `getCookies()` - получение текущих кук
- `clearCookies()` - очистка кук

### Пример использования в тестах

```java
@Test
public void testWithCookies() {
    // Установка куки
    setCookie("sessionId", "abc123");
    
    // Выполнение запроса
    Response response = given()
            .spec(getRequestSpec())
            .when()
            .get("/endpoint")
            .then()
            .statusCode(200)
            .extract()
            .response();
}
```

## Реализованные тесты

Проект содержит **300+ тестов** в четырёх тест-классах:

| Класс | Тестов | API |
|-------|--------|-----|
| CustomerApiTest | 72 | Customer, Widget, MethodConfig, CustomerConfig, LayerDetails |
| MetricsApiTest | 35 | Метрики, сессии, metrics-info |
| QueryIntersectApiTest | 78 | getObjectsByCustomerConfig, getObjectsByPolygon, getObjectsByCoordinates, getAggregatedObjectsByCoordinates, methodConfigs, userRequestHistory |
| LayerStyleApiTest | 119 | tree-layers, lock, icons, markers, maintenance/backup, layer-tags, data-hub, websocket, permissions |

**Запуск отдельной суиты:** `mvn test -Dtest=CustomerApiTest` (или MetricsApiTest, QueryIntersectApiTest, LayerStyleApiTest). Для LayerStyle можно использовать скрипт `run-layer-style-tests.ps1` (подставляет cookie из переменной окружения, если задана).

### 1. Customer API (Клиенты)
- ✅ Создание клиента (POST)
- ✅ Создание дубликата клиента (NEGATIVE)
- ✅ Создание клиента без обязательных полей (NEGATIVE)
- ✅ Создание клиента с невалидным JSON (NEGATIVE)
- ✅ Удаление клиента (DELETE)

### 2. Widget API (Виджеты)
- ✅ Создание виджета (POST)
- ✅ Создание дубликата виджета (NEGATIVE)
- ✅ Получение виджетов по customerCode (GET)
- ✅ Получение виджета по коду (GET)
- ✅ Обновление виджета (PUT)
- ✅ Удаление виджета (DELETE)
- ✅ Негативные тесты (несуществующие данные, невалидные запросы)

### 3. MethodConfig API (Конфигурации методов)
- ✅ Создание конфигурации метода (POST)
- ✅ Создание дубликата конфигурации (NEGATIVE)
- ✅ Получение конфигураций по customerCode (GET)
- ✅ Получение конфигурации по типу метода (GET)
- ✅ Обновление конфигурации (PUT)
- ✅ Удаление конфигурации (DELETE)
- ✅ Негативные тесты (несуществующие данные, невалидные запросы)

### 4. CustomerConfig API (Конфигурации клиентов)
- ✅ Создание конфигурации клиента (POST)
- ✅ Получение конфигурации клиента (GET)
- ✅ Создание конфигурации с невалидным UUID (NEGATIVE)
- ✅ Негативные тесты (несуществующие данные, невалидные запросы)

### 5. LayerDetails API (Детали слоев)
- ✅ Получение деталей слоев (GET)

### 6. Metrics API (Метрики)
- ✅ Очистка и чтение метрик, отправка метрик
- ✅ Сессии: start, summary, stage, env, finish
- ✅ metrics-info: search, users, session, presets, enigma, archive
- ✅ Негативные тесты (невалидный JSON, отсутствующие поля)

### 7. Query-Intersect API (Объекты по конфигурации и геометрии)
- ✅ getObjectsByCustomerConfig (polygon, aggregated, point, radius)
- ✅ getObjectsByPolygon, getObjectsByCoordinates, getAggregatedObjectsByCoordinates
- ✅ methodConfigs (GET/PUT), userRequestHistory (list, get by id)
- ✅ Негативные тесты (валидация полей, геометрии, pageRequest)

### 8. LayerStyle API (Слои и стили)
- ✅ tree-layers (POST/PUT/GET/DELETE, change-position, get-settings, create-model, display-tree)
- ✅ lock (acquire/release), icons, markers, maintenance (backup/restore/export)
- ✅ layer-tags, data-hub, websocket-connection, permissions

Все тесты выполняются в строгом порядке (priority 1-N) и включают:
- ✅ Позитивные тесты (успешные сценарии)
- ✅ Негативные тесты (валидация ошибок)
- ✅ Проверки статус-кодов
- ✅ Проверки структуры ответов
- ✅ Проверки бизнес-логики

## API Endpoints

**Base URL** задаётся переменной окружения `baseUrl` или `-DbaseUrl`. В репозитории по умолчанию используется безопасная заглушка `https://example.com` — для реального запуска укажите URL вашего стенда.

Дополнительно для LayerStyle можно задать `dictionariesUrl` (по умолчанию `https://example.com`). Для теста с «другой кукой» в QueryIntersectApiTest используется переменная `cookie_autotest`.

### Основные группы endpoints

1. **Manager Intersect Object** (Customer, Widget, MethodConfig, CustomerConfig, LayerDetails)  
   Префикс: `/da-cm-map-backend-manager-intersect-object/` — customers, widgets, methodConfigs, customerConfigs, layerDetails.

2. **Metrics**  
   Base: `.../da-cm-map-backend-pushmetric` — /metrics, /metrics/read, /metrics/session/*, /metrics-info/*.

3. **Query-Intersect Object**  
   Префиксы: `/da-cm-map-backend-query-intersect-object/` (geoData/v2/*, methodConfigs, userRequestHistory).

4. **LayerStyle**  
   Base: `.../da-cm-map-backend-layerstyle` — /tree-layers, /lock, /icons, /markers, /maintenance/*, /layer-tags, /data-hub, /websocket-connection, /permissions.

**Требуется:** Cookie для аутентификации (переменная окружения `cookie` или `-Dcookie`)

## Безопасность

### Защита чувствительных данных

Проект включает автоматическую маскировку чувствительных данных:

- ✅ **Куки и токены** автоматически маскируются в логах
- ✅ **Фильтр SensitiveDataFilter** перехватывает все запросы/ответы
- ✅ **Значения кук** заменяются на `***MASKED***` в логах и отчетах
- ✅ **Allure отчеты** не содержат реальных значений кук

Это критично для безопасности и предотвращает:
- Компрометацию учетных записей
- Несанкционированный доступ к API
- Нарушение политики безопасности

### Конфигурация через переменные окружения

Все чувствительные данные настраиваются через переменные окружения:
- `cookie` - кука для аутентификации
- `baseUrl` - базовый URL API (опционально)

Никакие чувствительные данные не хранятся в коде!
