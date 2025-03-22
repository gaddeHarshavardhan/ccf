package com.oneassist.ccf.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneassist.ccf.enums.Category;
import com.oneassist.ccf.enums.Option;
import com.oneassist.ccf.enums.ServiceEnum;
import com.oneassist.ccf.enums.ServiceStatus;
import com.oneassist.ccf.contract.ConfigRequest;
import com.oneassist.ccf.contract.ServiceRequestDTO;
import com.oneassist.ccf.entity.CategoryServiceConfigEntity;
import com.oneassist.ccf.entity.ServiceRequestEntity;
import com.oneassist.ccf.repository.ProductCategoryRepository;
import com.oneassist.ccf.repository.ServiceRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ServiceRequestService {

    private static final Logger log = LoggerFactory.getLogger(ServiceRequestService.class);

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    /**
     * Create a new service request with initial data
     */
    @Transactional
    public ServiceRequestDTO createServiceRequest(ServiceRequestDTO requestDTO) throws JsonProcessingException {
        try {
            final ServiceRequestEntity serviceRequest = new ServiceRequestEntity();

            // Generate a unique SR ID if not provided
            if (requestDTO.getSrId() == null || requestDTO.getSrId().isEmpty()) {
                final String uuid = UUID.randomUUID().toString().substring(0, 8);
                serviceRequest.setSrId("sr_" + uuid);
            } else {
                serviceRequest.setSrId(requestDTO.getSrId());
            }

            // Fetch the latest configuration for the given category and service
            final Optional<CategoryServiceConfigEntity> optional = productCategoryRepository.findFirstByCategoryNameAndServiceNameOrderByVersionDesc(
                    requestDTO.getCategory(), requestDTO.getService());
            if (optional.isEmpty()) {
                throw new RuntimeException("No configuration found for category: " + requestDTO.getCategory() + ", service: " + requestDTO.getService());
            }

            final List<ConfigRequest.Stage> stages = objectMapper.readValue(optional.get().getConfiguration(), new TypeReference<>() {
            });

            // Set basic service request properties
            serviceRequest.setClaimType(requestDTO.getClaimType());
            serviceRequest.setStatus(ServiceStatus.IN_PROGRESS.getStatus());
            serviceRequest.setCurrentStage(stages.get(0).getStageName());
            serviceRequest.setConfigVersion(optional.get().getVersion());
            serviceRequest.setCategory(requestDTO.getCategory());
            serviceRequest.setService(requestDTO.getService());
            serviceRequest.setCustomerName(requestDTO.getCustomerName());
            serviceRequest.setContact(requestDTO.getContact());
            serviceRequest.setDeviceMake(requestDTO.getDeviceMake());
            serviceRequest.setCreatedDate(LocalDateTime.now());
            serviceRequest.setLastUpdated(LocalDateTime.now());

            final Category category = Category.valueOf(requestDTO.getCategory());
            final ServiceEnum service = ServiceEnum.valueOf(requestDTO.getService());

            serviceRequest.setClaimType(category.getDisplayName() + " " + service.getDisplayName());

            // Convert stageData to JSON string
            if (requestDTO.getStageData() != null) {
                final String stageDataJson = objectMapper.writeValueAsString(requestDTO.getStageData());
                serviceRequest.setStageData(stageDataJson);
            }

            // Save the service request
            final ServiceRequestEntity savedRequest = serviceRequestRepository.save(serviceRequest);

            // Create and return DTO with the generated SR ID
            return mapToDTO(savedRequest);

        } catch (JsonProcessingException e) {
            log.error("Error processing JSON data for service request", e);
            throw new RuntimeException("Failed to process claim stage data", e);
        }
    }

    /**
     * Find a service request by its SR ID
     */
    @Transactional(readOnly = true)
    public ServiceRequestDTO findServiceRequestById(String srId) {
        final ServiceRequestEntity serviceRequest = serviceRequestRepository.findBySrId(srId)
                .orElseThrow(() -> new RuntimeException("Service request not found with ID: " + srId));

        return mapToDTO(serviceRequest);
    }

    /**
     * Update an existing service request
     */
    @Transactional
    public ServiceRequestDTO updateServiceRequest(String srId, ServiceRequestDTO requestDTO) {
        try {
            final ServiceRequestEntity serviceRequest = serviceRequestRepository.findBySrId(srId)
                    .orElseThrow(() -> new RuntimeException("Service request not found with ID: " + srId));

            // Update the fields that can be modified
            if (requestDTO.getStatus() != null) {
                serviceRequest.setStatus(requestDTO.getStatus());
            }

            // Update stage data if provided
            if (requestDTO.getStageData() != null) {
                String stageDataJson = objectMapper.writeValueAsString(requestDTO.getStageData());
                serviceRequest.setStageData(stageDataJson);
            }

            serviceRequest.setLastUpdated(LocalDateTime.now());

            // Save the updated service request
            final ServiceRequestEntity updatedRequest = serviceRequestRepository.save(serviceRequest);

            return mapToDTO(updatedRequest);

        } catch (JsonProcessingException e) {
            log.error("Error processing JSON data for service request", e);
            throw new RuntimeException("Failed to process claim stage data", e);
        }
    }

    @Transactional
    public ServiceRequestDTO submitServiceRequest(String srId, ServiceRequestDTO requestDTO) {
        log.info("Submitting service request with ID: {}", srId);
        try {
            final ServiceRequestEntity serviceRequest = serviceRequestRepository.findBySrId(srId)
                    .orElseThrow(() -> new RuntimeException("Service request not found with ID: " + srId));

            final ConfigRequest.Stage currentStage = getStage(serviceRequest);
            if (currentStage == null) {
                throw new RuntimeException("No next stage found for current stage: " + serviceRequest.getCurrentStage());
            }

            final String nextStage = determineNextStage(requestDTO, currentStage);
            if (nextStage == null || nextStage.isEmpty()) {
                serviceRequest.setStatus(ServiceStatus.COMPLETED.getStatus());
            } else {
                serviceRequest.setCurrentStage(nextStage);
            }

            // Update stage data if provided
            if (requestDTO.getStageData() != null) {
                final String stageDataJson = objectMapper.writeValueAsString(requestDTO.getStageData());
                serviceRequest.setStageData(stageDataJson);
            }

            serviceRequest.setLastUpdated(LocalDateTime.now());

            // Save the updated service request
            final ServiceRequestEntity updatedRequest = serviceRequestRepository.save(serviceRequest);

            return mapToDTO(updatedRequest);

        } catch (JsonProcessingException e) {
            log.error("Error processing JSON data for service request", e);
            throw new RuntimeException("Failed to process claim stage data", e);
        }
    }

    public String determineNextStage(ServiceRequestDTO serviceRequest, ConfigRequest.Stage stageConfig) {
        // Extract the current stage name and its data
        final String currentStageName = serviceRequest.getCurrentStage();
        final Map<String, Object> currentStageData = getStageData(serviceRequest, currentStageName);

        if (currentStageData == null || stageConfig == null) {
            return null;
        }

        final ConfigRequest.Action submitAction = stageConfig.getActions().stream()
                .filter(action -> action.getOption().equalsIgnoreCase(Option.SUBMIT.getValue()))
                .findAny().orElse(null);

        if (submitAction == null) {
            throw new RuntimeException("No submit action found for current stage: " + currentStageName);
        }

        if (submitAction.getConditions() == null || submitAction.getConditions().isEmpty()) {
            return submitAction.getStage();
        }

        // Check each action to find a valid transition
        boolean allConditionsMet = true;

        // Verify if all conditions for this action are met
        for (ConfigRequest.Condition condition : submitAction.getConditions()) {
            String fieldName = condition.getField();
            String operator = condition.getOperator();
            String expectedValue = condition.getValue();

            // Get the actual value from the stage data
            Object actualValue = currentStageData.get(fieldName);

            // Check if the condition is met
            if (!isConditionMet(actualValue, operator, expectedValue)) {
                allConditionsMet = false;
                break;
            }
        }

        // If all conditions are met, this is our next stage
        if (allConditionsMet) {
            return submitAction.getStage();
        }

        // No valid transition found
        return null;
    }

    private Map<String, Object> getStageData(ServiceRequestDTO serviceRequest, String stageName) {
        if (serviceRequest.getStageData() instanceof Map) {
            Map<String, Object> allStageData = (Map<String, Object>) serviceRequest.getStageData();
            return (Map<String, Object>) allStageData.get(stageName);
        }
        return null;
    }

    private boolean isConditionMet(Object actualValue, String operator, String expectedValue) {
        if (actualValue == null) {
            return false;
        }

        String strValue = actualValue.toString();

        switch (operator.toLowerCase()) {
            case "equals":
                return strValue.equals(expectedValue);
            case "not_equals":
                return !strValue.equals(expectedValue);
            case "contains":
                return strValue.contains(expectedValue);
            case "starts_with":
                return strValue.startsWith(expectedValue);
            case "ends_with":
                return strValue.endsWith(expectedValue);
            case "greater_than":
                try {
                    double numActual = Double.parseDouble(strValue);
                    double numExpected = Double.parseDouble(expectedValue);
                    return numActual > numExpected;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "less_than":
                try {
                    double numActual = Double.parseDouble(strValue);
                    double numExpected = Double.parseDouble(expectedValue);
                    return numActual < numExpected;
                } catch (NumberFormatException e) {
                    return false;
                }
            default:
                return false;
        }
    }

    public ConfigRequest.Stage getStage(ServiceRequestEntity serviceRequest) throws JsonProcessingException {

        final CategoryServiceConfigEntity categoryServiceConfigEntity = productCategoryRepository.findByCategoryNameAndServiceNameAndVersion(
                        serviceRequest.getCategory(), serviceRequest.getService(), serviceRequest.getConfigVersion())
                .orElseThrow(() -> new RuntimeException("Product category not found with name: PE"));

        final List<ConfigRequest.Stage> stages = objectMapper.readValue(categoryServiceConfigEntity.getConfiguration(), new TypeReference<List<ConfigRequest.Stage>>() {
        });


        for (ConfigRequest.Stage stage : stages) {
            if (stage.getStageName().equalsIgnoreCase(serviceRequest.getCurrentStage())) {
                return stage;
            }
        }

        return null;
    }

    /**
     * Convert entity to DTO
     */
    private ServiceRequestDTO mapToDTO(ServiceRequestEntity serviceRequest) {
        final ServiceRequestDTO dto = new ServiceRequestDTO();
        dto.setSrId(serviceRequest.getSrId());
        dto.setCreatedDate(serviceRequest.getCreatedDate());
        dto.setClaimType(serviceRequest.getClaimType());
        dto.setStatus(serviceRequest.getStatus());
        dto.setCurrentStage(serviceRequest.getCurrentStage());
        dto.setLastUpdated(serviceRequest.getLastUpdated());
        dto.setCustomerName(serviceRequest.getCustomerName());
        dto.setCategory(serviceRequest.getCategory());
        dto.setService(serviceRequest.getService());
        dto.setConfigVersion(serviceRequest.getConfigVersion());
        dto.setDeviceMake(serviceRequest.getDeviceMake());
        dto.setContact(serviceRequest.getContact());

        // Convert JSON string to Object
        if (serviceRequest.getStageData() != null && !serviceRequest.getStageData().isEmpty()) {
            try {
                Object stageData = objectMapper.readValue(serviceRequest.getStageData(), Object.class);
                dto.setStageData(stageData);
            } catch (JsonProcessingException e) {
                log.error("Error deserializing stage data JSON", e);
                dto.setStageData(serviceRequest.getStageData());
            }
        }

        return dto;
    }
}
