package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for MenuItemReviews */
@Tag(name = "MenuItemReview")
@RequestMapping("/api/MenuItemReview")
@RestController
@Slf4j
public class MenuItemReviewController extends ApiController {

  @Autowired MenuItemReviewRepository menuItemReviewRepository;

  /**
   * List all menu item reviews
   *
   * @return an iterable of MenuItemReview
   */
  @Operation(summary = "List all menu item reviews")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<MenuItemReview> allMenuItemReviews() {
    Iterable<MenuItemReview> reviews = menuItemReviewRepository.findAll();
    return reviews;
  }

  /**
   * Get a single menu item review by id
   *
   * @param id the id of the menu item review
   * @return a MenuItemReview
   */
  @Operation(summary = "Get a single menu item review by its id")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public MenuItemReview getById(
      @Parameter(name = "id", description = "id of the menu item review") @RequestParam Long id) {
    MenuItemReview menuItemReview =
        menuItemReviewRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

    return menuItemReview;
  }

  /**
   * Create a new menu item review
   *
   * @param itemId the id of the menu item being reviewed
   * @param reviewerEmail the email of the reviewer
   * @param stars the rating, from 0 to 5 stars
   * @param dateReviewed the date and time of the review
   * @param comments the text of the review
   * @return the saved menu item review
   */
  @Operation(summary = "Create a new menu item review")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public MenuItemReview postMenuItemReview(
      @Parameter(name = "itemId", description = "id of the menu item being reviewed") @RequestParam
          Long itemId,
      @Parameter(name = "reviewerEmail", description = "email of the reviewer") @RequestParam
          String reviewerEmail,
      @Parameter(name = "stars", description = "rating from 0 to 5 stars") @RequestParam int stars,
      @Parameter(
              name = "dateReviewed",
              description =
                  "date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)")
          @RequestParam("dateReviewed")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateReviewed,
      @Parameter(name = "comments", description = "text of the review") @RequestParam
          String comments)
      throws JsonProcessingException {

    // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // See: https://www.baeldung.com/spring-date-parameters

    log.info("dateReviewed={}", dateReviewed);

    MenuItemReview menuItemReview = new MenuItemReview();
    menuItemReview.setItemId(itemId);
    menuItemReview.setReviewerEmail(reviewerEmail);
    menuItemReview.setStars(stars);
    menuItemReview.setDateReviewed(dateReviewed);
    menuItemReview.setComments(comments);

    MenuItemReview savedMenuItemReview = menuItemReviewRepository.save(menuItemReview);

    return savedMenuItemReview;
  }

  /**
   * Delete a menu item review
   *
   * @param id the id of the menu item review to delete
   * @return a message indicating the menu item review was deleted
   */
  @Operation(summary = "Delete a MenuItemReview")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteMenuItemReview(@Parameter(name = "id") @RequestParam Long id) {
    MenuItemReview menuItemReview =
        menuItemReviewRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

    menuItemReviewRepository.delete(menuItemReview);
    return genericMessage("MenuItemReview with id %s deleted".formatted(id));
  }

  /**
   * Update a single menu item review
   *
   * @param id id of the menu item review to update
   * @param incoming the new menu item review
   * @return the updated menu item review object
   */
  @Operation(summary = "Update a single menu item review")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public MenuItemReview updateMenuItemReview(
      @Parameter(name = "id") @RequestParam Long id, @RequestBody @Valid MenuItemReview incoming) {

    MenuItemReview menuItemReview =
        menuItemReviewRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

    menuItemReview.setItemId(incoming.getItemId());
    menuItemReview.setReviewerEmail(incoming.getReviewerEmail());
    menuItemReview.setStars(incoming.getStars());
    menuItemReview.setDateReviewed(incoming.getDateReviewed());
    menuItemReview.setComments(incoming.getComments());

    menuItemReviewRepository.save(menuItemReview);

    return menuItemReview;
  }
}
