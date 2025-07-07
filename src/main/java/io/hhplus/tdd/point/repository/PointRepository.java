package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.point.UserPoint;

import java.util.Optional;

public interface PointRepository {
    Optional<UserPoint> findById(Long userId);
    UserPoint save(UserPoint userPoint);
}