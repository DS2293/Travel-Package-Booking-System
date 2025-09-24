package com.tpbs.reviewservice.repository;

import com.tpbs.reviewservice.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("select r from Review r where r.userID = :userId")
    List<Review> findAllByUserId(@Param("userId") Long userId);

    @Query("select r from Review r where r.packageID = :packageId")
    List<Review> findAllByPackageId(@Param("packageId") Long packageId);
}