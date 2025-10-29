package com.rss.core.account.application.service;

import com.rss.core.account.domain.entity.VisitorCount;
import com.rss.core.account.domain.repository.VisitorCountRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitorService {

    private final VisitorCountRepository repository;

    @Transactional
    public void incrementToday() {
        LocalDate today = LocalDate.now();
        // First, try to lock and increment if exists
        var locked = repository.lockByVisitDate(today);
        if (locked.isPresent()) {
            VisitorCount vc = locked.get();
            vc.setCount(vc.getCount() + 1);
            repository.save(vc);
            return;
        }
        // Otherwise try to create with count = 1; if a concurrent insert happens, retry by locking
        try {
            VisitorCount created = VisitorCount.builder()
                    .visitDate(today)
                    .count(1)
                    .build();
            repository.saveAndFlush(created);
        } catch (DataIntegrityViolationException e) {
            // Another transaction inserted the row concurrently; lock and increment
            VisitorCount vc = repository.lockByVisitDate(today)
                    .orElseThrow(() -> new IllegalStateException("VisitorCount row should exist after conflict"));
            vc.setCount(vc.getCount() + 1);
            repository.save(vc);
        }

        var yesterdayVisit = repository.findByVisitDate(today.minusDays(1)).orElse(null);
        if (yesterdayVisit != null) {
            log.info("VISITS on {}: {}", yesterdayVisit.getVisitDate(), yesterdayVisit.getCount());
        } else {
            log.info("VISITS on {}: {}", today.minusDays(1), 0);
        }
    }
}
