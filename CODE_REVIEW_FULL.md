# Полное ревью кода DT_autotest

Проведён пофайловый обзор всех Java-классов проекта (база, тесты, модели).

---

## 1. Базовые классы и конфигурация

### 1.1 BaseTest (`src/test/java/com/dt/base/BaseTest.java`)

**Плюсы:**
- Единая точка настройки запросов: AllureRestAssured + SensitiveDataFilter, куки, baseUrl.
- `setBaseUrl`, `setCookie`/`setCookies`, `getRequestSpec()`, `clearCookies()` — понятный API.
- `getRequestSpecNoLog()` для тяжёлых ответов (бэкапы, экспорт) — логи не забиваются.
- В конструкторе включено `RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()` — при падении теста виден запрос/ответ.

**Замечания:**
- `getRequestSpecNoLog()` каждый раз создаёт новый `PrintStream` и не добавляет AllureRestAssured/SensitiveDataFilter в билдер. Куки подставляются, но запросы без логов не попадают в Allure и не маскируются фильтром. Для продакшена при необходимости можно вынести NULL_STREAM в поле и добавить те же фильтры в билдер (без логирования тела).
- В комментарии указан «пустой PrintStream», по факту создаётся ещё один анонимный OutputStream в методе — можно переиспользовать `NULL_STREAM`.

### 1.2 Config (`src/main/java/com/dt/base/Config.java`)

**Плюсы:**
- Чтение cookie, baseUrl, dictionariesUrl из `-D` и env — без хардкода секретов.
- Приоритет: системные свойства, затем переменные окружения, для URL — дефолт.
- JavaDoc с примерами запуска.

**Замечания:** Нет.

### 1.3 SensitiveDataFilter (`src/main/java/com/dt/base/SensitiveDataFilter.java`)

**Плюсы:**
- Маскировка заголовков Cookie/Set-Cookie в запросе перед отправкой (через `maskCookieValue`).
- `maskForLogging` / `maskCookiesForLogging` для безопасного логирования.

**Замечания:**
- В блоке обработки ответа (строки 76–87) цикл по заголовкам ответа только проверяет имя, но не подменяет значение и не записывает маскированную версию. То есть Set-Cookie в ответах при логировании RestAssured могут уйти в лог без маскировки. Имеет смысл либо реально подменять заголовки ответа на маскированные, либо явно задокументировать, что маскируются только запросы.

---

## 2. Тестовые классы

### 2.1 CustomerApiTest (`src/test/java/com/dt/tests/CustomerApiTest.java`)

**Объём:** 72 теста (priority 1–72), Customer / Widget / MethodConfig / CustomerConfig / LayerDetails.

**Плюсы:**
- Последовательность: создание → дубликаты и негативы → GET/PUT/DELETE → снова негативы. Понятный порядок.
- Использование моделей (Customer, Widget, WidgetData, MethodConfig, CustomerConfig) вместо голых JSON-строк где уместно.
- Allure: @Epic, @Feature, @Story, @Severity, @Description.
- Константы эндпоинтов и тестовых данных вынесены (CUSTOMER_CODE, WIDGET_CODE и т.д.).

**Замечания:**
- **Cookie в setUp:** при отсутствии cookie только `logger.warn`, тесты не падают сразу. В LayerStyleApiTest, QueryIntersectTestBase, MetricsApiTest при отсутствии cookie кидается исключение в @BeforeClass. Имеет смысл унифицировать: либо везде падать в setUp, либо везде предупреждать и падать при первом запросе (как сейчас в Metrics — checkNotLoginPage).
- В тестах 11 (testGetWidgetsByCustomerCode) и др. используется Java `assert` (например `assert widgetFound`, `assert "Тестовый блок".equals(name)`). При запуске без `-ea` эти проверки не выполняются. Надёжнее везде использовать `Assert.assertTrue(..., "message")` и т.п. из TestNG.

### 2.2 MetricsApiTest (`src/test/java/com/dt/tests/MetricsApiTest.java`)

**Объём:** 35 тестов (priority 1–35), метрики, сессии, metrics-info.

**Плюсы:**
- В первом тесте (и в setUp по сути) явная проверка cookie и `IllegalStateException` при её отсутствии.
- Хелпер `checkNotLoginPage(responseBody)` — при ответе-HTML логина кидается понятная ошибка с подсказкой про cookie.
- Модели метрик (Session, Summary, Stage, Environment и т.д.) с Jackson, переиспользуются в нескольких тестах.
- Хелперы `createBasicSession`, `createBasicSummary`, `createBasicStage` уменьшают дублирование.
- Нормализация дат для сравнения (`normalizeDateForComparison`) учтена под формат бэкенда.

**Замечания:**
- В @BeforeClass cookie проверяется только логированием ошибки, без throw (в отличие от первого теста). То есть при пустой cookie класс инициализируется, а падение происходит уже в тесте 1. Можно в setUp сразу кидать исключение, как в LayerStyleApiTest, чтобы быстрее падать и не путать с другими ошибками.

### 2.3 QueryIntersectTestBase (`src/test/java/com/dt/tests/QueryIntersectTestBase.java`)

**Плюсы:**
- Общие константы (URL, эндпоинты, геометрии, CUSTOMER_CODE), один setUp с проверкой cookie и throw.
- Хелперы проверки структуры ответов: `assertFeatureCollectionStructure`, `assertUserRequestHistoryFeaturesStructure`, `assertFeatureWithCentroidAndDistance`, `assertAggregatedFeatureStructure`, `assertAggregatedFeaturesPropertiesIdAndPolygonsClosed`, `assertPolygonsClosed` — уменьшают дубли и дают единообразные сообщения об ошибках.
- `getRequestSpecWithAutotestCookie()` — опциональная вторая кука для теста «доступ запрещён»; при отсутствии возвращается null, тест сам решает skip или fallback.

**Замечания:**
- `lastRequestId` — static Integer, заполняется в тесте 67, используется в тесте 71. При пропуске теста 67 тест 71 кидает SkipException. Это нормально, но зависимость между тестами стоит иметь в виду при изменении порядка или отключении тестов.
- В `getRequestSpecWithAutotestCookie()` не задаётся baseUri в билдере — используется глобальный RestAssured.baseURI из setUp. Это корректно.

### 2.4 QueryIntersectApiTest (`src/test/java/com/dt/tests/QueryIntersectApiTest.java`)

**Объём:** 78 тестов (priority 1–78), getObjectsByCustomerConfig, getObjectsByPolygon, getObjectsByCoordinates, getAggregatedObjectsByCoordinates, methodConfigs, userRequestHistory.

**Плюсы:**
- DataProvider для негативов с отсутствующими полями (`missingRequiredFieldGetObjectsByCustomerConfig`).
- Широкое использование моделей запросов (GetObjectsByCustomerConfigRequest, GetObjectsByPolygonRequest, GetObjectsByCoordinatesPostRequest, GetAggregatedObjectsByCoordinatesRequest, Layer, MainLayer, PageRequest, UserRequestHistoryListRequest, SortRequest и т.д.).
- Условный тест с другой кукой (testGetObjectsByPolygonWithDifferentCookies): при отсутствии cookie_autotest — SkipException с подсказкой по PowerShell.
- Проверки структуры через базовые хелперы (assertFeatureCollectionStructure и др.).

**Замечания:**
- Файл очень большой (4054 строки). При добавлении новых сценариев можно выносить группы тестов в отдельные классы (например, по эндпоинтам) или в наследники QueryIntersectTestBase с общим setUp.
- Жёстко заданные координаты полигонов (константы в базе и в тестах) — для разных окружений при необходимости можно вынести в конфиг или properties.

### 2.5 LayerStyleApiTest (`src/test/java/com/dt/tests/LayerStyleApiTest.java`)

**Объём:** 119 тестов (priority 1–112+), tree-layers, lock, icons, markers, maintenance/backup, layer-tags, data-hub, websocket, permissions.

**Плюсы:**
- Чёткая нумерация и описание (в комментариях и Allure).
- Константы LAYER_ID, GROUP_ID, эндпоинты, DEFAULT_BASE_URL, отдельный DEFAULT_DICTIONARIES_URL для справочников.
- Для тяжёлых ответов используется `getRequestSpecNoLog()` (backup, export, GET /tree-layers после restore).
- В @BeforeClass при отсутствии cookie — AssertionError с подсказкой.

**Замечания:**
- **Thread.sleep(8000)** в testVerifyLayerLabelRestoredAfterBackup (тест 64) — хрупко при медленном restore. Лучше polling: повторять GET /tree-layers с таймаутом (например, до 30 сек с шагом 2 сек) и выходить, как только слой появится с ожидаемым label.
- Длинная цепочка зависимостей от одного набора данных (слой/группа с фиксированными UUID): при падении одного из ранних тестов каскадно падают следующие. По возможности можно ослабить (например, создавать слой в начале блока тестов backup/tags или использовать данные из предыдущего GET), но это уже рефакторинг.
- Жёстко прописанные URL в теле запросов (STORAGE_ICONS_BASE, STORAGE_MARKERS_BASE, и полные URL в негативных тестах с test.png) — для смены окружения нужно менять константы или выносить в Config.

---

## 3. Модели (model)

Просмотрены: Customer, Widget, WidgetData, Session, GetObjectsByPolygonRequest, а также косвенно остальные через импорты и использование.

**Плюсы:**
- Единый стиль: Jackson @JsonProperty, при необходимости @JsonIgnoreProperties(ignoreUnknown = true), getters/setters, конструкторы (в т.ч. пустой для десериализации).
- Модели используются в тестах для сборки тел запросов и разбора ответов (где уместно), меньше строковых JSON.

**Замечания:**
- В Session и других моделях метрик много полей; при изменении контракта API нужно обновлять модели — отдельно не ревьюировались на полное соответствие текущему API (нет контрактов в репозитории).
- GetObjectsByPolygonRequest, GetObjectsByCustomerConfigRequest и т.д. — структура соответствует использованию в QueryIntersectApiTest; замечаний по стилю нет.

---

## 4. Ресурсы и конфигурация

### testng.xml
- Помечен как устаревший (в комментарии), содержит только часть тестов (CustomerApiTest). Полный прогон — через `mvn test`. Для выборочного запуска можно обновить suite или оставить как есть с явной оговоркой в README.

### pom.xml
- Java 11, TestNG, REST Assured 5.5.0, Allure, Jackson, SLF4J. Версии зафиксированы.
- Surefire: aspectjweaver для Allure, testFailureIgnore=false. Retry или повтор при падении не настроены — при желании можно добавить TestNG retry analyzer для нестабильных тестов.

### README.md
- Описание стека, структуры, запуска, переменных (cookie, baseUrl), безопасности (маскировка куки). Указано «72+ тестов» — по факту тестов значительно больше (Customer 72, Metrics 35, QueryIntersect 78, LayerStyle 119 и т.д.). Имеет смысл обновить число и перечень суит (Customer, Metrics, QueryIntersect, LayerStyle).

---

## 5. Сводка по критичным и полезным доработкам

| Приоритет | Где | Что |
|-----------|-----|-----|
| Высокий | CustomerApiTest | Заменить Java `assert` на TestNG `Assert.*` (тесты 11 и др.), иначе при отключённых assert проверки не выполняются. |
| Высокий | CustomerApiTest | Унифицировать поведение при отсутствии cookie: в @BeforeClass кидать исключение (как в LayerStyleApiTest / QueryIntersectTestBase), а не только warn. |
| Средний | SensitiveDataFilter | Доделать маскировку Set-Cookie в ответе (реально подменять заголовки ответа на маскированные или явно задокументировать ограничение). |
| Средний | LayerStyleApiTest | Заменить Thread.sleep(8000) в тесте 64 на polling GET /tree-layers с таймаутом. |
| Средний | BaseTest | В getRequestSpecNoLog() при необходимости добавить AllureRestAssured и SensitiveDataFilter (без логирования тела), чтобы запросы попадали в отчёт и маскировались. |
| Низкий | README | Обновить количество и список тестов/суит. |
| Низкий | MetricsApiTest | В @BeforeClass при отсутствии cookie сразу throw, как в других суитах. |
| Низкий | BaseTest | В getRequestSpecNoLog() переиспользовать один NULL_STREAM вместо создания нового PrintStream. |

---

## 6. Итог

- Архитектура и разделение ролей (BaseTest, Config, SensitiveDataFilter, модели, суиты) выглядят хорошо; код готов к использованию в dev/test и к доработкам под прод.
- Безопасность: секреты не в коде, маскировка в запросах есть; ответы в SensitiveDataFilter стоит доработать или явно описать.
- Стабильность: один явно хрупкий момент — фиксированная пауза 8 сек в LayerStyleApiTest; остальное — унификация проверки cookie и замена assert на Assert.
- Документация: README и JavaDoc в порядке; актуализировать число тестов и при необходимости описать ограничение по маскировке ответов.

После внесения правок из таблицы выше проект можно считать готовым к использованию в том числе на прод-окружении (с передачей cookie/baseUrl через CI/секреты и без деструктивных тестов на прод, если так заложено политикой).
