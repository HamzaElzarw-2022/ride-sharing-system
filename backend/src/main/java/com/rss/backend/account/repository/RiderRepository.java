package com.rss.backend.account.repository;

import com.rss.backend.domain.entity.Rider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiderRepository extends JpaRepository<Rider, Long> {
}