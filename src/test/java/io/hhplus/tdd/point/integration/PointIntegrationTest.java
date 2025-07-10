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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

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

        ((UserPointTable) userPointTable).insertOrUpdate(1L,10000L);//현재 포인트 초기화
    }



    @Test
    void 포인트_조회(){

        //given
        long userId = 1L;
        long existingPoint = 10000L;//현재 포인트 10,000원으로 초기화 ↑

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

        //expected
        long existingPoint = 13000L;

        // when
        UserPoint charge = pointService.pointCharge(userId, chargeAmount);//서비스 호출

        // then(point + chargeAmount)
        //결과검증
        assertThat(charge.point()).isEqualTo(existingPoint);//Stub 현재포인트 10,000 + 충전포인트 3,000 =13,000원

    }

    //포인트 사용 /point/{id}/use
    @Test
    void 포인트_사용_감소(){

        //given
        long userId = 1L;
        long usingPoint = 2000L;

        //expected
        long existingPoint = 8000L;

        // when
        UserPoint userPoint = pointService.pointUse(userId, usingPoint);//서비스 호출

        // then(point - usingPoint)
        assertThat(userPoint.point()).isEqualTo(existingPoint);//Stub 현재포인트 10,000 - 충전포인트 2,000 = 8,000원

    }

    //포인트 사용내역 조회 /point/{id}/histories
    @Test
    void 포인트_사용내역_조회(){

        //given
        long userId = 1L;
        long chargePoint = 5000L;
        long usingPoint = 2000L;
        long now = System.currentTimeMillis();
        ((PointHistoryTable) pointHistoryTable).insert(userId, chargePoint, TransactionType.CHARGE, now);
        ((PointHistoryTable) pointHistoryTable).insert(userId, usingPoint, TransactionType.USE, now + 1);

        // when
        List<PointHistory> pointHistories = pointService.getPointHistories(userId);

        // then
        assertThat(pointHistories).hasSize(2);
        assertThat(pointHistories.get(0).type()).isEqualTo(TransactionType.CHARGE);
        assertThat(pointHistories.get(0).amount()).isEqualTo(5000L);
        assertThat(pointHistories.get(1).type()).isEqualTo(TransactionType.USE);
        assertThat(pointHistories.get(1).amount()).isEqualTo(2000L);

    }

    //잔고가 부족 할 경우, 포인트 사용은 실패
    @Test
    void 포인트_사용_실패_잔고부족() {

        // given
        long userId = 1L;
        long usePoint = 100000L;  // 현재포인트 10,000원, 사용포인트 100,000원

        // when & then
        assertThatThrownBy(() -> pointService.pointUse(userId, usePoint))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("포인트 부족");
    }

    //포인트 사용, 충전 시 0원 사용은 안된다.(이건 근데 서비스 보다는 controller에서 입력값을 아예 안받는게 더 좋은거 같음(서비스의 분기문이 너무 많아짐)
    @Test
    void 포인트_0원_충전_불가(){

        //given
        long userId = 1L;
        long chargeAmount = 0L;

        //when/then
        assertThatThrownBy(()-> pointService.pointCharge(userId, chargeAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("충전 금액은 0원보다 커야 합니다.");
    }

    @Test
    void 포인트_0원_사용_불가(){

        //given
        long userId = 1L;
        long useAmount = 0L;

        //when/then
        assertThatThrownBy(()-> pointService.pointUse(userId, useAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용 금액은 0원보다 커야 합니다.");
    }


    //포인트 충전은 1,000원 단위로만 가능
    //셀프 주차장 생각을 함, 토스페이, 카카오페이, 네이버페이, 쿠팡페이등 여러가지 페이는 1원단위까지 충전가능함
    @Test
    void 포인트_충전_단위_1000(){

        //given
        long userId = 1L;
        long chargeAmount = 15220L;

        //when/then
        assertThatThrownBy(()-> pointService.pointCharge(userId, chargeAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("충전금액은 1,000원 단위로 충전해주시기 바랍니다.");
    }


    @AfterEach
    void tearDown() {
        // 매 테스트 후 정리할 작업
        pointService = null;
        userPointTable = null;
        pointHistoryTable = null;
    }

}
