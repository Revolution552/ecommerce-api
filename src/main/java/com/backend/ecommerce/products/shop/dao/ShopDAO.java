package com.backend.ecommerce.products.shop.dao;

import com.backend.ecommerce.products.shop.model.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShopDAO extends ListCrudRepository<Shop, Long> {

    /**
     * Retrieve all shops owned by a specific user.
     * @param userId the ID of the user.
     * @return a list of shops associated with the given user ID.
     */
    List<Shop> findByUserId(Long userId);

    /**
     * Retrieve paginated shops owned by a specific user.
     * @param userId the ID of the user.
     * @param pageable pagination information.
     * @return a page of shops associated with the given user ID.
     */
    Page<Shop> findByUserId(Long userId, Pageable pageable);

    /**
     * Find shops by exact name, ignoring case sensitivity.
     * @param name the name of the shop.
     * @return a list of shops with an exact name match, case-insensitive.
     */
    List<Shop> findByNameIgnoreCase(String name);

    /**
     * Find shops by a partial name match, ignoring case.
     * Useful for search functionality with flexible name matching.
     * @param name the partial name to search for.
     * @return a list of shops whose names contain the specified partial name.
     */
    List<Shop> findByNameContainingIgnoreCase(String name);

    /**
     * Find shops located at a specific location.
     * @param location the location of the shop.
     * @return a list of shops located in the specified location.
     */
    List<Shop> findByLocation(String location);

    /**
     * Find shops by location, ignoring case sensitivity.
     * @param location the location of the shop.
     * @return a list of shops located in the specified location, case-insensitive.
     */
    List<Shop> findByLocationIgnoreCase(String location);

    /**
     * Find shops with a partial location match, ignoring case sensitivity.
     * Useful for location-based search functionality.
     * @param location the partial location to search for.
     * @return a list of shops located in areas containing the specified partial location.
     */
    List<Shop> findByLocationContainingIgnoreCase(String location);

    /**
     * Retrieve all shops with pagination support.
     * @param pageable pagination details.
     * @return a paginated list of all shops.
     */
    Page<Shop> findAll(Pageable pageable);
}
