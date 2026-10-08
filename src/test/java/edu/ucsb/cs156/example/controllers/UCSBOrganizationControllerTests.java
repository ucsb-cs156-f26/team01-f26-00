package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {

  @MockitoBean UCSBOrganizationRepository ucsbOrganizationRepository;
  @MockitoBean UserRepository userRepository;

  private MockHttpServletRequestBuilder organizationPost(boolean inactive) {
    return post("/api/UCSBOrganization/post")
        .param("orgCode", "SKY")
        .param("orgTranslationShort", "SKYDIVING CLUB")
        .param("orgTranslation", "SKYDIVING CLUB AT UCSB")
        .param("inactive", Boolean.toString(inactive));
  }

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/UCSBOrganization/all")).andExpect(status().isForbidden());
    verifyNoInteractions(ucsbOrganizationRepository);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_an_empty_list() throws Exception {
    when(ucsbOrganizationRepository.findAll()).thenReturn(List.of());
    mockMvc
        .perform(get("/api/UCSBOrganization/all"))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));
    verify(ucsbOrganizationRepository).findAll();
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all_organizations() throws Exception {
    UCSBOrganization sky =
        UCSBOrganization.builder()
            .orgCode("SKY")
            .orgTranslationShort("SKYDIVING CLUB")
            .orgTranslation("SKYDIVING CLUB AT UCSB")
            .inactive(false)
            .build();
    UCSBOrganization krc =
        UCSBOrganization.builder()
            .orgCode("KRC")
            .orgTranslationShort("KOREAN RADIO CL")
            .orgTranslation("KOREAN RADIO CLUB")
            .inactive(true)
            .build();
    List<UCSBOrganization> organizations = List.of(sky, krc);
    when(ucsbOrganizationRepository.findAll()).thenReturn(organizations);

    MvcResult response =
        mockMvc.perform(get("/api/UCSBOrganization/all")).andExpect(status().isOk()).andReturn();

    verify(ucsbOrganizationRepository).findAll();
    assertEquals(
        mapper.writeValueAsString(organizations), response.getResponse().getContentAsString());
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(organizationPost(false).with(csrf())).andExpect(status().isForbidden());
    verifyNoInteractions(ucsbOrganizationRepository);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_users_cannot_post() throws Exception {
    mockMvc.perform(organizationPost(false).with(csrf())).andExpect(status().isForbidden());
    verifyNoInteractions(ucsbOrganizationRepository);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admins_cannot_post_without_csrf() throws Exception {
    mockMvc.perform(organizationPost(false)).andExpect(status().isForbidden());
    verifyNoInteractions(ucsbOrganizationRepository);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admins_can_post_active_organizations() throws Exception {
    assertAdminCanPost(false);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admins_can_post_inactive_organizations() throws Exception {
    assertAdminCanPost(true);
  }

  private void assertAdminCanPost(boolean inactive) throws Exception {
    UCSBOrganization organization =
        UCSBOrganization.builder()
            .orgCode("SKY")
            .orgTranslationShort("SKYDIVING CLUB")
            .orgTranslation("SKYDIVING CLUB AT UCSB")
            .inactive(inactive)
            .build();
    // A distinct saved value ensures the response comes from the repository.
    UCSBOrganization saved =
        UCSBOrganization.builder()
            .orgCode("SKY")
            .orgTranslationShort("SKYDIVING CLUB")
            .orgTranslation("SAVED SKYDIVING CLUB AT UCSB")
            .inactive(inactive)
            .build();
    when(ucsbOrganizationRepository.save(eq(organization))).thenReturn(saved);

    MvcResult response =
        mockMvc
            .perform(organizationPost(inactive).with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbOrganizationRepository).save(eq(organization));
    assertEquals(mapper.writeValueAsString(saved), response.getResponse().getContentAsString());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admins_cannot_post_with_missing_fields() throws Exception {
    mockMvc
        .perform(post("/api/UCSBOrganization/post").param("orgCode", "SKY").with(csrf()))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(ucsbOrganizationRepository);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admins_cannot_post_with_invalid_inactive_value() throws Exception {
    mockMvc
        .perform(
            post("/api/UCSBOrganization/post")
                .param("orgCode", "SKY")
                .param("orgTranslationShort", "SKYDIVING CLUB")
                .param("orgTranslation", "SKYDIVING CLUB AT UCSB")
                .param("inactive", "invalid")
                .with(csrf()))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(ucsbOrganizationRepository);
  }

  private MockHttpServletRequestBuilder organizationPut() {
    return put("/api/UCSBOrganization")
        .param("id", "SKY")
        .contentType(MediaType.APPLICATION_JSON)
        .content(
            "{\"orgTranslationShort\":\"New short name\",\"orgTranslation\":\"New full name\",\"inactive\":true}");
  }

  @Test
  public void logged_out_users_cannot_access_single_record_routes() throws Exception {
    mockMvc
        .perform(get("/api/UCSBOrganization").param("id", "SKY"))
        .andExpect(status().isForbidden());
    mockMvc
        .perform(delete("/api/UCSBOrganization").param("id", "SKY").with(csrf()))
        .andExpect(status().isForbidden());
    mockMvc.perform(organizationPut().with(csrf())).andExpect(status().isForbidden());
    verifyNoInteractions(ucsbOrganizationRepository);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_users_cannot_delete_or_update() throws Exception {
    mockMvc
        .perform(delete("/api/UCSBOrganization").param("id", "SKY").with(csrf()))
        .andExpect(status().isForbidden());
    mockMvc.perform(organizationPut().with(csrf())).andExpect(status().isForbidden());
    verifyNoInteractions(ucsbOrganizationRepository);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admins_cannot_delete_or_update_without_csrf() throws Exception {
    mockMvc
        .perform(delete("/api/UCSBOrganization").param("id", "SKY"))
        .andExpect(status().isForbidden());
    mockMvc.perform(organizationPut()).andExpect(status().isForbidden());
    verifyNoInteractions(ucsbOrganizationRepository);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void users_can_get_an_existing_organization_by_id() throws Exception {
    UCSBOrganization organization = new UCSBOrganization("123", "Short name", "Full name", true);
    when(ucsbOrganizationRepository.findById("123")).thenReturn(Optional.of(organization));
    mockMvc
        .perform(get("/api/UCSBOrganization").param("id", "123"))
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(organization)));
    verify(ucsbOrganizationRepository).findById("123");
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void getting_a_missing_organization_returns_404() throws Exception {
    when(ucsbOrganizationRepository.findById("123")).thenReturn(Optional.empty());
    mockMvc
        .perform(get("/api/UCSBOrganization").param("id", "123"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.type").value("EntityNotFoundException"))
        .andExpect(jsonPath("$.message").value("id 123 not found"));
    verify(ucsbOrganizationRepository).findById("123");
    verifyNoMoreInteractions(ucsbOrganizationRepository);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admins_can_delete_an_existing_organization() throws Exception {
    UCSBOrganization organization = new UCSBOrganization("123", "Short name", "Full name", false);
    when(ucsbOrganizationRepository.findById("123")).thenReturn(Optional.of(organization));
    mockMvc
        .perform(delete("/api/UCSBOrganization").param("id", "123").with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("record 123 deleted"));
    verify(ucsbOrganizationRepository).findById("123");
    verify(ucsbOrganizationRepository).delete(organization);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void deleting_a_missing_organization_returns_404() throws Exception {
    when(ucsbOrganizationRepository.findById("123")).thenReturn(Optional.empty());
    mockMvc
        .perform(delete("/api/UCSBOrganization").param("id", "123").with(csrf()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.type").value("EntityNotFoundException"))
        .andExpect(jsonPath("$.message").value("record 123 not found"));
    verify(ucsbOrganizationRepository).findById("123");
    verifyNoMoreInteractions(ucsbOrganizationRepository);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void admins_can_update_non_key_fields_and_the_code_stays_unchanged(boolean inactive)
      throws Exception {
    UCSBOrganization original =
        new UCSBOrganization("SKY", "Old short name", "Old full name", !inactive);
    UCSBOrganization incoming =
        new UCSBOrganization("DIFFERENT", "New short name", "New full name", inactive);
    UCSBOrganization expected =
        new UCSBOrganization("SKY", "New short name", "New full name", inactive);
    when(ucsbOrganizationRepository.findById("SKY")).thenReturn(Optional.of(original));
    mockMvc
        .perform(
            put("/api/UCSBOrganization")
                .param("id", "SKY")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(incoming))
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(content().json(mapper.writeValueAsString(expected)));
    verify(ucsbOrganizationRepository).findById("SKY");
    verify(ucsbOrganizationRepository).save(eq(expected));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void updating_a_missing_organization_returns_404() throws Exception {
    when(ucsbOrganizationRepository.findById("SKY")).thenReturn(Optional.empty());
    mockMvc
        .perform(organizationPut().with(csrf()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.type").value("EntityNotFoundException"))
        .andExpect(jsonPath("$.message").value("id SKY not found"));
    verify(ucsbOrganizationRepository).findById("SKY");
    verifyNoMoreInteractions(ucsbOrganizationRepository);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void single_record_routes_require_id() throws Exception {
    mockMvc.perform(get("/api/UCSBOrganization")).andExpect(status().isBadRequest());
    mockMvc
        .perform(delete("/api/UCSBOrganization").with(csrf()))
        .andExpect(status().isBadRequest());
    mockMvc
        .perform(
            put("/api/UCSBOrganization")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(csrf()))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(ucsbOrganizationRepository);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void update_requires_a_valid_json_body() throws Exception {
    mockMvc
        .perform(
            put("/api/UCSBOrganization")
                .param("id", "SKY")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
        .andExpect(status().isBadRequest());
    mockMvc
        .perform(
            put("/api/UCSBOrganization")
                .param("id", "SKY")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{")
                .with(csrf()))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(ucsbOrganizationRepository);
  }
}
