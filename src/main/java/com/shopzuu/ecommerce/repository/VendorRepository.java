package com.shopzuu.ecommerce.repository;

import com.shopzuu.ecommerce.model.Vendor;
import com.shopzuu.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {

    Optional<Vendor> findByUser(User user);

    Optional<Vendor> findByUserId(Long userId);

    List<Vendor> findByStatus(Vendor.VendorStatus status);

    List<Vendor> findByPlan(Vendor.SubscriptionPlan plan);

    @Query("SELECT COUNT(v) FROM Vendor v WHERE v.status = 'APPROVED'")
    Integer countActiveVendors();

    @Query("SELECT COUNT(v) FROM Vendor v WHERE v.status = 'PENDING'")
    Integer countPendingVendors();

    @Query("SELECT SUM(v.platformCommissionPaid) FROM Vendor v")
    Double totalCommissionCollected();

    @Query("SELECT v FROM Vendor v WHERE v.status = 'APPROVED' " +
            "ORDER BY v.totalEarnings DESC")
    List<Vendor> findTopVendors();
}