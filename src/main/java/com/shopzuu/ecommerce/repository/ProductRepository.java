package com.shopzuu.ecommerce.repository;

import com.shopzuu.ecommerce.model.Product;
import com.shopzuu.ecommerce.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByVendor(Vendor vendor);

    List<Product> findByVendorId(Long vendorId);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByIsActiveTrue();

    List<Product> findByIsFeaturedTrue();

    List<Product> findByIsActiveTrueAndIsFeaturedTrue();

    @Query("SELECT p FROM Product p WHERE p.isActive = true " +
            "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Product> searchProducts(@Param("keyword") String keyword);

    @Query("SELECT p FROM Product p WHERE p.isActive = true " +
            "AND p.category.id = :categoryId")
    List<Product> findActiveByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT p FROM Product p WHERE p.isActive = true " +
            "AND p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceRange(
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice
    );

    @Query("SELECT p FROM Product p WHERE p.vendor.id = :vendorId " +
            "ORDER BY p.totalSold DESC")
    List<Product> findTopSellingByVendor(@Param("vendorId") Long vendorId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.vendor.id = :vendorId")
    Integer countByVendorId(@Param("vendorId") Long vendorId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.isActive = true")
    Integer countActiveProducts();
}