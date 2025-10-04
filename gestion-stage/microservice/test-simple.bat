@echo off
echo Testing Backend Security Issue
echo.

echo 1. Testing current problem state...
curl -X GET "http://localhost:8082/api/candidatures/test-current-problem"
echo.
echo.

echo 2. Testing security isolation...
curl -X GET "http://localhost:8082/api/candidatures/test-security-simple"
echo.
echo.

echo 3. Health check...
curl -X GET "http://localhost:8082/api/candidatures/health"
echo.

pause