@echo off
echo ========================================
echo DIAGNOSTIC DES SERVICES MICROSERVICES
echo ========================================

echo.
echo 1. Verification des services Docker...
docker-compose ps

echo.
echo 2. Test de connectivite API Gateway...
curl -s -o nul -w "API Gateway (8090): %%{http_code}\n" http://localhost:8090/actuator/health 2>nul || echo "API Gateway (8090): INACCESSIBLE"

echo.
echo 3. Test de connectivite Eureka...
curl -s -o nul -w "Eureka (8761): %%{http_code}\n" http://localhost:8761/actuator/health 2>nul || echo "Eureka (8761): INACCESSIBLE"

echo.
echo 4. Test de connectivite User Service...
curl -s -o nul -w "User Service (8080): %%{http_code}\n" http://localhost:8080/actuator/health 2>nul || echo "User Service (8080): INACCESSIBLE"

echo.
echo 5. Test de connectivite Offers Service...
curl -s -o nul -w "Offers Service (8081): %%{http_code}\n" http://localhost:8081/actuator/health 2>nul || echo "Offers Service (8081): INACCESSIBLE"

echo.
echo 6. Test de connectivite PostgreSQL...
docker exec microservice-postgres-1 pg_isready -U postgres 2>nul && echo "PostgreSQL: OK" || echo "PostgreSQL: ERREUR"

echo.
echo 7. Test de connectivite Redis...
docker exec microservice-redis-1 redis-cli ping 2>nul && echo "Redis: OK" || echo "Redis: ERREUR"

echo.
echo 8. Verification des logs des services principaux...
echo "--- API Gateway logs (dernieres 5 lignes) ---"
docker-compose logs --tail=5 api-gateway

echo.
echo "--- User Service logs (dernieres 5 lignes) ---"
docker-compose logs --tail=5 user-service

echo.
echo "--- Offers Service logs (dernieres 5 lignes) ---"
docker-compose logs --tail=5 offers-service

echo.
echo ========================================
echo DIAGNOSTIC TERMINE
echo ========================================
pause