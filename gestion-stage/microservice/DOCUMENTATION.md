# 🚀 Guide de Démarrage des Microservices

## 📋 Prérequis

- **Java 17+** installé
- **Maven 3.6+** installé
- **Docker Desktop** installé et démarré
- **Git** installé

## 🏗️ Architecture

### Services Infrastructure
- **PostgreSQL** (5432) - Base de données
- **MinIO** (9010/9011) - Stockage fichiers
- **RabbitMQ** (5672/15672) - Messagerie
- **Redis** (6379) - Cache
- **Config Server** (8888) - Configuration centralisée
- **Eureka Server** (8761) - Service Discovery

### Services Métier
- **API Gateway** (8090) - Point d'entrée
- **User Service** (8080) - Gestion utilisateurs
- **Offers Service** (8081) - Gestion offres
- **Applications Service** (8082) - Gestion candidatures
- **Evaluation Service** (8083) - Système d'évaluation
- **Notifications Service** (8084) - Notifications
- **Message Service** (8085) - Messagerie temps réel
- **Reporting Service** (8086) - Rapports
- **Workflow Service** (8087) - Orchestration
- **Conventions Service** (8095) - Gestion conventions

## 🚀 Démarrage Rapide

### Option 1: Démarrage Automatique
```bash
# 1. Construction de tous les services
build-all.bat

# 2. Démarrage de tous les services
start-all.bat
```

### Option 2: Démarrage Docker Compose
```bash
# Construction et démarrage en une commande
docker-compose up -d --build
```

## 🔧 Démarrage Manuel (Étape par Étape)

### 1. Construction des Services
```bash
# Config Server
cd config-server && mvn clean package -DskipTests && cd ..

# Eureka Server
cd Eureka-server && mvn clean package -DskipTests && cd ..

# API Gateway
cd api-gateway && mvn clean package -DskipTests && cd ..

# Services métier
cd user-service && mvn clean package -DskipTests && cd ..
cd offers-service && mvn clean package -DskipTests && cd ..
cd applications-service && mvn clean package -DskipTests && cd ..
cd conventions-service && mvn clean package -DskipTests && cd ..
cd evaluation-service && mvn clean package -DskipTests && cd ..
cd notifications-service && mvn clean package -DskipTests && cd ..
cd message-service && mvn clean package -DskipTests && cd ..
cd reporting-service && mvn clean package -DskipTests && cd ..
cd workflow-service && mvn clean package -DskipTests && cd ..
```

### 2. Démarrage Infrastructure
```bash
docker-compose up -d postgres minio rabbitmq redis
```

### 3. Démarrage Config Server
```bash
docker-compose up -d config-server
# Attendre 30 secondes
```

### 4. Démarrage Eureka Server
```bash
docker-compose up -d eureka-server
# Attendre 20 secondes
```

### 5. Démarrage API Gateway
```bash
docker-compose up -d api-gateway
```

### 6. Démarrage Services Métier
```bash
docker-compose up -d user-service offers-service applications-service conventions-service evaluation-service notifications-service message-service reporting-service workflow-service
```

## 🔍 Vérification du Démarrage

### Commandes de Vérification
```bash
# État des conteneurs
docker-compose ps

# Logs d'un service
docker-compose logs [nom-service]

# Logs en temps réel
docker-compose logs -f [nom-service]
```

### URLs de Vérification
- **Config Server**: http://localhost:8888/actuator/health
- **Eureka Dashboard**: http://localhost:8761 (admin: `eureka-admin` / `pkf`)
- **API Gateway**: http://localhost:8090/actuator/health
- **MinIO Console**: http://localhost:9011 (admin: `minioadmin` / `minioadmin123`)
- **RabbitMQ Management**: http://localhost:15672 (admin: `admin` / `admin123`)

## 🛠️ Commandes Utiles

### Gestion des Services
```bash
# Arrêter tous les services
docker-compose down

# Redémarrer un service
docker-compose restart [nom-service]

# Voir les logs
docker-compose logs [nom-service]

# Reconstruire un service
docker-compose up -d --build [nom-service]
```

### Nettoyage
```bash
# Arrêter et supprimer tout
docker-compose down -v

# Supprimer les images
docker-compose down --rmi all

# Nettoyage complet Docker
docker system prune -a
```

## 🔧 Configuration

### Variables d'Environnement (.env)
```env
# Base de données
DB_HOST=postgres
DB_PORT=5432
DB_NAME=microservice_db
DB_USER=postgres
DB_PASSWORD=postgres

# MinIO
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=minioadmin123

# RabbitMQ
RABBITMQ_USER=admin
RABBITMQ_PASSWORD=admin123
```

### Ports Utilisés
| Service | Port | Description |
|---------|------|-------------|
| PostgreSQL | 5432 | Base de données |
| MinIO | 9010/9011 | Stockage fichiers |
| RabbitMQ | 5672/15672 | Messagerie |
| Redis | 6379 | Cache |
| Config Server | 8888 | Configuration |
| Eureka Server | 8761 | Service Discovery |
| API Gateway | 8090 | Point d'entrée |
| User Service | 8080 | Gestion utilisateurs |
| Offers Service | 8081 | Gestion offres |
| Applications Service | 8082 | Candidatures |
| Evaluation Service | 8083 | Évaluations |
| Notifications Service | 8084 | Notifications |
| Message Service | 8085 | Messages |
| Reporting Service | 8086 | Rapports |
| Workflow Service | 8087 | Workflows |
| Conventions Service | 8095 | Conventions |

## 🐛 Dépannage

### Problèmes Courants

**Service ne démarre pas:**
```bash
# Vérifier les logs
docker-compose logs [nom-service]

# Redémarrer le service
docker-compose restart [nom-service]
```

**Port déjà utilisé:**
```bash
# Voir les ports utilisés
netstat -an | findstr :8080

# Arrêter le processus
taskkill /PID [PID] /F
```

**Problème de mémoire:**
```bash
# Augmenter la mémoire Docker Desktop
# Settings > Resources > Memory > 4GB+
```

**Base de données non accessible:**
```bash
# Vérifier PostgreSQL
docker-compose logs postgres

# Recréer la base
docker-compose down postgres
docker-compose up -d postgres
```

### Ordre de Démarrage Important
1. Infrastructure (PostgreSQL, MinIO, RabbitMQ, Redis)
2. Config Server
3. Eureka Server
4. API Gateway
5. Services métier

## 📊 Monitoring

### Health Checks
- Config Server: http://localhost:8888/actuator/health
- Eureka: http://localhost:8761/actuator/health
- API Gateway: http://localhost:8090/actuator/health
- User Service: http://localhost:8080/actuator/health

### Test des Utilisateurs
```bash
# Tester tous les utilisateurs par défaut
test-users.bat

# Ou manuellement
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"richardmogou99@gmail.com\",\"password\":\"password\"}"
```

### Dashboards
- **Eureka Dashboard**: http://localhost:8761
- **MinIO Console**: http://localhost:9011
- **RabbitMQ Management**: http://localhost:15672

## 🔐 Sécurité

### Credentials par Défaut

**Infrastructure:**
- **PostgreSQL**: `postgres` / `postgres`
- **MinIO**: `minioadmin` / `minioadmin123`
- **RabbitMQ**: `admin` / `admin123`
- **Eureka**: `eureka-admin` / `pkf`

**Utilisateurs de l'application:**
- **Admin**: `richardmogou99@gmail.com` / `password`
- **Étudiant**: `johanmogou@gmail.com` / `password`
- **Enseignant**: `sarahmogou@gmail.com` / `password`
- **Entreprise**: `researchecenter@pkf.com` / `password`

⚠️ **Important**: Changez ces credentials en production !

## 📝 Notes

- Les services utilisent la configuration centralisée via Config Server
- Eureka gère la découverte des services automatiquement
- L'API Gateway route toutes les requêtes vers les services appropriés
- Les données sont persistées dans PostgreSQL avec des schémas séparés
- MinIO gère le stockage des fichiers (documents, images, etc.)