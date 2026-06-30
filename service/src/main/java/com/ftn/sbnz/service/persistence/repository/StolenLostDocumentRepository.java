package com.ftn.sbnz.service.persistence.repository;

import com.ftn.sbnz.service.persistence.entity.StolenLostDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StolenLostDocumentRepository extends JpaRepository<StolenLostDocumentEntity, Long> {
    Optional<StolenLostDocumentEntity> findByDocumentNumberAndActiveTrue(String documentNumber);
}