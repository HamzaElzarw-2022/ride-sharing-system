package com.rss.backend.account.repository;

import com.rss.backend.domain.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverRepository extends JpaRepository<Driver, Long> {
}