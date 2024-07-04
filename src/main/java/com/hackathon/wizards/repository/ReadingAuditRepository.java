package com.hackathon.wizards.repository;

import com.hackathon.wizards.entity.ReadingAud;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadingAuditRepository  extends JpaRepository<ReadingAud, Long>  {

    @Query(value = "select TOP :dataPoints * from reading_audit ra where ra.device_id = :id order by created_at desc", nativeQuery = true)
    List<ReadingAud> findLastNPoints(@Param("id") Long id, @Param("dataPoints") Integer dataPoints);
}
