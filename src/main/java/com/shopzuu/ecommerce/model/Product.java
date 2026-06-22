package com.shopzuu.ecommerce.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {


    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    private ProductType productType = ProductType.VENDOR;

    @Column(name = "affiliate_url", length = 1000)
    private String affiliateUrl;

    @Column(name = "affiliate_source")
    private String affiliateSource; // "Amazon", "Flipkart", "Meesho"

    public enum ProductType {
        VENDOR, AFFILIATE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(name = "discount_price")
    private Double discountPrice;

    @Column(nullable = false)
    private Integer stock;

    @ElementCollection
    @CollectionTable(
            name = "product_images",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @Column(name = "image_url")
    private List<String> images;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "vendor_id", nullable = true)
    private Vendor vendor;

    @Column(name = "is_featured")
    private boolean isFeatured = false;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "total_sold")
    private Integer totalSold = 0;

    @Column(name = "rating")
    private Double rating = 0.0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}