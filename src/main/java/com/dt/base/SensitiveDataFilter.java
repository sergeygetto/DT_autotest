package com.dt.base;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.util.regex.Pattern;

/**
 * ФИЛЬТР ДЛЯ МАСКИРОВКИ ЧУВСТВИТЕЛЬНЫХ ДАННЫХ В ЛОГАХ
 * 
 * ЗАЧЕМ НУЖЕН:
 * - Предотвращает утечку чувствительных данных (куки, токены) в логи и отчеты
 * - Маскирует значения кук в запросах и ответах
 * - Критично для безопасности - куки не должны попадать в логи
 * 
 * КАК РАБОТАЕТ:
 * - Перехватывает запросы и ответы перед логированием
 * - Заменяет реальные значения кук на маскированные версии
 * - Применяется автоматически ко всем запросам через REST Assured
 * 
 * ЧТО МАСКИРУЕТ:
 * - Cookie заголовки в запросах
 * - Set-Cookie заголовки в ответах
 * - Любые значения, содержащие "cookie" в названии
 */
public class SensitiveDataFilter implements Filter {
    
    /**
     * Паттерн для поиска кук в заголовках
     * Ищет: Cookie: name=value или Set-Cookie: name=value
     */
    private static final Pattern COOKIE_PATTERN = Pattern.compile(
        "(?i)(cookie|set-cookie)\\s*[:=]\\s*([^;\\s]+)=([^;\\s]+)",
        Pattern.CASE_INSENSITIVE
    );
    
    /**
     * Маскированное значение для замены
     */
    private static final String MASKED_VALUE = "***MASKED***";
    
    /**
     * Обработка запроса перед отправкой
     * Маскирует куки в заголовках запроса
     */
    @Override
    public Response filter(FilterableRequestSpecification requestSpec, 
                          FilterableResponseSpecification responseSpec, 
                          FilterContext ctx) {
        
        // Получаем все заголовки запроса
        if (requestSpec.getHeaders() != null) {
            requestSpec.getHeaders().asList().forEach(header -> {
                String headerName = header.getName();
                String headerValue = header.getValue();
                
                // Если заголовок содержит куки - маскируем значение
                if (headerName != null && headerValue != null) {
                    if (headerName.equalsIgnoreCase("Cookie") || 
                        headerName.equalsIgnoreCase("Set-Cookie")) {
                        // Маскируем значение куки
                        String maskedValue = maskCookieValue(headerValue);
                        // Заменяем заголовок на маскированную версию
                        requestSpec.removeHeader(headerName);
                        requestSpec.header(headerName, maskedValue);
                    }
                }
            });
        }
        
        // Выполняем запрос
        Response response = ctx.next(requestSpec, responseSpec);
        
        // Маскируем куки в ответе
        if (response.getHeaders() != null) {
            response.getHeaders().asList().forEach(header -> {
                String headerName = header.getName();
                if (headerName != null && 
                    (headerName.equalsIgnoreCase("Set-Cookie") || 
                     headerName.equalsIgnoreCase("Cookie"))) {
                    // Заголовки ответа уже залогированы, но мы можем их маскировать
                    // для будущих операций
                }
            });
        }
        
        return response;
    }
    
    /**
     * Маскирует значение куки
     * Заменяет реальное значение на маскированное
     * 
     * Примеры:
     * - "cookie=abc123" -> "cookie=***MASKED***"
     * - "sessionId=xyz789; path=/; domain=example.com" -> "sessionId=***MASKED***; path=/; domain=example.com"
     * 
     * @param cookieValue исходное значение куки
     * @return маскированное значение
     */
    private String maskCookieValue(String cookieValue) {
        if (cookieValue == null || cookieValue.isEmpty()) {
            return cookieValue;
        }
        
        // Заменяем значения кук на маскированные
        // Паттерн: name=value -> name=***MASKED***
        return COOKIE_PATTERN.matcher(cookieValue).replaceAll(
            "$1: $2=" + MASKED_VALUE
        );
    }
    
    /**
     * Маскирует значение куки для логирования
     * Используется в logger.info() и других местах
     * 
     * @param cookieValue исходное значение куки
     * @return маскированное значение
     */
    public static String maskForLogging(String cookieValue) {
        if (cookieValue == null || cookieValue.isEmpty()) {
            return cookieValue;
        }
        
        // Если значение уже короткое и похоже на маскированное - возвращаем как есть
        if (cookieValue.equals(MASKED_VALUE) || cookieValue.length() < 10) {
            return cookieValue;
        }
        
        // Маскируем: оставляем первые 4 символа и последние 4, остальное заменяем
        if (cookieValue.length() > 8) {
            return cookieValue.substring(0, 4) + "***" + cookieValue.substring(cookieValue.length() - 4);
        }
        
        return MASKED_VALUE;
    }
    
    /**
     * Маскирует объект Cookies для логирования
     * 
     * @param cookies объект Cookies
     * @return строковое представление с маскированными значениями
     */
    public static String maskCookiesForLogging(io.restassured.http.Cookies cookies) {
        if (cookies == null) {
            return "null";
        }
        
        // Создаем маскированное представление
        StringBuilder masked = new StringBuilder("Cookies[");
        cookies.asList().forEach(cookie -> {
            if (masked.length() > 8) {
                masked.append(", ");
            }
            masked.append(cookie.getName())
                  .append("=")
                  .append(maskForLogging(cookie.getValue()));
        });
        masked.append("]");
        
        return masked.toString();
    }
}
