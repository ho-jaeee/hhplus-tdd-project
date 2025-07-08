package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.point.PointHistory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointHistoryRepository {

    PointHistory save(PointHistory history);
    List<PointHistory> findByUserId(long userId);
}
