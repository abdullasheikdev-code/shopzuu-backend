package com.shopzuu.ecommerce.repository;

import com.shopzuu.ecommerce.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByBuyerId(Long buyerId);

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByStatus(Order.OrderStatus status);

    // =========================
    // Vendor Orders
    // =========================

    @Query("""
            SELECT DISTINCT o
            FROM Order o
            JOIN o.items i
            WHERE i.vendor.id = :vendorId
            ORDER BY o.createdAt DESC
            """)
    List<Order> findOrdersByVendorId(@Param("vendorId") Long vendorId);

    @Query("""
            SELECT DISTINCT o
            FROM Order o
            JOIN o.items i
            WHERE i.vendor.id = :vendorId
            AND o.status = :status
            ORDER BY o.createdAt DESC
            """)
    List<Order> findOrdersByVendorIdAndStatus(
            @Param("vendorId") Long vendorId,
            @Param("status") Order.OrderStatus status
    );

    @Query("""
            SELECT COUNT(DISTINCT o.id)
            FROM Order o
            JOIN o.items i
            WHERE i.vendor.id = :vendorId
            AND o.paymentStatus = 'PAID'
            AND o.status IN (
                com.shopzuu.ecommerce.model.Order.OrderStatus.CONFIRMED,
                com.shopzuu.ecommerce.model.Order.OrderStatus.SHIPPED,
                com.shopzuu.ecommerce.model.Order.OrderStatus.DELIVERED
            )
            """)
    Integer countPaidOrdersByVendor(@Param("vendorId") Long vendorId);

    @Query("""
            SELECT COUNT(DISTINCT o.id)
            FROM Order o
            JOIN o.items i
            WHERE i.vendor.id = :vendorId
            AND o.status = 'PENDING'
            """)
    Integer countPendingOrdersByVendor(@Param("vendorId") Long vendorId);

    @Query("""
            SELECT DISTINCT o
            FROM Order o
            JOIN o.items i
            WHERE i.vendor.id = :vendorId
            ORDER BY o.createdAt DESC
            """)
    List<Order> findRecentOrders(@Param("vendorId") Long vendorId);

    // =========================
    // Admin Dashboard
    // =========================

    @Query("""
            SELECT COUNT(o)
            FROM Order o
            WHERE o.paymentStatus = 'PAID'
            AND o.status IN (
                com.shopzuu.ecommerce.model.Order.OrderStatus.CONFIRMED,
                com.shopzuu.ecommerce.model.Order.OrderStatus.SHIPPED,
                com.shopzuu.ecommerce.model.Order.OrderStatus.DELIVERED
            )
            """)
    Integer countPaidOrders();

    @Query("""
            SELECT COALESCE(SUM(o.totalAmount),0)
            FROM Order o
            WHERE o.paymentStatus = 'PAID'
            AND o.status IN (
                com.shopzuu.ecommerce.model.Order.OrderStatus.CONFIRMED,
                com.shopzuu.ecommerce.model.Order.OrderStatus.SHIPPED,
                com.shopzuu.ecommerce.model.Order.OrderStatus.DELIVERED
            )
            """)
    Double totalGMV();

    @Query("""
            SELECT COALESCE(SUM(o.platformCommission),0)
            FROM Order o
            WHERE o.paymentStatus = 'PAID'
            AND o.status IN (
                com.shopzuu.ecommerce.model.Order.OrderStatus.CONFIRMED,
                com.shopzuu.ecommerce.model.Order.OrderStatus.SHIPPED,
                com.shopzuu.ecommerce.model.Order.OrderStatus.DELIVERED
            )
            """)
    Double totalPlatformCommission();

    @Query("""
            SELECT COALESCE(SUM(o.platformCommission),0)
            FROM Order o
            WHERE o.paymentStatus = 'PAID'
            AND o.status IN (
                com.shopzuu.ecommerce.model.Order.OrderStatus.CONFIRMED,
                com.shopzuu.ecommerce.model.Order.OrderStatus.SHIPPED,
                com.shopzuu.ecommerce.model.Order.OrderStatus.DELIVERED
            )
            AND o.createdAt BETWEEN :start AND :end
            """)
    Double platformCommissionBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // =========================
    // Vendor Earnings
    // =========================

    @Query("""
            SELECT COALESCE(SUM(i.vendorEarning),0)
            FROM OrderItem i
            WHERE i.vendor.id = :vendorId
            AND i.order.paymentStatus = 'PAID'
            AND i.order.status IN (
                com.shopzuu.ecommerce.model.Order.OrderStatus.CONFIRMED,
                com.shopzuu.ecommerce.model.Order.OrderStatus.SHIPPED,
                com.shopzuu.ecommerce.model.Order.OrderStatus.DELIVERED
            )
            """)
    Double totalEarningsByVendor(@Param("vendorId") Long vendorId);

    @Query("""
            SELECT COALESCE(SUM(i.vendorEarning),0)
            FROM OrderItem i
            WHERE i.vendor.id = :vendorId
            AND i.order.paymentStatus = 'PAID'
            AND i.order.status IN (
                com.shopzuu.ecommerce.model.Order.OrderStatus.CONFIRMED,
                com.shopzuu.ecommerce.model.Order.OrderStatus.SHIPPED,
                com.shopzuu.ecommerce.model.Order.OrderStatus.DELIVERED
            )
            AND i.order.createdAt BETWEEN :start AND :end
            """)
    Double earningsByVendorBetween(
            @Param("vendorId") Long vendorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}