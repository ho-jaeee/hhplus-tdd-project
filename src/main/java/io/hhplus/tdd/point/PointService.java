package io.hhplus.tdd.point;

import io.hhplus.tdd.point.repository.PointRepository;
import org.springframework.stereotype.Service;

@Service
public class PointService {

    private final PointRepository pointRepository;

    public PointService(PointRepository pointRepository) {
        this.pointRepository = pointRepository;
    }

    //포인트 충전
    public UserPoint pointCharge(long userId, long amount) {
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

    //포인트 조회
    public long getPoint(long userId) {

        //사용자 정보 조회
        UserPoint userPoint = pointRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        // 순수하게 포인트만 리턴
        return userPoint.point();

    }

    //포인트 사용
    public UserPoint usePoint(long userId, long amount) {
        UserPoint userPoint = pointRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        if (userPoint.point() < amount) {
            throw new IllegalArgumentException("포인트 부족");
        }else {
            UserPoint updated = new UserPoint(
                    userPoint.id(),
                    userPoint.point() - amount,
                    System.currentTimeMillis()
            );

            // 저장 및 반환
            return pointRepository.save(updated);
        }
    }

}
