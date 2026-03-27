# Запуск ВСЕХ тестов с двумя куками (cookie и cookie_autotest)
# Использование: вставьте свои куки ниже между кавычками, затем выполните .\run-all-tests-two-cookies.ps1

# ========== ВСТАВЬТЕ СВОИ КУКИ СЮДА ==========
# Первая кука (основная)
$COOKIE_1 = ""

# Вторая кука (cookie_autotest)
$COOKIE_2 = ""
# ==============================================

# Если куки заданы выше — используем их; иначе пробуем загрузить из env-cookies.ps1
if ($COOKIE_1) { $env:cookie = $COOKIE_1 }
if ($COOKIE_2) { $env:cookie_autotest = $COOKIE_2 }

$envFile = Join-Path $PSScriptRoot "env-cookies.ps1"
if ((-not $env:cookie -or -not $env:cookie_autotest) -and (Test-Path $envFile)) {
    . $envFile
    Write-Host "Куки загружены из env-cookies.ps1"
}

if (-not $env:cookie) {
    Write-Warning "Переменная cookie не задана. Заполните COOKIE_1 и COOKIE_2 в начале скрипта или env-cookies.ps1"
}

Write-Host "Запуск всех тестов с двумя куками (cookie, cookie_autotest)..."
mvn test -Dcookie="$env:cookie" -Dcookie_autotest="$env:cookie_autotest"
