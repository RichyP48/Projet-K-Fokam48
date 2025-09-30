-- Création des tables académiques
CREATE TABLE IF NOT EXISTS schools (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    description TEXT
);

CREATE TABLE IF NOT EXISTS faculties (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    school_id BIGINT REFERENCES schools(id)
);

CREATE TABLE IF NOT EXISTS departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    faculty_id BIGINT REFERENCES faculties(id),
    head_teacher_id BIGINT REFERENCES users(id)
);

-- Ajout des colonnes académiques à user_profiles
ALTER TABLE user_profiles 
ADD COLUMN IF NOT EXISTS school_id BIGINT REFERENCES schools(id),
ADD COLUMN IF NOT EXISTS faculty_id BIGINT REFERENCES faculties(id),
ADD COLUMN IF NOT EXISTS department_id BIGINT REFERENCES departments(id),
ADD COLUMN IF NOT EXISTS study_level VARCHAR(10);

-- Suppression de l'ancienne colonne filiere
ALTER TABLE user_profiles DROP COLUMN IF EXISTS filiere;