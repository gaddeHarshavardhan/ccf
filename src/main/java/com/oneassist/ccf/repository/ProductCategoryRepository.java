package com.oneassist.ccf.repository;

import com.oneassist.ccf.entity.CategoryServiceConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductCategoryRepository extends JpaRepository<CategoryServiceConfigEntity, Long> {

    Optional<CategoryServiceConfigEntity> findFirstByCategoryNameAndServiceNameOrderByVersionDesc(String categoryName, String serviceName);

    Optional<CategoryServiceConfigEntity> findByCategoryNameAndServiceName(String categoryName, String serviceName);

    Optional<CategoryServiceConfigEntity> findByCategoryNameAndServiceNameAndVersion(String categoryName, String serviceName, int version);

}
