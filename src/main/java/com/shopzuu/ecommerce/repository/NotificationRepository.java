package com.shopzuu.ecommerce.repository;

import com.shopzuu.ecommerce.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    Integer countByUserIdAndIsReadFalse(Long userId);
}