package edu.ucsb.cs156.example.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This is a JPA entity that represents a MenuItemReview, i.e. a review of an item on the menu at a
 * UCSB dining commons.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "menuitemreviews")
public class MenuItemReview {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private Long itemId;
  private String reviewerEmail;
  private int stars;
  private LocalDateTime dateReviewed;
  private String comments;
}
