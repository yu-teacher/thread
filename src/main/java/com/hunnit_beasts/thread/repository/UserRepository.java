package com.hunnit_beasts.thread.repository;

import com.hunnit_beasts.thread.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "SELECT u.*, SLEEP(:delayMs / 1000) FROM User u WHERE u.id = :id", nativeQuery = true)
    User findByIdWithDelay(@Param("id") Long id, @Param("delayMs") int delayMs);
}