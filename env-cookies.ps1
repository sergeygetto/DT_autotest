# Файл с переменными окружения для кук
# Использование:
#   Вариант 1 — загрузить в текущую сессию и запустить тесты:
#     . .\env-cookies.ps1
#     mvn test -Dcookie="$env:cookie" -Dcookie_autotest="$env:cookie_autotest"
#
#   Вариант 2 — использовать run-all-tests-two-cookies.ps1 (он загрузит этот файл и запустит все тесты)

# Первая кука (основная)
$env:cookie = "_"

# Вторая кука (cookie_autotest)
$env:cookie_autotest = ""
