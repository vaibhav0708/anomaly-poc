package org.vaibhav.poc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vaibhav.poc.model.PartnerEvent;

@Repository
public interface PartnerEventRepository extends JpaRepository<PartnerEvent, Long> {
}
