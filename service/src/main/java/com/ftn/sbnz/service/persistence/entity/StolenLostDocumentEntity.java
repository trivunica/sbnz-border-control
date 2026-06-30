package com.ftn.sbnz.service.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "stolen_lost_document")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StolenLostDocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_number", nullable = false, unique = true)
    private String documentNumber;

    @Column(name = "document_type", nullable = false, length = 20)
    private String documentType;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "reported_by")
    private String reportedBy;

    @Column(name = "reported_at")
    private LocalDate reportedAt;

    @Column(name = "reason", nullable = false, length = 20)
    private String reason;   // "STOLEN" | "LOST"

    @Column(name = "active", nullable = false)
    private boolean active = true;
}