package com.tpbs.insuranceservice.repository;

import com.tpbs.insuranceservice.model.Insurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsuranceRepository extends JpaRepository<Insurance, Long> {
    
    List<Insurance> findByUserId(Long userId);
    
    List<Insurance> findByBookingId(Long bookingId);
    
    List<Insurance> findByStatus(String status);
    
    Optional<Insurance> findByPolicyNumber(String policyNumber);
    
    @Query("SELECT i FROM Insurance i WHERE i.userId = :userId AND i.status = :status")
    List<Insurance> findByUserIdAndStatus(Long userId, String status);
    
    @Query("SELECT i FROM Insurance i WHERE i.bookingId = :bookingId AND i.status = 'ACTIVE'")
    Optional<Insurance> findActiveInsuranceByBookingId(Long bookingId);
}