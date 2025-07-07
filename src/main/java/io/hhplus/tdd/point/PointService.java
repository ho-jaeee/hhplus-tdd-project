package io.hhplus.tdd.point;

import io.hhplus.tdd.point.repository.PointRepository;
import org.springframework.stereotype.Service;

@Service
public class PointService {

    private final PointRepository pointRepository;

    public PointService(PointRepository pointRepository) {
        this.pointRepository = pointRepository;
    }


    public UserPoint  pointCharge(long userId, long amount) {
        // 기존 포인트 조회 or 신규 생성
        UserPoint current = pointRepository.findById(userId)
                .orElseGet(() -> UserPoint.empty(userId));

        // 새로운 포인트 객체 생성
        UserPoint updated = new UserPoint(
                current.id(),
                current.point() + amount,
                System.currentTimeMillis()
        );

        // 저장 및 반환
        return pointRepository.save(updated);
    }
}
