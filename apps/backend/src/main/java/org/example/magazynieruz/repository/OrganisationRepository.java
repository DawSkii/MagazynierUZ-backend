package org.example.magazynieruz.repository;

import org.example.magazynieruz.model.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganisationRepository extends JpaRepository<Organisation, Long> {
    boolean existsByTIN(String TIN);
}
