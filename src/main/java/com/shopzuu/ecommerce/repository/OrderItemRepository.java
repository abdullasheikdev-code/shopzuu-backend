package com.shopzuu.ecommerce.repository;

import com.shopzuu.ecommerce.model.Order;
import com.shopzuu.ecommerce.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Get all order items of a vendor
    List<OrderItem> findByVendorId(Long vendorId);

    // Top order items
    @Query("""
            SELECT i
            FROM OrderItem i
            WHERE i.vendor.id = :vendorId
            ORDER BY i.subtotal DESC
            """)
    List<OrderItem> findTopItemsByVendor(@Param("vendorId") Long vendorId);

    // Top selling products
    @Query("""
            SELECT i.product.id,
                   i.product.name,
                   SUM(i.quantity),
                   SUM(i.subtotal)
            FROM OrderItem i
            JOIN i.order o
            WHERE i.vendor.id = :vendorId
              AND o.paymentStatus = com.shopzuu.ecommerce.model.Order.PaymentStatus.PAID
              AND o.status IN (
                    com.shopzuu.ecommerce.model.Order.OrderStatus.CONFIRMED,
                    com.shopzuu.ecommerce.model.Order.OrderStatus.SHIPPED,
                    com.shopzuu.ecommerce.model.Order.OrderStatus.DELIVERED
              )
            GROUP BY i.product.id, i.product.name
            ORDER BY SUM(i.quantity) DESC
            """)
    List<Object[]> findTopProductsByVendor(@Param("vendorId") Long vendorId);

    // Total commission earned by platform from this vendor
    @Query("""
            SELECT COALESCE(SUM(i.commissionAmount),0)
            FROM OrderItem i
            JOIN i.order o
            WHERE i.vendor.id = :vendorId
              AND o.paymentStatus = com.shopzuu.ecommerce.model.Order.PaymentStatus.PAID
              AND o.status IN (
                    com.shopzuu.ecommerce.model.Order.OrderStatus.CONFIRMED,
                    com.shopzuu.ecommerce.model.Order.OrderStatus.SHIPPED,
                    com.shopzuu.ecommerce.model.Order.OrderStatus.DELIVERED
              )
            """)
    Double getTotalCommissionByVendor(@Param("vendorId") Long vendorId);
}