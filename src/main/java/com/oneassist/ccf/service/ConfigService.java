package com.oneassist.ccf.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneassist.ccf.entity.CategoryServiceConfigEntity;
import com.oneassist.ccf.repository.ProductCategoryRepository;
import com.oneassist.ccf.contract.ConfigRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConfigService {

    private static final Logger log = LoggerFactory.getLogger(ConfigService.class);


    @Autowired
    private ProductCategoryRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    public CategoryServiceConfigEntity saveConfig(ConfigRequest request) throws JsonProcessingException {
        String categoryName = request.getCategory();
        String serviceName = request.getService();
        log.info("Saving configuration for category: {}, service: {}", categoryName, serviceName);

        String configJson = objectMapper.writeValueAsString(request.getStages());

        Optional<CategoryServiceConfigEntity> existingConfig = repository.findFirstByCategoryNameAndServiceNameOrderByVersionDesc(categoryName, serviceName);

        int version = existingConfig.isPresent() ? existingConfig.get().getVersion() + 1 : 1;

        CategoryServiceConfigEntity config = new CategoryServiceConfigEntity();
        config.setCategoryName(categoryName);
        config.setServiceName(serviceName);
        config.setConfiguration(configJson);
        config.setVersion(version);

        return repository.save(config);
    }

    public List<CategoryServiceConfigEntity> getAllConfigs() {
        return repository.findLatestVersionsForEachCategoryAndService();
    }

    public Optional<CategoryServiceConfigEntity> getConfigByCategoryAndServiceAndVersion(String categoryName, String serviceName, int version) {
        return repository.findByCategoryNameAndServiceNameAndVersion(categoryName, serviceName, version);
    }
}