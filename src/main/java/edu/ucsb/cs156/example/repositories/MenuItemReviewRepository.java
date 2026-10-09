package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.MenuItemReview;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

/** The MenuItemReviewRepository is a repository for MenuItemReview entities. */
@Repository
@RepositoryRestResource(exported = false)
public interface MenuItemReviewRepository extends CrudRepository<MenuItemReview, Long> {}
