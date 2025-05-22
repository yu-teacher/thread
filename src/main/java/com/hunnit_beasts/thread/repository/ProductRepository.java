package com.hunnit_beasts.thread.repository;

import com.hunnit_beasts.thread.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = "SELECT p.*, SLEEP(:delayMs / 1000) FROM Product p WHERE p.id = :id", nativeQuery = true)
    Product findByIdWithDelay(@Param("id") Long id, @Param("delayMs") int delayMs);
}