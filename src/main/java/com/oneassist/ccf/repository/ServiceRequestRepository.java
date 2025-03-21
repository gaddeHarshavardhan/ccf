package com.oneassist.ccf.repository;

import com.oneassist.ccf.entity.ServiceRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequestEntity, String> {

    Optional<ServiceRequestEntity> findBySrId(String srId);
}
