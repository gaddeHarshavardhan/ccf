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

/**
 * Service class for managing product category configurations.
 */
@Service
public class ConfigService {

    private static final Logger log = LoggerFactory.getLogger(ConfigService.class);

    @Autowired
    private ProductCategoryRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Save a new configuration for a product category and service.
     *
     * @param request The configuration request containing the category, service, and stages.
     * @return The saved configuration entity.
     * @throws JsonProcessingException If there is an error processing JSON data.
     */
    public CategoryServiceConfigEntity saveConfig(ConfigRequest request) throws JsonProcessingException {
        final String categoryName = request.getCategory();
        final String serviceName = request.getService();
        log.info("Saving configuration for category: {}, service: {}", categoryName, serviceName);

        final String configJson = objectMapper.writeValueAsString(request.getStages());

        final Optional<CategoryServiceConfigEntity> existingConfig = repository.findFirstByCategoryNameAndServiceNameOrderByVersionDesc(categoryName, serviceName);

        final int version = existingConfig.isPresent() ? existingConfig.get().getVersion() + 1 : 1;

        final CategoryServiceConfigEntity config = new CategoryServiceConfigEntity();
        config.setCategoryName(categoryName);
        config.setServiceName(serviceName);
        config.setConfiguration(configJson);
        config.setVersion(version);

        return repository.save(config);
    }

    /**
     * Get all the latest versions of configurations for each product category and service.
     *
     * @return A list of configuration entities.
     */
    public List<CategoryServiceConfigEntity> getAllConfigs() {
        return repository.findLatestVersionsForEachCategoryAndService();
    }

    /**
     * Get a specific configuration for a product category and service by version.
     *
     * @param categoryName The name of the product category.
     * @param serviceName The name of the service.
     * @param version The version of the configuration.
     * @return An optional containing the configuration entity if found, otherwise an empty optional.
     */
    public Optional<CategoryServiceConfigEntity> getConfigByCategoryAndServiceAndVersion(String categoryName, String serviceName, int version) {
        return repository.findByCategoryNameAndServiceNameAndVersion(categoryName, serviceName, version);
    }
}