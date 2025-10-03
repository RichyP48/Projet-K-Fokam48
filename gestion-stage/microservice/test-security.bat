@echo off
echo Testing Backend Security - Company Application Isolation
echo.

REM Test 1: Company 1 tries to access Company 2's applications
echo Test 1: Company 1 accessing Company 2 applications (should be FORBIDDEN)
curl -X GET "http://localhost:8082/api/candidatures/entreprise/2" ^
     -H "Authorization: Bearer COMPANY_1_TOKEN" ^
     -H "Content-Type: application/json"
echo.
echo.

REM Test 2: Company 1 tries to access offer owned by Company 2
echo Test 2: Company 1 accessing Company 2's offer applications (should be FORBIDDEN)
curl -X GET "http://localhost:8082/api/candidatures/offre/12" ^
     -H "Authorization: Bearer COMPANY_1_TOKEN" ^
     -H "Content-Type: application/json"
echo.
echo.

REM Test 3: Company 1 accessing their own applications (should work)
echo Test 3: Company 1 accessing own applications (should work)
curl -X GET "http://localhost:8082/api/candidatures/entreprise/me" ^
     -H "Authorization: Bearer COMPANY_1_TOKEN" ^
     -H "Content-Type: application/json"
echo.
echo.

REM Test 4: Debug - Check isolation
echo Test 4: Debug isolation test
curl -X GET "http://localhost:8082/api/candidatures/debug/isolation-test" ^
     -H "Content-Type: application/json"
echo.

pause