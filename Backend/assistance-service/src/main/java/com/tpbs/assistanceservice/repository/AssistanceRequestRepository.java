package com.tpbs.assistanceservice.repository;

import com.tpbs.assistanceservice.model.AssistanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssistanceRequestRepository extends JpaRepository<AssistanceRequest, Long> {
    @Query("select a from AssistanceRequest a where a.userID = :userId")
    List<AssistanceRequest> findAllByUserId(@Param("userId") Long userId);
}
