package com.mogou.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "offers-service")
public interface OffersServiceClient {
    
    @GetMapping("/api/offers/stats/filiere")
    List<Map<String, Object>> getOfferStatsByFiliere(@RequestParam("periode") String periode);
}