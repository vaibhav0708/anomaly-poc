package org.vaibhav.poc.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// simplified for zip
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartnerEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String partnerId;

    @Column(columnDefinition = "TEXT")
    private String eventJson;

    @Column(columnDefinition = "TEXT")
    private String normalizedText;
}
