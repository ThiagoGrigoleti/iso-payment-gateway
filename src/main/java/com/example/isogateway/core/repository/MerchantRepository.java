package com.example.isogateway.core.repository;

import com.example.isogateway.core.domain.MerchantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<MerchantEntity, Long> {

    Optional<MerchantEntity> findByApiKeyHashAndActiveTrue(String apiKeyHash);

    Optional<MerchantEntity> findByApiKeyPrefixAndActiveTrue(String apiKeyPrefix);

    boolean existsByApiKeyHash(String apiKeyHash);
}
