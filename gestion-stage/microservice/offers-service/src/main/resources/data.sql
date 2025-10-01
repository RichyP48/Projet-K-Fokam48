-- Insert test offers for company 2
INSERT INTO offre_stage (id, titre, description, domaine, duree, localisation, salaire, statut, entreprise_id, date_publication, date_expiration) 
VALUES 
(1, 'Stage Développement Java', 'Développement d''applications Java Spring Boot', 'INFORMATIQUE', 6, 'Paris', 1000.0, 'PUBLIEE', 2, CURRENT_DATE, CURRENT_DATE + INTERVAL '30 days'),
(2, 'Stage Marketing Digital', 'Gestion des campagnes marketing et réseaux sociaux', 'MARKETING', 4, 'Lyon', 800.0, 'PUBLIEE', 2, CURRENT_DATE, CURRENT_DATE + INTERVAL '30 days'),
(3, 'Stage Data Science', 'Analyse de données et machine learning', 'INFORMATIQUE', 6, 'Toulouse', 1200.0, 'PUBLIEE', 1, CURRENT_DATE, CURRENT_DATE + INTERVAL '30 days')
ON CONFLICT (id) DO NOTHING;

-- Reset sequence
SELECT setval('offre_stage_id_seq', (SELECT MAX(id) FROM offre_stage));