package com.hackathon.wizards.repository;

import com.hackathon.wizards.entity.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppConfigRepository extends JpaRepository<AppConfig, Long> {

    AppConfig findAllByKey(String key);

}
