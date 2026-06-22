package com.shopzuu.ecommerce.repository;

import com.shopzuu.ecommerce.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByVendorId(Long vendorId);

    @Query("SELECT i FROM OrderItem i WHERE i.vendor.id = :vendorId " +
            "ORDER BY i.subtotal DESC")
    List<OrderItem> findTopItemsByVendor(@Param("vendorId") Long vendorId);

    @Query("SELECT i.product.id, i.product.name, SUM(i.quantity), SUM(i.subtotal) " +
            "FROM OrderItem i WHERE i.vendor.id = :vendorId " +
            "GROUP BY i.product.id, i.product.name " +
            "ORDER BY SUM(i.quantity) DESC")
    List<Object[]> findTopProductsByVendor(@Param("vendorId") Long vendorId);
}