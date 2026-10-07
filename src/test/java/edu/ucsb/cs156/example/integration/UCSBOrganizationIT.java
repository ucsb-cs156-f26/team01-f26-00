package edu.ucsb.cs156.example.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Verify the organization endpoints against a real database initialized by Liquibase. */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:organization-it;DB_CLOSE_DELAY=-1",
      "spring.jpa.hibernate.ddl-auto=validate"
    })
@AutoConfigureMockMvc
@ActiveProfiles("development")
@Transactional
public class UCSBOrganizationIT {
  @Autowired MockMvc mockMvc;
  @Autowired EntityManager entityManager;
  @Autowired UCSBOrganizationRepository ucsbOrganizationRepository;

  @Test
  @WithMockUser(roles = {"ADMIN", "USER"})
  public void posted_organization_is_persisted_and_returned_by_get_all() throws Exception {
    mockMvc
        .perform(
            post("/api/UCSBOrganization/post")
                .param("orgCode", "SKY")
                .param("orgTranslationShort", "SKYDIVING CLUB")
                .param("orgTranslation", "SKYDIVING CLUB AT UCSB")
                .param("inactive", "false")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orgCode").value("SKY"))
        .andExpect(jsonPath("$.orgTranslationShort").value("SKYDIVING CLUB"))
        .andExpect(jsonPath("$.orgTranslation").value("SKYDIVING CLUB AT UCSB"))
        .andExpect(jsonPath("$.inactive").value(false));

    assertEquals(
        "SKYDIVING CLUB AT UCSB",
        ucsbOrganizationRepository.findById("SKY").orElseThrow().getOrgTranslation());

    mockMvc
        .perform(get("/api/UCSBOrganization/all"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].orgCode").value("SKY"))
        .andExpect(jsonPath("$[0].orgTranslationShort").value("SKYDIVING CLUB"))
        .andExpect(jsonPath("$[0].orgTranslation").value("SKYDIVING CLUB AT UCSB"))
        .andExpect(jsonPath("$[0].inactive").value(false));
  }

  @Test
  public void swagger_documents_both_routes_and_the_creation_parameters() throws Exception {
    mockMvc
        .perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paths['/api/UCSBOrganization/all'].get.summary").exists())
        .andExpect(jsonPath("$.paths['/api/UCSBOrganization/post'].post.summary").exists())
        .andExpect(
            jsonPath("$.paths['/api/UCSBOrganization/post'].post.parameters.length()").value(4))
        .andExpect(
            jsonPath("$.paths['/api/UCSBOrganization/post'].post.parameters[0].name")
                .value("orgCode"))
        .andExpect(
            jsonPath("$.paths['/api/UCSBOrganization/post'].post.parameters[0].description")
                .value("Unique organization code"));
  }

  @Test
  @WithMockUser(roles = {"ADMIN", "USER"})
  public void single_record_routes_read_update_and_delete_persisted_records() throws Exception {
    ucsbOrganizationRepository.save(new UCSBOrganization("123", "Short name", "Full name", false));
    entityManager.flush();
    entityManager.clear();
    mockMvc
        .perform(get("/api/UCSBOrganization").param("id", "123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orgTranslation").value("Full name"));
    mockMvc
        .perform(
            put("/api/UCSBOrganization")
                .param("id", "123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"orgCode\":\"DIFFERENT\",\"orgTranslationShort\":\"New short name\",\"orgTranslation\":\"New full name\",\"inactive\":true}")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orgCode").value("123"))
        .andExpect(jsonPath("$.inactive").value(true));
    entityManager.flush();
    entityManager.clear();
    mockMvc
        .perform(get("/api/UCSBOrganization").param("id", "123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orgTranslationShort").value("New short name"))
        .andExpect(jsonPath("$.orgTranslation").value("New full name"))
        .andExpect(jsonPath("$.inactive").value(true));
    assertFalse(ucsbOrganizationRepository.existsById("DIFFERENT"));
    mockMvc
        .perform(delete("/api/UCSBOrganization").param("id", "123").with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("record 123 deleted"));
    entityManager.flush();
    entityManager.clear();
    assertFalse(ucsbOrganizationRepository.existsById("123"));
    mockMvc
        .perform(get("/api/UCSBOrganization").param("id", "123"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("id 123 not found"));
    mockMvc
        .perform(delete("/api/UCSBOrganization").param("id", "123").with(csrf()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("record 123 not found"));
  }

  @Test
  public void swagger_documents_all_single_record_routes() throws Exception {
    for (String method : new String[] {"get", "delete", "put"}) {
      String operation = "$.paths['/api/UCSBOrganization']." + method;
      mockMvc
          .perform(get("/v3/api-docs"))
          .andExpect(status().isOk())
          .andExpect(jsonPath(operation + ".summary").exists())
          .andExpect(jsonPath(operation + ".description").exists())
          .andExpect(jsonPath(operation + ".parameters[0].name").value("id"))
          .andExpect(jsonPath(operation + ".parameters[0].required").value(true))
          .andExpect(jsonPath(operation + ".parameters[0].schema.type").value("string"));
    }
  }
}
