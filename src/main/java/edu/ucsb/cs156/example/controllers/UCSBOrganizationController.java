package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for UCSBOrganization */
@Tag(name = "UCSBOrganization", description = "UCSB student organizations")
@RequestMapping("/api/UCSBOrganization")
@RestController
@Slf4j
public class UCSBOrganizationController extends ApiController {

  @Autowired UCSBOrganizationRepository ucsbOrganizationRepository;

  /**
   * Return all UCSB organizations. Accessible to logged-in users.
   *
   * @return a list of all UCSB organizations
   */
  @Operation(summary = "List all UCSB organizations")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBOrganization> allOrganizations() {
    return ucsbOrganizationRepository.findAll();
  }

  /**
   * Look up an organization by its code. Accessible to logged-in users.
   *
   * @param id the organization's orgCode
   * @return the matching organization
   */
  @Operation(summary = "Get a UCSB organization")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public UCSBOrganization getById(
      @Parameter(description = "Organization code to look up") @RequestParam String id) {
    return ucsbOrganizationRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("id %s not found".formatted(id)));
  }

  /**
   * Delete an organization. Accessible only to administrators.
   *
   * @param id the organization's orgCode
   * @return a message confirming deletion
   */
  @Operation(summary = "Delete a UCSB organization")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteOrganization(
      @Parameter(description = "Organization code to delete") @RequestParam String id) {
    UCSBOrganization organization =
        ucsbOrganizationRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("record %s not found".formatted(id)));
    ucsbOrganizationRepository.delete(organization);
    return genericMessage("record %s deleted".formatted(id));
  }

  /**
   * Update an organization's non-key fields. Accessible only to administrators.
   *
   * @param id the organization's orgCode, which remains unchanged
   * @param incoming the new short name, full name, and inactive flag
   * @return the updated organization
   */
  @Operation(summary = "Update a UCSB organization")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public UCSBOrganization updateOrganization(
      @Parameter(description = "Organization code to update") @RequestParam String id,
      @RequestBody @Valid UCSBOrganization incoming) {
    UCSBOrganization organization =
        ucsbOrganizationRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("id %s not found".formatted(id)));
    organization.setOrgTranslationShort(incoming.getOrgTranslationShort());
    organization.setOrgTranslation(incoming.getOrgTranslation());
    organization.setInactive(incoming.getInactive());
    ucsbOrganizationRepository.save(organization);
    return organization;
  }

  /**
   * Create a UCSB organization. Accessible only to administrators.
   *
   * @param orgCode unique organization code
   * @param orgTranslationShort short organization name
   * @param orgTranslation full organization name
   * @param inactive whether the organization is inactive
   * @return the saved organization
   */
  @Operation(summary = "Create a UCSB organization")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBOrganization postOrganization(
      @Parameter(description = "Unique organization code") @RequestParam String orgCode,
      @Parameter(description = "Short organization name") @RequestParam String orgTranslationShort,
      @Parameter(description = "Full organization name") @RequestParam String orgTranslation,
      @Parameter(description = "Whether the organization is inactive") @RequestParam
          boolean inactive) {
    UCSBOrganization organization = new UCSBOrganization();
    organization.setOrgCode(orgCode);
    organization.setOrgTranslationShort(orgTranslationShort);
    organization.setOrgTranslation(orgTranslation);
    organization.setInactive(inactive);
    return ucsbOrganizationRepository.save(organization);
  }
}
