package com.backend.ecommerce.products.product.controller;

import com.backend.ecommerce.products.product.model.Review;
import com.backend.ecommerce.products.product.payload.ReviewDTO;
import com.backend.ecommerce.products.product.service.ReviewService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> addReview(@RequestBody @Valid ReviewDTO reviewRequest) {
        Map<String, Object> response = new HashMap<>();
        logger.info("Received request to add a new review for productId: {} by userId: {}",
                reviewRequest.getProductId(), reviewRequest.getUserId());
        try {
            Review review = new Review();
            review.setProductId(reviewRequest.getProductId());
            review.setUserId(reviewRequest.getUserId());
            review.setRating(reviewRequest.getRating());
            review.setComment(reviewRequest.getComment());

            Review createdReview = reviewService.addReview(review);
            logger.info("Review created successfully with ID: {}", createdReview.getId());

            response.put("success", true);
            response.put("message", "Review added successfully!");
            response.put("review", createdReview);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            logger.error("Failed to add review for productId: {} by userId: {}: {}",
                    reviewRequest.getProductId(), reviewRequest.getUserId(), e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to add review: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Map<String, Object>> getReviewsByProduct(@PathVariable Long productId) {
        Map<String, Object> response = new HashMap<>();
        logger.info("Fetching reviews for productId: {}", productId);

        List<Review> reviews = reviewService.getReviewsByProduct(productId);

        if (reviews.isEmpty()) {
            logger.warn("No reviews found for productId: {}", productId);
            response.put("success", true); // Still true, just no results
            response.put("message", "No reviews found for product ID: " + productId + ".");
            response.put("reviews", reviews); // Return empty list
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            logger.info("Fetched {} reviews for productId: {}", reviews.size(), productId);
            response.put("success", true);
            response.put("message", "Reviews retrieved successfully for product ID: " + productId + ".");
            response.put("reviews", reviews);
            return ResponseEntity.ok(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteReview(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        logger.warn("Received request to delete review with id: {}", id); // Warn level for delete operations

        try {
            reviewService.deleteReview(id);
            logger.info("Review with id: {} deleted successfully", id);
            response.put("success", true);
            response.put("message", "Review with ID: " + id + " deleted successfully.");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response); // 204 No Content is standard for successful delete with no body
        } catch (RuntimeException e) { // Catch RuntimeException for more general errors
            logger.error("Failed to delete review with id: {}: {}", id, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to delete review: " + e.getMessage());
            // Use NOT_FOUND if the service throws a specific exception for not found, otherwise INTERNAL_SERVER_ERROR
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
