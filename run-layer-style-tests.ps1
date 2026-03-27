# Запуск тестов LayerStyleApiTest с одной кукой из переменной окружения
# Использование:
#   1) Задайте переменную окружения cookie, затем выполните скрипт:
#      $env:cookie = "ваша_кука_здесь"
#      .\run-layer-style-tests.ps1
#   2) Или передайте куку при вызове:
#      $env:cookie = "ваша_кука"; .\run-layer-style-tests.ps1

$defaultCookie = ''

if (-not $env:cookie) {
    $env:cookie = $defaultCookie
    Write-Warning "Используется cookie по умолчанию (задайте `$env:cookie для своей куки)"
}

# Кука передаётся через переменную окружения (наследуется JVM при запуске Maven).
# Не используем -Dcookie=..., т.к. длинная строка в PowerShell может обрезаться или ломать командную строку.
Write-Host "Запуск тестов LayerStyleApiTest с кукой из переменной окружения cookie..."
mvn test -Dtest=LayerStyleApiTest
