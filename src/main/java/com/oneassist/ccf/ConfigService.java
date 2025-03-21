package com.oneassist.ccf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConfigService {

    @Autowired
    private ProductCategoryRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    public ProductCategoryEntity saveConfig(ConfigRequest request) throws JsonProcessingException {
        String categoryName = request.getCategoryName();
        String configJson = objectMapper.writeValueAsString(request.getStages());

        Optional<ProductCategoryEntity> existingConfig = repository.findByCategoryName(categoryName);

        ProductCategoryEntity config;
        if (existingConfig.isPresent()) {
            config = existingConfig.get();
            config.setConfiguration(configJson);
        } else {
            config = new ProductCategoryEntity();
            config.setCategoryName(categoryName);
            config.setConfiguration(configJson);
        }

        return repository.save(config);
    }

    public List<ProductCategoryEntity> getAllConfigs() {
        return repository.findAll();
    }

    public Optional<ProductCategoryEntity> getConfigByCategory(String categoryName) {
        return repository.findByCategoryName(categoryName);
    }
}