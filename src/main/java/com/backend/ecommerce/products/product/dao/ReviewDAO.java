package com.backend.ecommerce.products.product.dao;

import com.backend.ecommerce.products.product.model.Review;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewDAO extends ListCrudRepository<Review, Long> {
    List<Review> findByProductId(Long productId);
}
