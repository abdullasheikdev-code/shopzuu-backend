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

    @Query("SELECT o FROM Order o JOIN o.items i WHERE i.vendor.id = :vendorId")
    List<Order> findOrdersByVendorId(@Param("vendorId") Long vendorId);

    @Query("SELECT o FROM Order o JOIN o.items i WHERE i.vendor.id = :vendorId " +
            "AND o.status = :status")
    List<Order> findOrdersByVendorIdAndStatus(
            @Param("vendorId") Long vendorId,
            @Param("status") Order.OrderStatus status
    );

    @Query("SELECT COUNT(o) FROM Order o WHERE o.paymentStatus = 'PAID'")
    Integer countPaidOrders();

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.paymentStatus = 'PAID'")
    Double totalGMV();

    @Query("SELECT SUM(o.platformCommission) FROM Order o " +
            "WHERE o.paymentStatus = 'PAID'")
    Double totalPlatformCommission();

    @Query("SELECT SUM(o.platformCommission) FROM Order o " +
            "WHERE o.paymentStatus = 'PAID' " +
            "AND o.createdAt BETWEEN :start AND :end")
    Double platformCommissionBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT SUM(i.vendorEarning) FROM OrderItem i " +
            "WHERE i.vendor.id = :vendorId")
    Double totalEarningsByVendor(@Param("vendorId") Long vendorId);

    @Query("SELECT SUM(i.vendorEarning) FROM OrderItem i " +
            "WHERE i.vendor.id = :vendorId " +
            "AND i.order.createdAt BETWEEN :start AND :end")
    Double earningsByVendorBetween(
            @Param("vendorId") Long vendorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}