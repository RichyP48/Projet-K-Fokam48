bui# 🎯 Plateforme de Gestion de Stages - Microservices

## 🚀 Démarrage Rapide

```bash
# 1. Construire tous les services
.\build-all.bat

# 2. Démarrer tous les services
.\start-all.bat

# 3. Vérifier le démarrage
docker-compose ps
```

## 📋 Services Disponibles

| Service | Port | URL | Description |
|---------|------|-----|-------------|
| **API Gateway** | 8090 | http://localhost:8090 | Point d'entrée principal |
| **Eureka Dashboard** | 8761 | http://localhost:8761 | Service Discovery |
| **Config Server** | 8888 | http://localhost:8888 | Configuration centralisée |
| **User Service** | 8080 | http://localhost:8080 | Gestion utilisateurs |
| **Offers Service** | 8081 | http://localhost:8081 | Gestion offres de stage |
| **Applications Service** | 8082 | http://localhost:8082 | Gestion candidatures |
| **Conventions Service** | 8095 | http://localhost:8095 | Gestion conventions |
| **Evaluation Service** | 8083 | http://localhost:8083 | Système d'évaluation |
| **Notifications Service** | 8084 | http://localhost:8084 | Service de notifications |
| **Message Service** | 8085 | http://localhost:8085 | Messagerie temps réel |
| **Reporting Service** | 8086 | http://localhost:8086 | Génération de rapports |
| **Workflow Service** | 8087 | http://localhost:8087 | Orchestration des processus |

## 🔧 Infrastructure

| Service | Port | URL | Credentials |
|---------|------|-----|-------------|
| **PostgreSQL** | 5432 | localhost:5432 | `postgres` / `postgres` |
| **MinIO Console** | 9011 | http://localhost:9011 | `minioadmin` / `minioadmin123` |
| **RabbitMQ Management** | 15672 | http://localhost:15672 | `admin` / `admin123` |
| **Redis** | 6379 | localhost:6379 | - |

## 📖 Documentation Complète

Consultez [DOCUMENTATION.md](DOCUMENTATION.md) pour :
- Guide de démarrage détaillé
- Configuration avancée
- Dépannage
- Architecture complète

## 🛠️ Commandes Utiles

```bash
# Voir l'état des services
docker-compose ps

# Voir les logs d'un service
docker-compose logs [nom-service]

# Redémarrer un service
docker-compose restart [nom-service]

# Arrêter tous les services
docker-compose down
```

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   API Gateway   │────│  Eureka Server  │────│  Config Server  │
│     :8090       │    │      :8761      │    │      :8888      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │
         ├── User Service (:8080)
         ├── Offers Service (:8081)
         ├── Applications Service (:8082)
         ├── Evaluation Service (:8083)
         ├── Notifications Service (:8084)
         ├── Message Service (:8085)
         ├── Reporting Service (:8086)
         ├── Workflow Service (:8087)
         └── Conventions Service (:8095)
```

## 🔐 Sécurité

- Authentification JWT centralisée
- Configuration sécurisée via Config Server
- Isolation des services par conteneurs Docker
- Base de données avec schémas séparés par service

### 👥 Utilisateurs par Défaut
- **Admin**: `richardmogou99@gmail.com` / `password`
- **Étudiant**: `johanmogou@gmail.com` / `password`
- **Enseignant**: `sarahmogou@gmail.com` / `password`
- **Entreprise**: `researchecenter@pkf.com` / `password`

## 📊 Fonctionnalités

- **Gestion des utilisateurs** : Authentification, profils, rôles
- **Offres de stage** : Création, recherche, gestion des offres
- **Candidatures** : Processus de candidature avec workflow
- **Conventions** : Génération et signature électronique
- **Évaluations** : Système d'évaluation des stages
- **Notifications** : Email et notifications temps réel
- **Messagerie** : Chat en temps réel via WebSocket
- **Rapports** : Génération de statistiques et exports
- **Workflows** : Orchestration automatisée des processus

configure local minio service in docker

docker compose up minio --build -d

// or if already build just launch it

docker compose up minio -d
configure .env variable to be recognise by your ide (intellij, vscode)