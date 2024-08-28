package com.madison.mymajorapp.services;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.time.LocalDate;

@Service
public class VisitorCounter {
    private AtomicLong count = new AtomicLong(0);
    private ConcurrentHashMap<String, LocalDate> visitors = new ConcurrentHashMap<>();

    public long incrementAndGet(String visitorId) {
        LocalDate today = LocalDate.now();
        visitors.compute(visitorId, (key, lastVisit) -> {
            if (lastVisit == null || !lastVisit.equals(today)) {
                count.incrementAndGet();
                return today;
            }
            return lastVisit;
        });
        return count.get();
    }

    public long getCount() {
        return count.get();
    }
}
