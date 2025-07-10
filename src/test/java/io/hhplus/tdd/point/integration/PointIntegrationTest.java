package io.hhplus.tdd.point.integration;


import io.hhplus.tdd.point.TransactionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest// Mockito가 아닌 스프링을 올려서 하는 테스트
public class PointIntegrationTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserPointTable userPointTable;

    @Autowired
    private PointHistoryTable pointHistoryTable;

    @BeforeEach
    void setUp() {
        userPointTable = new UserPointTable(); // 직접 생성
        pointHistoryTable = new PointHistoryTable(); // 직접 생성
        pointService = new PointService(pointHistoryTable, userPointTable); // 수동 주입

        ((UserPointTable) userPointTable).insertOrUpdate(1L,10000L);
    }



    @Test
    void 포인트_조회(){

        //given
        long userId = 1L;
        long existingPoint = 10000L;

        // when
        //pointService.pointCharge(userId, existingPoint);
        long result = pointService.getPoint(userId); // 실제 호출

        // then
        // 결과 검증
        assertThat(result).isEqualTo(existingPoint);//Stub


    }

    @Test
    //포인트 충전 /point/{id}/charge
    void 포인트_충전_증가(){
        // given
        long userId = 1L;
        long chargeAmount = 3000L;
        long existingPoint = 13000L;


        // when
        UserPoint charge = pointService.pointCharge(userId, chargeAmount);

        // then
        assertThat(charge.point()).isEqualTo(existingPoint);//Stub 10,000 + 3,000

    }

    //포인트 사용 /point/{id}/use
    @Test
    void 포인트_사용_감소(){

        //given
        long userId = 1L;
        long usingPoint = 2000L;
        long existingPoint = 8000L;

        // when
        UserPoint userPoint = pointService.pointUse(userId, usingPoint);

        // then(point - usingPoint)
        assertThat(userPoint.point()).isEqualTo(existingPoint);//Stub 10,000 - 2,000

    }

    //포인트 사용내역 조회 /point/{id}/histories
    @Test
    void 포인트_사용내역_조회(){

        //given
        long userId = 1L;
        long chargePoint = 5000L;
        long usingPoint = 2000L;
        long now = System.currentTimeMillis();


        // when
        // UserPoint chargedPoint = pointService.pointCharge(userId, chargePoint);
        // UserPoint userPoint = pointService.pointUse(userId, usingPoint);

        ((PointHistoryTable) pointHistoryTable).insert(userId, chargePoint, TransactionType.CHARGE, now);
        ((PointHistoryTable) pointHistoryTable).insert(userId, usingPoint, TransactionType.USE, now + 1);

        List<PointHistory> pointHistories = pointService.getPointHistories(userId);


        // then
        assertThat(pointHistories).hasSize(2);

        assertThat(pointHistories.get(0).type()).isEqualTo(TransactionType.CHARGE);
        assertThat(pointHistories.get(0).amount()).isEqualTo(5000L);
        assertThat(pointHistories.get(1).type()).isEqualTo(TransactionType.USE);
        assertThat(pointHistories.get(1).amount()).isEqualTo(2000L);



    }


    @AfterEach
    void tearDown() {
        // 매 테스트 후 정리할 작업
        pointService = null;
        userPointTable = null;
        pointHistoryTable = null;
    }


}
