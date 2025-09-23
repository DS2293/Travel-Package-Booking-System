package com.tpbs.userservice.repository;

import com.tpbs.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    List<User> findByRole(String role);
    
    List<User> findByApproval(String approval);
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.approval = :approval")
    List<User> findByRoleAndApproval(@Param("role") String role, @Param("approval") String approval);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") String role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.approval = 'pending'")
    long countPendingApprovals();
} 