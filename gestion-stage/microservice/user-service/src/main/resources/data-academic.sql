-- Insertion des écoles
INSERT INTO schools (name, address, description) VALUES 
('Université de Yaoundé I', 'Yaoundé, Cameroun', 'Université publique du Cameroun'),
('Université de Douala', 'Douala, Cameroun', 'Université publique du Cameroun'),
('ENSP Yaoundé', 'Yaoundé, Cameroun', 'École Nationale Supérieure Polytechnique');

-- Insertion des facultés
INSERT INTO faculties (name, school_id) VALUES 
('Faculté des Sciences', 1),
('Faculté de Médecine', 1),
('École Normale Supérieure', 1),
('Faculté des Sciences Économiques', 2),
('Faculté des Lettres', 2),
('Génie Informatique', 3),
('Génie Civil', 3);

-- Insertion des départements
INSERT INTO departments (name, faculty_id) VALUES 
('Informatique', 1),
('Mathématiques', 1),
('Physique', 1),
('Médecine Générale', 2),
('Chirurgie', 2),
('Enseignement Primaire', 3),
('Enseignement Secondaire', 3),
('Économie', 4),
('Gestion', 4),
('Français', 5),
('Anglais', 5),
('Génie Logiciel', 6),
('Réseaux et Télécommunications', 6),
('Bâtiment et Travaux Publics', 7),
('Géotechnique', 7);