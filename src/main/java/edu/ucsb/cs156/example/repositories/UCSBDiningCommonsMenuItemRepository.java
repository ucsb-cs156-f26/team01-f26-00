package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

/**
 * The UCSBDiningCommonsMenuItemRepository is a repository for UCSBDiningCommonsMenuItem entities
 */
@Repository
@RepositoryRestResource(exported = false)
public interface UCSBDiningCommonsMenuItemRepository
    extends CrudRepository<UCSBDiningCommonsMenuItem, Long> {}
