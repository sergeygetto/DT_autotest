# План: сырой JSON и расширенная проверка features в Query-тестах

## Текущее состояние

| Проблема | Количество | Где |
|----------|------------|-----|
| Сырой JSON (конкатенация строк для тела запроса) | ~20 мест | Негативные тесты getObjectsByCustomerConfig, getObjectsByPolygon, getAggregatedObjectsByCoordinates и др. |
| Расширенная проверка features (цикл в самом тесте) | 12 мест | getObjectsByCoordinates (centroid, distance, objectId, properties), getAggregatedObjectsByCoordinates (centroid, вложенность MultiPolygon) |

---

## 1. Сырой JSON → модели и фабрики

### Что сделать

**Вариант A (предпочтительный):** везде, где есть POJO для запроса, собирать тело через объект и `.body(request)`. Для негативных кейсов («без поля X») — фабрики или билдеры.

**Вариант B:** вынести JSON-строки в тестовые ресурсы (`src/test/resources/request-bodies/`) и читать файл в тесте. Меньше типобезопасности, но проще править JSON вручную.

### Конкретные шаги

1. **getObjectsByCustomerConfig (негативные)**  
   Уже есть: `GetObjectsByCustomerConfigRequest`, `MainLayer`, `PageRequest`.  
   - Для «без customerCode»: собрать запрос без `customerCode` (отдельный фабричный метод или билдер с `customerCode = null` и не сериализовать null).  
   - Для «без methodType»: то же через модель.  
   - Для «невалидный JSON»: оставить строку или один файл `invalid.json`.

2. **getObjectsByPolygon (негативные)**  
   Есть: `GetObjectsByPolygonRequest`, `Layer`, `PageRequest`.  
   - Тесты «без layers», «без coordinates», «пустой layers» и т.п. — строить через POJO (с пустыми списками, null там, где нужно «без поля»).  
   - Использовать `@JsonInclude(JsonInclude.Include.NON_NULL)` в моделях, чтобы не передавать null в JSON.

3. **getAggregatedObjectsByCoordinates (негативные)**  
   Есть: `GetAggregatedObjectsByCoordinatesRequest`, `MainLayer`, `PageRequest`.  
   - Аналогично: «без mainLayer», «без coordinates» — через объекты и фабрики/билдеры.

4. **Фабрики тестовых данных (рекомендуется)**  
   Создать класс, например `QueryIntersectRequestFactory`, в `src/test/java` с методами:
   - `getObjectsByCustomerConfigWithoutCustomerCode()`
   - `getObjectsByCustomerConfigWithoutMethodType()`
   - `getObjectsByPolygonWithoutLayers()`
   - и т.д.  
   Возвращают готовый объект запроса (или DTO), который REST Assured сериализует в JSON. Тогда в тесте: `.body(QueryIntersectRequestFactory.getObjectsByCustomerConfigWithoutCustomerCode())`.

### Пример замены сырого JSON на модель

**Было:**
```java
String requestBody = "{\n"
    + "  \"customerCode\": \"" + CUSTOMER_CODE + "\",\n"
    + "  \"mainLayer\": { \"layerId\": \"\", \"properties\": [ \"string\" ] },\n"
    + "  \"geometry\": \"" + POLYGON_GEOMETRY + "\",\n"
    + "  \"pageRequest\": { \"pageNumber\": 0, \"pageSize\": 10000 }\n"
    + "}";
given().spec(getRequestSpec()).contentType(ContentType.JSON).body(requestBody)...
```

**Стало:**
```java
GetObjectsByCustomerConfigRequest request = GetObjectsByCustomerConfigRequest.builder()
    .customerCode(CUSTOMER_CODE)
    .mainLayer(new MainLayer("", List.of("string")))
    .geometry(POLYGON_GEOMETRY)
    .methodType(METHOD_TYPE_POLYGON)
    .pageRequest(new PageRequest(0, 10000))
    .build();
given().spec(getRequestSpec()).contentType(ContentType.JSON).body(request)...
```

Если у моделей нет билдера — добавить Lombok `@Builder` или написать фабричные методы в `QueryIntersectRequestFactory`.

---

## 2. Расширенная проверка features → вынести в хелперы

### Что сделать

Сейчас в тестах остаётся **12 циклов** по `features` с разной логикой:

| Тип ответа | Что проверяется | Действие |
|------------|------------------|----------|
| getObjectsByCoordinates (с returnCentroid) | type, layerId, **centroid**, **distance**, geometry, **objectId**, **properties**, **correlationId** | Вынести в метод `assertFeatureWithCentroidAndDistance(features)` в базовый класс |
| getAggregatedObjectsByCoordinates (CONTAINS, WITHIN, UNION, …) | type=Feature, layerId, **centroid**, geometry (MultiPolygon), **вложенность coordinates** | Вынести в метод `assertAggregatedFeatureStructure(features)` в базовый класс |

### Конкретные шаги

1. **В `QueryIntersectTestBase` добавить два метода:**

   - **`assertFeatureWithCentroidAndDistance(List<Map<String, Object>> features)`**  
     Проверки: feature не null, type, layerId, centroid (type Point, coordinates), distance (Number), geometry (type, coordinates), objectId, properties (id и т.д.), correlationId.  
     Использовать в тестах getObjectsByCoordinates, где ожидается такая структура (примерно строки 1438–1498 и аналоги).

   - **`assertAggregatedFeatureStructure(List<Map<String, Object>> features)`**  
     Проверки: feature не null, type = "Feature", layerId, centroid (Point, coordinates size 2), geometry (MultiPolygon, coordinates), при необходимости — вложенность координат (несколько уровней List).  
     Использовать во всех тестах getAggregatedObjectsByCoordinates с расширенной проверкой (строки 2569–2602, 2684–2701, и т.д.).

2. **В тестах заменить цикл на вызов метода.**

   Было:
   ```java
   List<Map<String, Object>> features = response.jsonPath().getList("content.features");
   for (int i = 0; i < features.size(); i++) {
       Map<String, Object> feature = features.get(i);
       Assert.assertNotNull(feature, ...);
       Assert.assertNotNull(feature.get("centroid"), ...);
       // ... ещё 20 строк
   }
   ```

   Стало:
   ```java
   List<Map<String, Object>> features = response.jsonPath().getList("content.features");
   assertFeatureWithCentroidAndDistance(features);  // или assertAggregatedFeatureStructure(features)
   ```

3. **Различия между тестами.**  
   Если в одном-двух тестах проверяется что-то уникальное (например, только в одном тесте — вложенность координат), можно:
   - оставить одну дополнительную проверку в тесте после вызова общего метода, или  
   - добавить параметр в хелпер (например, `boolean checkNestedCoordinates`).

---

## Порядок работ (рекомендуемый)

1. Добавить в **QueryIntersectTestBase** методы **assertFeatureWithCentroidAndDistance** и **assertAggregatedFeatureStructure** и заменить на них все 12 расширенных циклов в тестах — это сразу уберёт дублирование и упростит поддержку.
2. Ввести **QueryIntersectRequestFactory** (или аналогичное имя) и перенести туда 2–3 негативных кейса getObjectsByCustomerConfig через модели — как образец.
3. Постепенно заменять остальной сырой JSON на фабрики/модели (по одному эндпоинту или группе тестов).

После шага 1 оценка «поддерживаемость» и «читаемость» тестов уже заметно вырастет; после шагов 2–3 тесты станут проще менять при изменении API.
