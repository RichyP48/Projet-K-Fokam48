package com.mogou.service;

import com.mogou.dto.CreateOffreStageRequest;
import com.mogou.dto.OffreStageDto;
import com.mogou.dto.UpdateOffreRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OffreStageService {

    OffreStageDto createOffre(CreateOffreStageRequest request);

    Page<OffreStageDto> searchOffres(String domaine, Integer duree, String ville, String keyword, Pageable pageable);

    OffreStageDto getOffreById(Long id);

    OffreStageDto updateOffre(Long id, UpdateOffreRequest request);

    void deleteOffre(Long id);

    Page<OffreStageDto> getOffresByEntrepriseId(Long entrepriseId, Pageable pageable);
}