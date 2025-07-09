package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointService {


    private final PointHistoryTable pointHistoryTable;
    private final UserPointTable userPointTable;

    public PointService(PointHistoryTable pointHistoryTable, UserPointTable userPointTable) {
        this.pointHistoryTable = pointHistoryTable;
        this.userPointTable = userPointTable;
    }


    //포인트 충전
    public UserPoint pointCharge(long userId, long amount) {

        // 사용자 기존 포인트 조회 or 신규 생성 시 0포인트로 생성함
        UserPoint current = userPointTable.selectById(userId);

        //0보다 작은 값은 충전 불가
        if(amount <= 0) {
           throw new IllegalArgumentException("충전 금액은 0원보다 커야 합니다.");//이 조건은 컨트롤러에서 하고싶음
        //1000단위로 충전 가능
       } else if (amount%1000 != 0) {
            throw new IllegalArgumentException("충전금액은 1,000원 단위로 충전해주시기 바랍니다.");
        } else {
           //이력 저장
           pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());
           //기존 포인트 값에 추가
           long updatePoint = current.point() + amount;
           // 충전 저장 및 반환
           return userPointTable.insertOrUpdate(userId, updatePoint);
       }
    }

    //포인트 조회
    public long getPoint(long userId) {

        //사용자 정보 조회
        UserPoint userPoint = userPointTable.selectById(userId);

        // 순수하게 포인트만 리턴
        return userPoint.point();

    }

    //포인트 사용
    public UserPoint pointUse(long userId, long amount) {
        UserPoint userPoint = userPointTable.selectById(userId);

        if (userPoint.point() < amount) {
            throw new IllegalArgumentException("포인트 부족");

        } else if (amount<=0) {
            throw new IllegalArgumentException("사용 금액은 0원보다 커야 합니다.");

        } else {

            //이력 저장
            pointHistoryTable.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());
            //포인트 사용 값 반영
            long updatePoint = userPoint.point() - amount;
            // 사용 저장 및 반환
            return userPointTable.insertOrUpdate(userId, updatePoint);

        }
    }

    //포인트 사용내역 가져오기
    public List<PointHistory> getPointHistories(long userId) {

        List<PointHistory> userHistories;
        userHistories = pointHistoryTable.selectAllByUserId(userId);

        if(userHistories == null || userHistories.isEmpty()) {
            throw new IllegalArgumentException("포인트 충전/사용 내역이 없습니다.");
        }else {
            return userHistories;
        }
    }


}
