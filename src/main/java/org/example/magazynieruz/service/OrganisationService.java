package org.example.magazynieruz.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.magazynieruz.dto.organisation.CreateOrganisationRequest;
import org.example.magazynieruz.dto.organisation.OrganisationResponse;
import org.example.magazynieruz.dto.organisation.UpdateOrganisationRequest;
import org.example.magazynieruz.mapper.OrganisationMapper;
import org.example.magazynieruz.model.Organisation;
import org.example.magazynieruz.repository.OrganisationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing organisations.
 * Provides CRUD operations for organisation entities.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final OrganisationMapper organisationMapper;

    /**
     * Creates a new organisation.
     *
     * @param request the organisation creation request
     * @return OrganisationResponse containing created organisation details
     * @throws IllegalArgumentException if organisation name already exists
     */
    @Transactional
    public OrganisationResponse createOrganisation(CreateOrganisationRequest request) {
        log.info("Creating organisation with name: {}", request.name());
        
        if (organisationRepository.findAll().stream()
                .anyMatch(o -> o.getName().equals(request.name()))) {
            throw new IllegalArgumentException("Organisation with name " + request.name() + " already exists");
        }
        
        Organisation organisation = organisationMapper.toEntity(request);
        Organisation saved = organisationRepository.save(organisation);
        
        log.info("Organisation created with ID: {}", saved.getId());
        return organisationMapper.toResponse(saved);
    }

    /**
     * Retrieves all organisations in the system.
     *
     * @return list of all organisations
     */
    @Transactional(readOnly = true)
    public List<OrganisationResponse> getAllOrganisations() {
        log.info("Fetching all organisations");
        return organisationRepository.findAll().stream()
                .map(organisationMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific organisation by ID.
     *
     * @param id the organisation ID
     * @return OrganisationResponse containing organisation details
     * @throws EntityNotFoundException if organisation not found
     */
    @Transactional(readOnly = true)
    public OrganisationResponse getOrganisationById(Long id) {
        log.info("Fetching organisation with ID: {}", id);
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found with ID: " + id));
        return organisationMapper.toResponse(organisation);
    }

    /**
     * Updates an existing organisation.
     *
     * @param id the organisation ID
     * @param request the update request
     * @return OrganisationResponse containing updated organisation details
     * @throws EntityNotFoundException if organisation not found
     */
    @Transactional
    public OrganisationResponse updateOrganisation(Long id, UpdateOrganisationRequest request) {
        log.info("Updating organisation with ID: {}", id);
        
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found with ID: " + id));
        
        if (request.name() != null) {
            organisation.setName(request.name());
        }
        if (request.tin() != null) {
            organisation.setTIN(request.tin());
        }
        
        Organisation updated = organisationRepository.save(organisation);
        log.info("Organisation updated with ID: {}", updated.getId());
        
        return organisationMapper.toResponse(updated);
    }

    /**
     * Deletes an organisation from the system.
     *
     * @param id the organisation ID
     * @throws EntityNotFoundException if organisation not found
     */
    @Transactional
    public void deleteOrganisation(Long id) {
        log.info("Deleting organisation with ID: {}", id);
        
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found with ID: " + id));
        
        organisationRepository.delete(organisation);
        log.info("Organisation deleted with ID: {}", id);
    }
}
