package org.vaibhav.poc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyResult {
    private boolean isAnomaly;
    private String reason;
}
