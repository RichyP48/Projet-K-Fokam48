@echo off
echo "=== Construction de tous les microservices ==="

echo "1. Config Server..."
cd config-server
call mvn clean package -DskipTests
cd ..

echo "2. Eureka Server..."
cd Eureka-server
call mvn clean package -DskipTests
cd ..

echo "3. API Gateway..."
cd api-gateway
call mvn clean package -DskipTests
cd ..

echo "4. User Service..."
cd user-service
call mvn clean package -DskipTests
cd ..

echo "5. Offers Service..."
cd offers-service
call mvn clean package -DskipTests
cd ..

echo "6. Applications Service..."
cd applications-service
call mvn clean package -DskipTests
cd ..

echo "7. Conventions Service..."
cd conventions-service
call mvn clean package -DskipTests
cd ..

echo "8. Evaluation Service..."
cd evaluation-service
call mvn clean package -DskipTests
cd ..

echo "9. Notifications Service..."
cd notifications-service
call mvn clean package -DskipTests
cd ..

echo "10. Message Service..."
cd message-service
call mvn clean package -DskipTests
cd ..

echo "11. Reporting Service..."
cd reporting-service
call mvn clean package -DskipTests
cd ..

echo "12. Workflow Service..."
cd workflow-service
call mvn clean package -DskipTests
cd ..

echo "=== Construction terminée ==="