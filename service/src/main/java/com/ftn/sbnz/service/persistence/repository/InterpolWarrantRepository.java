package com.ftn.sbnz.service.persistence.repository;

import com.ftn.sbnz.service.persistence.entity.InterpolWarrantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InterpolWarrantRepository extends JpaRepository<InterpolWarrantEntity, Long> {
    Optional<InterpolWarrantEntity> findByDocumentNumberAndActiveTrue(String documentNumber);
}