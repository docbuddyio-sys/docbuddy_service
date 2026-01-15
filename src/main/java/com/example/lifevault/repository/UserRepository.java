package com.example.lifevault.repository;

import com.example.lifevault.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByMobile(String mobile);

    Boolean existsByMobile(String mobile);

    Boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}
