package com.ftn.sbnz.service.persistence.repository;

import com.ftn.sbnz.service.persistence.entity.CrossingEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrossingEventRepository extends JpaRepository<CrossingEventEntity, Long> {
    List<CrossingEventEntity> findByPlateNumber(String plateNumber);
}