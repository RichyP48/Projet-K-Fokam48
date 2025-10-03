package com.mogou.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "offers-service", configuration = com.mogou.config.FeignConfig.class)
public interface OffersClient {
    
    @GetMapping("/api/offers/{id}/validate")
    boolean isOfferValid(@PathVariable("id") Long id);
    
    @GetMapping("/api/offers/{id}")
    OfferDto getOfferById(@PathVariable("id") Long id);
    
    @GetMapping("/api/offers/company/{companyId}")
    java.util.List<OfferDto> getOffersByCompanyId(@PathVariable("companyId") Long companyId);
}