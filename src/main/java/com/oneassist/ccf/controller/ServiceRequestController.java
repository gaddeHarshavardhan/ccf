package com.oneassist.ccf.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oneassist.ccf.contract.ServiceRequestDTO;
import com.oneassist.ccf.service.ServiceRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/claims")
public class ServiceRequestController {

    private static final Logger log = LoggerFactory.getLogger(ServiceRequestController.class);

    @Autowired
    private ServiceRequestService serviceRequestService;

    @PostMapping
    public ResponseEntity<ServiceRequestDTO> createServiceRequest(@RequestBody ServiceRequestDTO requestDTO) throws JsonProcessingException {

        log.info("Creating new service request for claim type: {}", requestDTO.getClaimType());

        final ServiceRequestDTO createdRequest = serviceRequestService.createServiceRequest(requestDTO);

        log.info("Service request created successfully with ID: {}", createdRequest.getSrId());

        return new ResponseEntity<>(createdRequest, HttpStatus.CREATED);
    }

    @GetMapping("/{srId}")
    public ResponseEntity<ServiceRequestDTO> getServiceRequestById(@PathVariable String srId) {

        log.info("Fetching service request with ID: {}", srId);

        final ServiceRequestDTO requestDTO = serviceRequestService.findServiceRequestById(srId);
        return ResponseEntity.ok(requestDTO);
    }

    @PostMapping("/{srId}")
    public ResponseEntity<ServiceRequestDTO> updateServiceRequest(@PathVariable String srId,
                                                                  @RequestBody ServiceRequestDTO requestDTO) {

        log.info("Updating service request with ID: {}", srId);

        final ServiceRequestDTO updatedRequest = serviceRequestService.updateServiceRequest(srId, requestDTO);

        log.info("Service request updated successfully: {}", srId);

        return ResponseEntity.ok(updatedRequest);
    }

    @PostMapping("/{srId}/submit")
    public ResponseEntity<ServiceRequestDTO> submitServiceRequest(@PathVariable String srId,
                                                                  @RequestBody ServiceRequestDTO requestDTO) {

        log.info("Updating service request with ID: {}", srId);

        final ServiceRequestDTO updatedRequest = serviceRequestService.submitServiceRequest(srId, requestDTO);

        log.info("Service request updated successfully: {}", srId);

        return ResponseEntity.ok(updatedRequest);
    }
}
