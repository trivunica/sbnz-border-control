package com.ftn.sbnz.service.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "cep_alarms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CepAlarmEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;
    private String plateNumber;
    private String message;
    private LocalDateTime crossedAt;
}