package com.oneassist.ccf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
                String uuid = UUID.randomUUID().toString().substring(0, 8);
                serviceRequest.setSrId("sr_"  + uuid);
            } else {
                serviceRequest.setSrId(requestDTO.getSrId());
            }

            // Set basic service request properties
            serviceRequest.setClaimType(requestDTO.getClaimType());
            serviceRequest.setStatus("In Progress");  // Initial status for new requests
            serviceRequest.setCurrentStage("Document Upload");  // Initial stage
            serviceRequest.setCategory(requestDTO.getCategory());
            serviceRequest.setCustomerName(requestDTO.getCustomerName());
            serviceRequest.setContact(requestDTO.getContact());
            serviceRequest.setDeviceMake(requestDTO.getDeviceMake());
            serviceRequest.setCreatedDate(LocalDateTime.now());
            serviceRequest.setLastUpdated(LocalDateTime.now());

            // Convert stageData to JSON string
            if (requestDTO.getStageData() != null) {
                String stageDataJson = objectMapper.writeValueAsString(requestDTO.getStageData());
                serviceRequest.setStageData(stageDataJson);
            }

            // Save the service request
            ServiceRequestEntity savedRequest = serviceRequestRepository.save(serviceRequest);

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
        ServiceRequestEntity serviceRequest = serviceRequestRepository.findBySrId(srId)
                .orElseThrow(() -> new RuntimeException("Service request not found with ID: " + srId));

        return mapToDTO(serviceRequest);
    }

    /**
     * Update an existing service request
     */
    @Transactional
    public ServiceRequestDTO updateServiceRequest(String srId, ServiceRequestDTO requestDTO) {
        try {
            ServiceRequestEntity serviceRequest = serviceRequestRepository.findBySrId(srId)
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
            ServiceRequestEntity updatedRequest = serviceRequestRepository.save(serviceRequest);

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
            ServiceRequestEntity serviceRequest = serviceRequestRepository.findBySrId(srId)
                    .orElseThrow(() -> new RuntimeException("Service request not found with ID: " + srId));

            ConfigRequest.Stage currentStage = getStage(serviceRequest.getCurrentStage());
            if (currentStage==null){
                throw new RuntimeException("No next stage found for current stage: " + serviceRequest.getCurrentStage());
            }

            String nextStage = getNextStage(currentStage);
            if (nextStage == null) {
                serviceRequest.setStatus("Completed");
            } else {
                serviceRequest.setCurrentStage(nextStage);
            }

            // Update stage data if provided
            if (requestDTO.getStageData() != null) {
                String stageDataJson = objectMapper.writeValueAsString(requestDTO.getStageData());
                serviceRequest.setStageData(stageDataJson);
            }

            serviceRequest.setLastUpdated(LocalDateTime.now());

            // Save the updated service request
            ServiceRequestEntity updatedRequest = serviceRequestRepository.save(serviceRequest);

            return mapToDTO(updatedRequest);

        } catch (JsonProcessingException e) {
            log.error("Error processing JSON data for service request", e);
            throw new RuntimeException("Failed to process claim stage data", e);
        }
    }

    public ConfigRequest.Stage getStage(String currentStage) throws JsonProcessingException {

        ProductCategoryEntity productCategoryEntity = productCategoryRepository.findByCategoryName("PE")
                .orElseThrow(() -> new RuntimeException("Product category not found with name: PE"));

        List<ConfigRequest.Stage> stages = objectMapper.readValue(productCategoryEntity.getConfiguration(), new TypeReference<List<ConfigRequest.Stage>>() {});


        for (ConfigRequest.Stage stage : stages) {
            if (stage.getStageName().equalsIgnoreCase(currentStage)) {
                return stage;
            }
        }

        return null;
    }

    public String getNextStage(ConfigRequest.Stage currentStage){

        if (currentStage.getActions()==null || currentStage.getActions().isEmpty()){
            return null;
        }

        return currentStage.getActions().stream()
                .filter(action -> action.getOption().equalsIgnoreCase("Submit"))
                .map(action -> action.getStage())
                .findFirst().orElse(null);
    }

    /**
     * Convert entity to DTO
     */
    private ServiceRequestDTO mapToDTO(ServiceRequestEntity serviceRequest) {
        ServiceRequestDTO dto = new ServiceRequestDTO();
        dto.setSrId(serviceRequest.getSrId());
        dto.setCreatedDate(serviceRequest.getCreatedDate());
        dto.setClaimType(serviceRequest.getClaimType());
        dto.setStatus(serviceRequest.getStatus());
        dto.setCurrentStage(serviceRequest.getCurrentStage());
        dto.setLastUpdated(serviceRequest.getLastUpdated());
        dto.setCustomerName(serviceRequest.getCustomerName());
        dto.setCategory(serviceRequest.getCategory());
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
