INSERT INTO offre_stage (titre, description, entreprise_id, domaine, duree, localisation, salaire, statut, date_publication, date_expiration) 
VALUES 
('Stage Développement Java', 'Développement d''applications Java Spring Boot', 2, 'INFORMATIQUE', 6, 'Paris', 1000.0, 'PUBLIEE', CURRENT_DATE, CURRENT_DATE + INTERVAL '30 days'),
('Stage Marketing Digital', 'Gestion des campagnes marketing et réseaux sociaux', 2, 'MARKETING', 4, 'Lyon', 800.0, 'PUBLIEE', CURRENT_DATE, CURRENT_DATE + INTERVAL '30 days');