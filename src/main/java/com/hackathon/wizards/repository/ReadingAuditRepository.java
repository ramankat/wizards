package com.hackathon.wizards.repository;

import com.hackathon.wizards.entity.ReadingAud;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadingAuditRepository  extends JpaRepository<ReadingAud, Long>  {

    @Query(value = "select * from reading_audit ra where ra.device_id = :id order by id desc limit :dataPoints", nativeQuery = true)
    List<ReadingAud> findLastNPoints(@Param("id") Long id, @Param("dataPoints") Integer dataPoints);

    List<ReadingAud> findAllByCreatedAtGreaterThan(LocalDateTime createdAt);
}
