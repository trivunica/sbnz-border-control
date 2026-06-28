package com.ftn.sbnz.service.persistence.repository;

import com.ftn.sbnz.service.persistence.entity.CepAlarmEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CepAlarmRepository extends JpaRepository<CepAlarmEntity, Long> {
    List<CepAlarmEntity> findByPlateNumber(String plateNumber);
}
