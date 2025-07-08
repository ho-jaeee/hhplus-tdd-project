package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.point.UserPoint;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointRepository {
    Optional<UserPoint> findById(Long userId);
    UserPoint save(UserPoint userPoint);
}