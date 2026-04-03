package com.shopwave.repository;

import com.shopwave.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * CategoryRepository — data access layer for Category entities.
 *
 * By extending JpaRepository<Category, Long>, Spring Data JPA automatically
 * implements all the standard CRUD operations for us:
 *   - save(), findById(), findAll(), deleteById(), count(), etc.
 *
 * We don't need to write a single line of SQL for basic operations!
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // No custom queries needed for Category in this assignment.
    // JpaRepository gives us everything we need out of the box.
}
