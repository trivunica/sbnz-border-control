package com.ftn.sbnz.service.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "crossing_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CrossingEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String plateNumber;
    private String driverDocumentNumber;
    private String borderCrossingId;
    private String companyName;
    private String destinationCountry;
    private LocalDateTime crossedAt;
}
