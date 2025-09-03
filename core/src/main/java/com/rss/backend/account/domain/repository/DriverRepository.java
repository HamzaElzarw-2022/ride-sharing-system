package com.rss.backend.account.domain.repository;

import com.rss.backend.account.domain.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverRepository extends JpaRepository<Driver, Long> {
}