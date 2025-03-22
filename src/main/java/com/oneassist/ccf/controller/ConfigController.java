package com.oneassist.ccf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneassist.ccf.contract.ConfigRequest;
import com.oneassist.ccf.service.ConfigService;
import com.oneassist.ccf.contract.CategoryServiceDTO;
import com.oneassist.ccf.entity.CategoryServiceConfigEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/configs")
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @PostMapping
    public ResponseEntity<?> saveConfig(@RequestBody ConfigRequest request) {
        try {
            final CategoryServiceConfigEntity savedConfig = configService.saveConfig(request);
            return new ResponseEntity<>(savedConfig, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error saving configuration: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<CategoryServiceDTO>> getAllConfigs() {
        // Get configs from the database and map them to DTOs
        final List<CategoryServiceConfigEntity> configs = configService.getAllConfigs();

        final List<CategoryServiceDTO> categoryServiceDTOS = new ArrayList<>();
        configs.forEach(config -> categoryServiceDTOS.add(mapToDto(config)));

        return new ResponseEntity<>(categoryServiceDTOS, HttpStatus.OK);
    }

    @GetMapping("/{categoryName}/{serviceName}/{version}")
    public ResponseEntity<?> getConfigByCategory(@PathVariable("categoryName") String categoryName,
                                                 @PathVariable("serviceName") String serviceName,
                                                 @PathVariable("version") int version) {
        // Get config from the database for category, service, and version
        final Optional<CategoryServiceConfigEntity> config = configService.getConfigByCategoryAndServiceAndVersion(categoryName, serviceName, version);
        if (config.isPresent()) {
            return new ResponseEntity<>(mapToDto(config.get()), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Configuration not found for category: " + categoryName, HttpStatus.NOT_FOUND);
        }
    }

    public CategoryServiceDTO mapToDto(CategoryServiceConfigEntity entity) {
        final CategoryServiceDTO dto = new CategoryServiceDTO();
        dto.setId(entity.getId());
        dto.setCategoryName(entity.getCategoryName());
        dto.setServiceName(entity.getServiceName());

        // Parse the string JSON into an Object
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Object configObject = objectMapper.readValue(entity.getConfiguration(), Object.class);
            dto.setConfiguration(configObject);
        } catch (Exception e) {
            dto.setConfiguration(null);
        }

        return dto;
    }
}