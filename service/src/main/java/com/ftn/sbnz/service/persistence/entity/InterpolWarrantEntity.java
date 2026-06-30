package com.ftn.sbnz.service.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "interpol_warrant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterpolWarrantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_number", nullable = false, unique = true)
    private String documentNumber;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "nationality", length = 3)
    private String nationality;

    @Column(name = "reason")
    private String reason;

    @Column(name = "issued_at")
    private LocalDate issuedAt;

    @Column(name = "expires_at")
    private LocalDate expiresAt;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}