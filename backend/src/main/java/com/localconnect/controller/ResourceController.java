package com.localconnect.controller;

import com.localconnect.domain.enums.ResourceCategory;
import com.localconnect.dto.ResourceDto;
import com.localconnect.service.ProviderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/resources")
public class ResourceController {

    private final ProviderService providerService;

    public ResourceController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResourceDto>> searchResources(
            @RequestParam ResourceCategory category,
            @RequestParam(required = false) String subType) {
        return ResponseEntity.ok(providerService.searchActiveResources(category, subType));
    }
}
