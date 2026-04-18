// src/main/java/com/backend/ecommerce/product/specification/ProductSpecification.java
package com.backend.ecommerce.product.specification;

import com.backend.ecommerce.product.model.Product;
import com.backend.ecommerce.product.model.ProductStatus;
import com.backend.ecommerce.product.payload.ProductFilterDto;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> filterBy(ProductFilterDto filter) {
        return (Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Only show published products by default for public queries
            predicates.add(cb.equal(root.get("status"), ProductStatus.PUBLISHED));

            if (filter.getKeyword() != null && !filter.getKeyword().isEmpty()) {
                String keyword = "%" + filter.getKeyword().toLowerCase() + "%";
                Predicate namePredicate = cb.like(cb.lower(root.get("name")), keyword);
                Predicate descPredicate = cb.like(cb.lower(root.get("description")), keyword);
                Predicate brandPredicate = cb.like(cb.lower(root.get("brand")), keyword);
                Predicate tagsPredicate = cb.like(cb.lower(root.get("tags")), keyword);
                predicates.add(cb.or(namePredicate, descPredicate, brandPredicate, tagsPredicate));
            }

            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
            }

            if (filter.getCategoryIds() != null && !filter.getCategoryIds().isEmpty()) {
                predicates.add(root.get("category").get("id").in(filter.getCategoryIds()));
            }

            if (filter.getShopId() != null) {
                predicates.add(cb.equal(root.get("shop").get("id"), filter.getShopId()));
            }

            if (filter.getBrand() != null && !filter.getBrand().isEmpty()) {
                predicates.add(cb.equal(root.get("brand"), filter.getBrand()));
            }

            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }

            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }

            if (filter.getInStock() != null && filter.getInStock()) {
                predicates.add(cb.greaterThan(root.get("quantity"), 0));
            }

            if (filter.getOnSale() != null && filter.getOnSale()) {
                predicates.add(cb.greaterThan(root.get("compareAtPrice"), root.get("price")));
            }

            if (filter.getFeatured() != null && filter.getFeatured()) {
                predicates.add(cb.isTrue(root.get("isFeatured")));
            }

            if (filter.getTags() != null && !filter.getTags().isEmpty()) {
                List<Predicate> tagPredicates = new ArrayList<>();
                for (String tag : filter.getTags()) {
                    tagPredicates.add(cb.like(cb.lower(root.get("tags")), "%" + tag.toLowerCase() + "%"));
                }
                predicates.add(cb.or(tagPredicates.toArray(new Predicate[0])));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Product> forAdmin(ProductFilterDto filter) {
        return (Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getKeyword() != null && !filter.getKeyword().isEmpty()) {
                String keyword = "%" + filter.getKeyword().toLowerCase() + "%";
                Predicate namePredicate = cb.like(cb.lower(root.get("name")), keyword);
                Predicate skuPredicate = cb.like(cb.lower(root.get("sku")), keyword);
                predicates.add(cb.or(namePredicate, skuPredicate));
            }

            if (filter.getShopId() != null) {
                predicates.add(cb.equal(root.get("shop").get("id"), filter.getShopId()));
            }

            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
            }

            if (filter.getBrand() != null && !filter.getBrand().isEmpty()) {
                predicates.add(cb.equal(root.get("brand"), filter.getBrand()));
            }

            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}