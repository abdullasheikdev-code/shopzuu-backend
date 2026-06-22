package com.shopzuu.ecommerce.repository;

import com.shopzuu.ecommerce.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentGatewayId(String paymentGatewayId);

    Optional<Payment> findByOrderId(Long orderId);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCESS'")
    Double totalSuccessfulPayments();

    @Query("SELECT SUM(p.amount) FROM Payment p " +
            "WHERE p.status = 'SUCCESS' AND p.gateway = :gateway")
    Double totalByGateway(@Param("gateway") String gateway);
}