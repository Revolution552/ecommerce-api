package com.backend.ecommerce.products.product.service;

import com.backend.ecommerce.products.product.dao.ReviewDAO;
import com.backend.ecommerce.products.product.model.Review;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    @Autowired
    private ReviewDAO reviewDAO;

    public Review addReview(Review review) {
        logger.info("Adding a new review for productId: {}", review.getProductId());
        Review savedReview = reviewDAO.save(review);
        logger.debug("Review added successfully: {}", savedReview);
        return savedReview;
    }

    public List<Review> getReviewsByProduct(Long productId) {
        logger.info("Fetching reviews for productId: {}", productId);
        List<Review> reviews = reviewDAO.findByProductId(productId);
        if (reviews.isEmpty()) {
            logger.warn("No reviews found for productId: {}", productId);
        } else {
            logger.debug("Fetched {} reviews for productId: {}", reviews.size(), productId);
        }
        return reviews;
    }

    public void deleteReview(Long id) {
        logger.info("Attempting to delete review with id: {}", id);
        if (reviewDAO.existsById(id)) {
            reviewDAO.deleteById(id);
            logger.info("Review with id: {} deleted successfully", id);
        } else {
            logger.warn("Review with id: {} not found", id);
        }
    }
}
