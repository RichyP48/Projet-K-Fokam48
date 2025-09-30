package com.mogou.service;


import com.mogou.dto.CreateOffreStageRequest;
import com.mogou.dto.OffreStageDto;
import com.mogou.dto.UpdateOffreRequest;
import com.mogou.model.DomaineStage;
import com.mogou.model.OffreStage;
import com.mogou.model.StatutOffre;
import com.mogou.repository.OffreStageRepository;
import com.mogou.repository.OffreStageSpecification;
// import org.springframework.cache.annotation.Cacheable;
import lombok.RequiredArgsConstructor;
// import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class OffreStageServiceImpl implements OffreStageService {

    private final OffreStageRepository offreStageRepository;
    // private final com.mogou.client.UserClient userClient; // Temporairement désactivé

    // ... (Le reste du code de la classe reste le même)
    @Override
    @Transactional
    public OffreStageDto createOffre(CreateOffreStageRequest request) {
        // Validation de l'entreprise
        // Validation temporairement désactivée pour éviter l'erreur 500
        // TODO: Réactiver quand user-service sera stable
        
        OffreStage offre = new OffreStage();
        offre.setTitre(request.getTitre());
        offre.setDescription(request.getDescription());
        offre.setEntrepriseId(request.getEntrepriseId());
        offre.setDomaine(request.getDomaine());
        offre.setDuree(request.getDuree());
        offre.setLocalisation(request.getLocalisation());
        offre.setCompetencesRequises(request.getCompetencesRequises());
        offre.setStatut(StatutOffre.PUBLIEE);
        offre.setDatePublication(LocalDate.now());
        offre.setDateExpiration(request.getDateExpiration());

        OffreStage offreSauvegardee = offreStageRepository.save(offre);
        return mapToDto(offreSauvegardee);
    }

    @Override
    @Transactional(readOnly = true)
    // @Cacheable(value = "offresCache", key = "{#domaine, #duree, #ville, #keyword, #pageable.pageNumber, #pageable.pageSize}")
    public Page<OffreStageDto> searchOffres(String domaine, Integer duree, String ville, String keyword, Pageable pageable) {
        DomaineStage domaineEnum = null;
        if (domaine != null && !domaine.isBlank()) {
            try {
                domaineEnum = DomaineStage.valueOf(domaine.toUpperCase());
            } catch (IllegalArgumentException e) {
                return Page.empty(pageable);
            }
        }

        Specification<OffreStage> spec = Specification.where(OffreStageSpecification.aPourDomaine(domaineEnum))
                .and(OffreStageSpecification.aPourDuree(duree))
                .and(OffreStageSpecification.estDansLaVille(ville))
                .and(OffreStageSpecification.contientMotCle(keyword));

        Page<OffreStage> offres = offreStageRepository.findAll(spec, pageable);
        return offres.map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public OffreStageDto getOffreById(Long id) {
        return offreStageRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée avec l'ID : " + id));
    }

    @Override
    @Transactional
    // @CacheEvict(value = "offresCache", allEntries = true)
    public OffreStageDto updateOffre(Long id, UpdateOffreRequest request) {
        OffreStage offre = offreStageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée avec l'ID : " + id));

        offre.setTitre(request.getTitre());
        offre.setDescription(request.getDescription());
        offre.setDomaine(request.getDomaine());
        offre.setDuree(request.getDuree());
        offre.setLocalisation(request.getLocalisation());
        offre.setCompetencesRequises(request.getCompetencesRequises());
        offre.setDateExpiration(request.getDateExpiration());

        OffreStage offreMiseAJour = offreStageRepository.save(offre);
        return mapToDto(offreMiseAJour);
    }

    @Override
    @Transactional
    // @CacheEvict(value = "offresCache", allEntries = true)
    public void deleteOffre(Long id) {
        if (!offreStageRepository.existsById(id)) {
            throw new RuntimeException("Offre non trouvée avec l'ID : " + id);
        }
        offreStageRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OffreStageDto> getOffresByEntrepriseId(Long entrepriseId, Pageable pageable) {
        Page<OffreStage> offres = offreStageRepository.findByEntrepriseId(entrepriseId, pageable);
        return offres.map(this::mapToDto);
    }

    private OffreStageDto mapToDto(OffreStage offre) {
        OffreStageDto dto = new OffreStageDto();
        dto.setId(offre.getId());
        dto.setTitre(offre.getTitre());
        dto.setDescription(offre.getDescription());
        dto.setEntrepriseId(offre.getEntrepriseId());
        dto.setCompanyName("Entreprise " + offre.getEntrepriseId()); // TODO: Récupérer nom via UserClient
        dto.setDomaine(offre.getDomaine());
        dto.setDuree(offre.getDuree());
        dto.setSalaire(offre.getSalaire());
        dto.setLocalisation(offre.getLocalisation());
        dto.setCompetencesRequises(offre.getCompetencesRequises());
        dto.setStatut(offre.getStatut());
        dto.setDatePublication(offre.getDatePublication());
        dto.setDateExpiration(offre.getDateExpiration());
        return dto;
    }
}

