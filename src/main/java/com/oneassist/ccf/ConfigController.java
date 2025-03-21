package com.oneassist.ccf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            ProductCategoryEntity savedConfig = configService.saveConfig(request);
            return new ResponseEntity<>(savedConfig, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error saving configuration: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<ProductCategoryEntity>> getAllConfigs() {
        List<ProductCategoryEntity> configs = configService.getAllConfigs();
        return new ResponseEntity<>(configs, HttpStatus.OK);
    }

    @GetMapping("/{categoryName}")
    public ResponseEntity<?> getConfigByCategory(@PathVariable String categoryName) {
        Optional<ProductCategoryEntity> config = configService.getConfigByCategory(categoryName);
        if (config.isPresent()) {

            return new ResponseEntity<>(mapToDto(config.get()), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Configuration not found for category: " + categoryName,
                    HttpStatus.NOT_FOUND);
        }
    }

    public ProductCategoryDTO mapToDto(ProductCategoryEntity entity) {
        ProductCategoryDTO dto = new ProductCategoryDTO();
        dto.setId(entity.getId());
        dto.setCategoryName(entity.getCategoryName());

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