package com.mogou.controller;

import com.mogou.dto.OffreStageDto;
import com.mogou.service.OffreStageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/simple-offers")
@RequiredArgsConstructor
public class SimpleOffersController {

    private final OffreStageService offreStageService;

    @GetMapping
    public ResponseEntity<Page<OffreStageDto>> getOffers(
            @RequestParam(required = false) Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        
        if (companyId != null) {
            try {
                Page<OffreStageDto> result = offreStageService.getOffresByEntrepriseId(companyId, pageable);
                return ResponseEntity.ok(result);
            } catch (Exception e) {
                return ResponseEntity.ok(Page.empty(pageable));
            }
        }
        
        try {
            Page<OffreStageDto> result = offreStageService.searchOffres(null, null, null, null, pageable);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(Page.empty(pageable));
        }
    }
}