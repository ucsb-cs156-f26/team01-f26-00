package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
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

/** This is a REST controller for UCSBDiningCommonsMenuItem */
@Tag(name = "UCSBDiningCommonsMenuItem")
@RequestMapping("/api/UCSBDiningCommonsMenuItem")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemController extends ApiController {

  @Autowired UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

  /**
   * List all UCSB dining commons menu items
   *
   * @return an iterable of UCSBDiningCommonsMenuItem
   */
  @Operation(summary = "List all ucsb dining commons menu items")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBDiningCommonsMenuItem> allUCSBDiningCommonsMenuItems() {
    Iterable<UCSBDiningCommonsMenuItem> menuItems = ucsbDiningCommonsMenuItemRepository.findAll();
    return menuItems;
  }

  /**
   * Get a single menu item by id
   *
   * @param id the id of the menu item
   * @return a UCSBDiningCommonsMenuItem
   */
  @Operation(summary = "Get a single menu item")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public UCSBDiningCommonsMenuItem getById(
      @Parameter(name = "id", description = "id of the menu item to look up, e.g. 1") @RequestParam
          Long id) {
    UCSBDiningCommonsMenuItem menuItem =
        ucsbDiningCommonsMenuItemRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    return menuItem;
  }

  /**
   * Create a new menu item
   *
   * @param diningCommonsCode the code of the dining commons where the item is served
   * @param name the name of the menu item
   * @param station the station in the dining commons where the item is served
   * @return the saved ucsbdiningcommonsmenuitem
   */
  @Operation(summary = "Create a new menu item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBDiningCommonsMenuItem postUCSBDiningCommonsMenuItem(
      @Parameter(
              name = "diningCommonsCode",
              description =
                  "code of the dining commons, e.g. ortega, carrillo, portola, de-la-guerra")
          @RequestParam
          String diningCommonsCode,
      @Parameter(name = "name", description = "name of the menu item, e.g. Baked Pesto Pasta")
          @RequestParam
          String name,
      @Parameter(
              name = "station",
              description = "station where the item is served, e.g. Entree Specials")
          @RequestParam
          String station) {

    UCSBDiningCommonsMenuItem menuItem = new UCSBDiningCommonsMenuItem();
    menuItem.setDiningCommonsCode(diningCommonsCode);
    menuItem.setName(name);
    menuItem.setStation(station);

    UCSBDiningCommonsMenuItem savedMenuItem = ucsbDiningCommonsMenuItemRepository.save(menuItem);

    return savedMenuItem;
  }

  /**
   * Delete a UCSBDiningCommonsMenuItem
   *
   * @param id the id of the menu item to delete
   * @return a message indicating the menu item was deleted
   */
  @Operation(summary = "Delete a menu item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteUCSBDiningCommonsMenuItem(
      @Parameter(name = "id", description = "id of the menu item to delete, e.g. 1") @RequestParam
          Long id) {
    UCSBDiningCommonsMenuItem menuItem =
        ucsbDiningCommonsMenuItemRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    ucsbDiningCommonsMenuItemRepository.delete(menuItem);
    return genericMessage("UCSBDiningCommonsMenuItem with id %s deleted".formatted(id));
  }

  /**
   * Update a single menu item
   *
   * @param id id of the menu item to update
   * @param incoming the new values for the menu item (the id in the request body is ignored)
   * @return the updated menu item object
   */
  @Operation(
      summary = "Update a single menu item",
      description =
          "Replaces diningCommonsCode, name and station with the values in the request body; any id"
              + " in the request body is ignored")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public UCSBDiningCommonsMenuItem updateUCSBDiningCommonsMenuItem(
      @Parameter(name = "id", description = "id of the menu item to update, e.g. 1") @RequestParam
          Long id,
      @RequestBody @Valid UCSBDiningCommonsMenuItem incoming) {

    UCSBDiningCommonsMenuItem menuItem =
        ucsbDiningCommonsMenuItemRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    menuItem.setDiningCommonsCode(incoming.getDiningCommonsCode());
    menuItem.setName(incoming.getName());
    menuItem.setStation(incoming.getStation());

    ucsbDiningCommonsMenuItemRepository.save(menuItem);

    return menuItem;
  }
}
