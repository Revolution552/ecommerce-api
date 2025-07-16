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

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    public ResponseEntity<Review> addReview(@RequestBody @Valid ReviewDTO reviewRequest) {
        logger.info("Received request to add a new review: {}", reviewRequest);

        Review review = new Review();
        review.setProductId(reviewRequest.getProductId());
        review.setUserId(reviewRequest.getUserId());
        review.setRating(reviewRequest.getRating());
        review.setComment(reviewRequest.getComment());

        Review createdReview = reviewService.addReview(review);
        logger.info("Review created successfully: {}", createdReview);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getReviewsByProduct(@PathVariable Long productId) {
        logger.info("Fetching reviews for productId: {}", productId);

        List<Review> reviews = reviewService.getReviewsByProduct(productId);

        if (reviews.isEmpty()) {
            logger.warn("No reviews found for productId: {}", productId);
        } else {
            logger.debug("Fetched {} reviews for productId: {}", reviews.size(), productId);
        }

        return ResponseEntity.ok(reviews);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        logger.info("Received request to delete review with id: {}", id);

        try {
            reviewService.deleteReview(id);
            logger.info("Review with id: {} deleted successfully", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Failed to delete review with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
