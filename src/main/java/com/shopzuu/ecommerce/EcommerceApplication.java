package com.shopzuu.ecommerce;

import com.shopzuu.ecommerce.model.*;
import com.shopzuu.ecommerce.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class EcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }

    // Seed default categories and admin user on first run
    @Bean
    public CommandLineRunner seedData(
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // Seed categories if empty
            if (categoryRepository.count() == 0) {
                String[] categoryNames = {
                        "Fashion & Clothing",
                        "Electronics",
                        "Home & Kitchen",
                        "Beauty & Personal Care",
                        "Handmade & Crafts",
                        "Food & Beverages",
                        "Books & Stationery",
                        "Sports & Fitness"
                };

                for (String name : categoryNames) {
                    Category category = Category.builder()
                            .name(name)
                            .description(name + " products")
                            .build();
                    categoryRepository.save(category);
                }
                System.out.println("✅ Categories seeded");
            }

            // Seed admin user if not exists
            if (!userRepository.existsByEmail("admin@zuu.in")) {
                User admin = User.builder()
                        .name("ShopzuuAdmin")
                        .email("admin@shopzuu.in")
                        .password(passwordEncoder.encode("Admin@123"))
                        .phone("9999999999")
                        .role(User.Role.ADMIN)
                        .isActive(true)
                        .build();
                userRepository.save(admin);
                System.out.println("✅ Admin user created");
                System.out.println("📧 Email: admin@shopzuu.in");
                System.out.println("🔑 Password: Admin@123");
            }
        };
    }

}