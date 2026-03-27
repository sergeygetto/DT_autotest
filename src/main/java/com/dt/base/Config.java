package com.dt.base;

/**
 * КЛАСС ДЛЯ РАБОТЫ С КОНФИГУРАЦИЕЙ И ПЕРЕМЕННЫМИ ОКРУЖЕНИЯ
 * 
 * ЗАЧЕМ НУЖЕН ЭТОТ КЛАСС:
 * - Читает настройки из переменных окружения или системных переменных
 * - Позволяет не хардкодить значения (куки, URL) в коде
 * - Можно менять настройки без изменения кода (удобно для разных окружений)
 * - Все методы static - можно вызывать без создания объекта
 * 
 * ПРИМЕРЫ ИСПОЛЬЗОВАНИЯ:
 * - Запуск тестов: mvn test -Dcookie="значение_куки"
 * - Или через переменную окружения: set cookie=значение_куки (Windows)
 * - Или: export cookie="значение_куки" (Linux/Mac)
 */
public class Config {
    
    /**
     * ПОЛУЧЕНИЕ КУКИ ИЗ ПЕРЕМЕННЫХ
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Читает значение куки из системной переменной (-Dcookie=value) или переменной окружения
     * - Позволяет не хранить куки в коде (безопасность)
     * - Можно передавать куку при запуске тестов
     * 
     * КАК РАБОТАЕТ:
     * 1. Сначала проверяет системную переменную (передается через -D при запуске)
     * 2. Если не найдена - проверяет переменную окружения ОС
     * 3. Если ничего не найдено - возвращает null
     * 
     * ПРИМЕРЫ ЗАПУСКА:
     * - mvn test -Dcookie="my-cookie-value" (системная переменная)
     * - set cookie=my-cookie-value && mvn test (переменная окружения Windows)
     * - export cookie="my-cookie-value" && mvn test (переменная окружения Linux/Mac)
     * 
     * @return значение куки или null, если не установлена
     */
    public static String getCookie() {
        // System.getProperty() - читает системную переменную Java
        // Передается через -D при запуске: mvn test -Dcookie="value"
        // ЗАЧЕМ: удобно передавать при запуске тестов
        String cookie = System.getProperty("cookie");
        
        // Если системная переменная найдена и не пустая - возвращаем её
        if (cookie != null && !cookie.isEmpty()) {
            return cookie;
        }
        
        // System.getenv() - читает переменную окружения операционной системы
        // Устанавливается в ОС: set cookie=value (Windows) или export cookie=value (Linux)
        // ЗАЧЕМ: можно установить один раз в системе и использовать везде
        cookie = System.getenv("cookie");
        
        // Если переменная окружения найдена и не пустая - возвращаем её
        if (cookie != null && !cookie.isEmpty()) {
            return cookie;
        }
        
        // Если ничего не найдено - возвращаем null
        // В тестах нужно проверить на null перед использованием
        return null;
    }
    
    /**
     * ПОЛУЧЕНИЕ БАЗОВОГО URL ИЗ ПЕРЕМЕННЫХ
     * 
     * ЗАЧЕМ НУЖЕН:
     * - Позволяет менять URL API без изменения кода
     * - Удобно для работы с разными окружениями (dev, test, prod)
     * - Если переменная не установлена - использует значение по умолчанию
     * 
     * КАК РАБОТАЕТ:
     * 1. Сначала проверяет системную переменную (-DbaseUrl=value)
     * 2. Если не найдена - проверяет переменную окружения
     * 3. Если ничего не найдено - возвращает значение по умолчанию
     * 
     * ПРИМЕРЫ ИСПОЛЬЗОВАНИЯ:
     * - Config.getBaseUrl("https://default-url.com") - вернет default-url если переменная не установлена
     * - mvn test -DbaseUrl="https://test-url.com" - переопределит URL
     * 
     * @param defaultUrl URL по умолчанию (используется если переменная не установлена)
     * @return базовый URL (из переменной или значение по умолчанию)
     */
    public static String getBaseUrl(String defaultUrl) {
        // Проверяем системную переменную baseUrl
        // ЗАЧЕМ: можно переопределить URL при запуске тестов
        String url = System.getProperty("baseUrl");
        
        // Если найдена и не пустая - возвращаем
        if (url != null && !url.isEmpty()) {
            return url;
        }
        
        // Проверяем переменную окружения baseUrl
        url = System.getenv("baseUrl");
        
        // Если найдена и не пустая - возвращаем
        if (url != null && !url.isEmpty()) {
            return url;
        }
        
        // Если ничего не найдено - возвращаем значение по умолчанию
        // ЗАЧЕМ: тесты будут работать даже если переменная не установлена
        return defaultUrl;
    }

    /**
     * Получение URL для API справочников (dictionaries).
     * Читает -DdictionariesUrl или переменную окружения dictionariesUrl.
     *
     * @param defaultUrl URL по умолчанию
     * @return URL для dictionaries API
     */
    public static String getDictionariesUrl(String defaultUrl) {
        String url = System.getProperty("dictionariesUrl");
        if (url != null && !url.isEmpty()) {
            return url;
        }
        url = System.getenv("dictionariesUrl");
        if (url != null && !url.isEmpty()) {
            return url;
        }
        return defaultUrl;
    }
}
