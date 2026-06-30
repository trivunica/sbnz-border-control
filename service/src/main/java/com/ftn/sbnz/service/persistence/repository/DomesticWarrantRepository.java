package com.ftn.sbnz.service.persistence.repository;

import com.ftn.sbnz.service.persistence.entity.DomesticWarrantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DomesticWarrantRepository extends JpaRepository<DomesticWarrantEntity, Long> {
    Optional<DomesticWarrantEntity> findByDocumentNumberAndActiveTrue(String documentNumber);
}