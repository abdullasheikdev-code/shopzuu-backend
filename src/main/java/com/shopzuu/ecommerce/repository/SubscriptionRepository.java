package com.shopzuu.ecommerce.repository;

import com.shopzuu.ecommerce.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByVendorIdAndStatus(
            Long vendorId,
            Subscription.SubscriptionStatus status
    );

    List<Subscription> findByVendorId(Long vendorId);

    @Query("SELECT SUM(s.amountPaid) FROM Subscription s " +
            "WHERE s.status = 'ACTIVE'")
    Double totalSubscriptionRevenue();

    @Query("SELECT SUM(s.amountPaid) FROM Subscription s " +
            "WHERE s.vendor.id = :vendorId")
    Double totalPaidByVendor(@Param("vendorId") Long vendorId);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.plan = :plan " +
            "AND s.status = 'ACTIVE'")
    Integer countByPlan(@Param("plan") String plan);
}