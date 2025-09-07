package com.rss.core.account.domain.repository;

import com.rss.core.account.domain.entity.Rider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiderRepository extends JpaRepository<Rider, Long> {
}