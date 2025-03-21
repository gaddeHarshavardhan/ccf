package com.oneassist.ccf.repository;

import com.oneassist.ccf.entity.CategoryServiceConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCategoryRepository extends JpaRepository<CategoryServiceConfigEntity, Long> {

    Optional<CategoryServiceConfigEntity> findFirstByCategoryNameAndServiceNameOrderByVersionDesc(String categoryName, String serviceName);

    Optional<CategoryServiceConfigEntity> findByCategoryNameAndServiceName(String categoryName, String serviceName);

    Optional<CategoryServiceConfigEntity> findByCategoryNameAndServiceNameAndVersion(String categoryName, String serviceName, int version);

    @Query("SELECT c FROM CategoryServiceConfigEntity c " +
            "WHERE (c.categoryName, c.serviceName, c.version) IN " +
            "(SELECT c2.categoryName, c2.serviceName, MAX(c2.version) " +
            "FROM CategoryServiceConfigEntity c2 " +
            "GROUP BY c2.categoryName, c2.serviceName)")
    List<CategoryServiceConfigEntity> findLatestVersionsForEachCategoryAndService();}
