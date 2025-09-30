package com.mogou.repository;

import com.mogou.model.DomaineStage;
import com.mogou.model.OffreStage;
import org.springframework.data.jpa.domain.Specification;

public class OffreStageSpecification {

    /**
     * Filtre pour le domaine du stage.
     * @param domaine Le domaine (enum).
     * @return Une spécification JPA.
     */
    public static Specification<OffreStage> aPourDomaine(DomaineStage domaine) {
        return (root, query, criteriaBuilder) ->
                domaine == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("domaine"), domaine);
    }

    /**
     * Filtre pour la durée du stage en mois.
     * @param duree La durée.
     * @return Une spécification JPA.
     */
    public static Specification<OffreStage> aPourDuree(Integer duree) {
        return (root, query, criteriaBuilder) ->
                duree == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("duree"), duree);
    }

    /**
     * Filtre pour la ville (recherche partielle dans la localisation).
     * @param ville Le nom de la ville.
     * @return Une spécification JPA.
     */
    public static Specification<OffreStage> estDansLaVille(String ville) {
        return (root, query, criteriaBuilder) ->
                ville == null || ville.isBlank() ? criteriaBuilder.conjunction() : criteriaBuilder.like(criteriaBuilder.lower(root.get("localisation")), "%" + ville.toLowerCase() + "%");
    }

    /**
     * Recherche full-text sur le titre et la description.
     * @param motCle Le mot-clé à rechercher.
     * @return Une spécification JPA.
     */
    public static Specification<OffreStage> contientMotCle(String motCle) {
        return (root, query, criteriaBuilder) ->
                motCle == null || motCle.isBlank() ? criteriaBuilder.conjunction() : criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("titre")), "%" + motCle.toLowerCase() + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + motCle.toLowerCase() + "%")
                );
    }
}
