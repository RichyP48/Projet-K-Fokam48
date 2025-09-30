@echo off
echo "=== Démarrage complet des microservices ==="

echo "1. Infrastructure..."
docker-compose up -d postgres minio rabbitmq redis

echo "2. Config Server..."
docker-compose up -d config-server

echo "3. Attente Config Server (30s)..."
ping -n 30 127.0.0.1 > nul

echo "4. Eureka Server..."
docker-compose up -d eureka-server

echo "5. Attente Eureka (20s)..."
ping -n 20 127.0.0.1 > nul

echo "6. API Gateway..."
docker-compose up -d api-gateway

echo "7. Services métier..."
docker-compose up -d user-service offers-service applications-service conventions-service evaluation-service notifications-service message-service reporting-service workflow-service

echo "=== Tous les services sont démarrés ==="
echo "Vérifiez avec: docker-compose ps"