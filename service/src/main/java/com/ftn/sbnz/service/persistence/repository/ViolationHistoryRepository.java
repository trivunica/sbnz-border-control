package com.ftn.sbnz.service.persistence.repository;

import com.ftn.sbnz.service.persistence.entity.ViolationHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ViolationHistoryRepository extends JpaRepository<ViolationHistoryEntity, Long> {
    List<ViolationHistoryEntity> findByPlateNumberAndViolationDateAfter(
            String plateNumber, LocalDate since);

    List<ViolationHistoryEntity> findByDriverLicenceNumberAndViolationDateAfter(
            String driverLicenceNumber, LocalDate since);

    @Query("SELECT DISTINCT v.driverLicenceNumber FROM ViolationHistoryEntity v " +
            "WHERE v.companyName = :company AND v.driverLicenceNumber IS NOT NULL")
    List<String> findDistinctDriversByCompany(@Param("company") String company);

    @Query("SELECT DISTINCT v.plateNumber FROM ViolationHistoryEntity v WHERE v.driverLicenceNumber = :licenceNumber AND v.plateNumber IS NOT NULL")
    List<String> findDistinctPlatesByDriver(@Param("licenceNumber") String licenceNumber);
}