package io.hhplus.tdd.point;

import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.PointRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointService {


    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;


    public PointService(PointRepository pointRepository, PointHistoryRepository pointHistoryRepository)   {

        this.pointRepository = pointRepository;
        this.pointHistoryRepository = pointHistoryRepository;


    }


    //포인트 충전
    public UserPoint pointCharge(long userId, long amount) {

        // 사용자 기존 포인트 조회 or 신규 생성
        UserPoint current = pointRepository.findById(userId)
                .orElseGet(() -> UserPoint.empty(userId));

        if(amount <= 0) {
           throw new IllegalArgumentException("충전 금액은 0원보다 커야 합니다.");//이 조건은 컨트롤러에서 하고싶음

       } else if (amount%1000 != 0) {
            throw new IllegalArgumentException("충전금액은 1,000원 단위로 충전해주시기 바랍니다.");

        } else {

           // 새로운 포인트 객체 생성
           UserPoint updated = new UserPoint(
                   current.id(),
                   current.point() + amount,
                   System.currentTimeMillis()
           );

           //이력 저장-별도 클래스로 빼려고 했으나 의존성이 심해짐(테스트 코드 작성 시 번거로움)
           PointHistory history = new PointHistory(
                   0L,
                   userId,
                   amount,
                   TransactionType.CHARGE,
                   System.currentTimeMillis()
           );
           pointHistoryRepository.save(history);

           // 충전 저장 및 반환
           return pointRepository.save(updated);

       }
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
    public UserPoint pointUse(long userId, long amount) {
        UserPoint userPoint = pointRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        if (userPoint.point() < amount) {
            throw new IllegalArgumentException("포인트 부족");

        } else if (amount<=0) {
            throw new IllegalArgumentException("사용 금액은 0원보다 커야 합니다.");

        } else {
            UserPoint updated = new UserPoint(
                    userPoint.id(),
                    userPoint.point() - amount,
                    System.currentTimeMillis()
            );


            //이력 저장-별도 클래스로 빼려고 했으나 의존성이 심해짐(테스트 코드 작성 시 번거로움)
            PointHistory history = new PointHistory(
                    0L,
                    userId,
                    -amount,
                    TransactionType.USE,
                    System.currentTimeMillis()
            );
            pointHistoryRepository.save(history);


            // 사용 저장 및 반환
            return pointRepository.save(updated);

        }
    }

    //포인트 사용내역 가져오기
    public List<PointHistory> getPointHistories(long userId) {

        List<PointHistory> userHistories;
        userHistories = pointHistoryRepository.findByUserId(userId);

        if(userHistories == null || userHistories.isEmpty()) {
            throw new IllegalArgumentException("포인트 충전/사용 내역이 없습니다.");
        }else {
            return userHistories;
        }
    }


}
