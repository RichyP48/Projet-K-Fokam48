-- Création des schémas pour chaque service
CREATE SCHEMA IF NOT EXISTS user_service;
CREATE SCHEMA IF NOT EXISTS offers_service;
CREATE SCHEMA IF NOT EXISTS applications_service;
CREATE SCHEMA IF NOT EXISTS conventions_service;
CREATE SCHEMA IF NOT EXISTS evaluation_service;
CREATE SCHEMA IF NOT EXISTS notifications_service;
CREATE SCHEMA IF NOT EXISTS message_service;
CREATE SCHEMA IF NOT EXISTS reporting_service;
CREATE SCHEMA IF NOT EXISTS workflow_service;

-- Utilisateurs pour chaque service
CREATE USER user_service_user WITH PASSWORD 'user_pass';
CREATE USER offers_service_user WITH PASSWORD 'offers_pass';
CREATE USER applications_service_user WITH PASSWORD 'applications_pass';
CREATE USER conventions_service_user WITH PASSWORD 'conventions_pass';
CREATE USER evaluation_service_user WITH PASSWORD 'evaluation_pass';
CREATE USER notifications_service_user WITH PASSWORD 'notifications_pass';
CREATE USER message_service_user WITH PASSWORD 'message_pass';
CREATE USER reporting_service_user WITH PASSWORD 'reporting_pass';
CREATE USER workflow_service_user WITH PASSWORD 'workflow_pass';

-- Permissions
GRANT ALL PRIVILEGES ON SCHEMA user_service TO user_service_user;
GRANT ALL PRIVILEGES ON SCHEMA offers_service TO offers_service_user;
GRANT ALL PRIVILEGES ON SCHEMA applications_service TO applications_service_user;
GRANT ALL PRIVILEGES ON SCHEMA conventions_service TO conventions_service_user;
GRANT ALL PRIVILEGES ON SCHEMA evaluation_service TO evaluation_service_user;
GRANT ALL PRIVILEGES ON SCHEMA notifications_service TO notifications_service_user;
GRANT ALL PRIVILEGES ON SCHEMA message_service TO message_service_user;
GRANT ALL PRIVILEGES ON SCHEMA reporting_service TO reporting_service_user;
GRANT ALL PRIVILEGES ON SCHEMA workflow_service TO workflow_service_user;