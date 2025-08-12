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
     * Find shops by exact name, ignoring case sensitivity.
     * @param name the name of the shop.
     * @return a list of shops with an exact name match, case-insensitive.
     */
    List<Shop> findByNameIgnoreCase(String name);

    /**
     * Find shops by location, ignoring case sensitivity.
     * @param location the location of the shop.
     * @return a list of shops located in the specified location, case-insensitive.
     */
    List<Shop> findByLocationIgnoreCase(String location);


    /**
     * Retrieve all shops with pagination support.
     * @param pageable pagination details.
     * @return a paginated list of all shops.
     */
    Page<Shop> findAll(Pageable pageable);
}
