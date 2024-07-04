package com.hackathon.wizards.repository;

import com.hackathon.wizards.entity.ReadingAud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadingAuditRepository  extends JpaRepository<ReadingAud, Long>  {

}
