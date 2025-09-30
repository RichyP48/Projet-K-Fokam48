@echo off
echo "=== Démarrage des microservices ==="

echo "1. Construction du Config Server..."
cd config-server
call mvn clean package -DskipTests
cd ..

echo "2. Démarrage de l'infrastructure..."
docker-compose up -d postgres minio rabbitmq redis config-server

echo "3. Attente du Config Server..."
timeout /t 30

echo "4. Démarrage d'Eureka..."
docker-compose up -d eureka-server

echo "5. Attente d'Eureka..."
timeout /t 20

echo "6. Démarrage de l'API Gateway..."
docker-compose up -d api-gateway

echo "7. Démarrage des services métier..."
docker-compose up -d user-service offers-service

echo "=== Services démarrés ==="
echo "Config Server: http://localhost:8888"
echo "Eureka: http://localhost:8761"
echo "API Gateway: http://localhost:8090"
echo "PostgreSQL: localhost:5432"
echo "MinIO: http://localhost:9001"
echo "RabbitMQ: http://localhost:15672"