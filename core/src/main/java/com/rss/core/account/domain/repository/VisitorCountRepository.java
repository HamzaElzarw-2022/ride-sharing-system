package com.rss.core.account.domain.repository;

import com.rss.core.account.domain.entity.VisitorCount;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface VisitorCountRepository extends JpaRepository<VisitorCount, Long> {

    Optional<VisitorCount> findByVisitDate(LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from VisitorCount v where v.visitDate = :date")
    Optional<VisitorCount> lockByVisitDate(@Param("date") LocalDate date);
}

