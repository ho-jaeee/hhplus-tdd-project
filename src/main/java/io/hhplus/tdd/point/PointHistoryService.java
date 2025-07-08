package io.hhplus.tdd.point;

import io.hhplus.tdd.point.repository.PointHistoryRepository;
import org.springframework.stereotype.Service;

@Service
public class PointHistoryService {

    private final PointHistoryRepository pointHistoryRepository;

    public PointHistoryService(PointHistoryRepository pointHistoryRepository) {
        this.pointHistoryRepository = pointHistoryRepository;
    }

    //포인트 충전 이력저장
    public void recordCharge(long userId, long amount) {


        PointHistory history = new PointHistory(
                0L,
                userId,
                amount,
                TransactionType.CHARGE,
                System.currentTimeMillis()
        );
        pointHistoryRepository.save(history);
    }

    //포인트 사용 이력저장
    public void recordUsage(long userId, long amount) {


        PointHistory history = new PointHistory(
                0L,
                userId,
                -amount,
                TransactionType.USE,
                System.currentTimeMillis()
        );
        pointHistoryRepository.save(history);
    }
}