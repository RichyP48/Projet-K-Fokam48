package com.mogou.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "offers-service")
public interface OffersClient {
    
    @GetMapping("/api/offers/company/{companyId}")
    java.util.List<OfferDto> getOffersByCompanyId(@PathVariable("companyId") Long companyId);
}