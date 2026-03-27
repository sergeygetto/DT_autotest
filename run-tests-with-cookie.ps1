# Скрипт для запуска всех тестов с кукой
# Использование: .\run-tests-with-cookie.ps1

$cookieValue = ""

Write-Host "Запуск всех тестов с установленной кукой..."
mvn test -Dcookie="$cookieValue"
