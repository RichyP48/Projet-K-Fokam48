package com.mogou.controller;

import com.mogou.dto.OffreStageDto;
import com.mogou.service.OffreStageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final OffreStageService offreStageService;

    @GetMapping("/me/offers")
    public ResponseEntity<Page<OffreStageDto>> getCurrentCompanyOffers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long entrepriseId = 4L; // TODO: Récupérer depuis JWT
        Pageable pageable = PageRequest.of(page, size);
        
        try {
            Page<OffreStageDto> result = offreStageService.getOffresByEntrepriseId(entrepriseId, pageable);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(Page.empty(pageable));
        }
    }
}