package com.edusage.repository;

import com.edusage.model.User;
import com.edusage.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByRole(Role role);
    long countByActiveTrue();
    long countByActiveFalse();
    List<User> findAllByOrderByCreatedAtDesc();
}
