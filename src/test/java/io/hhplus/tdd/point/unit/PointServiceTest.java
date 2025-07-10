package io.hhplus.tdd.point.unit;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {


    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @InjectMocks
    PointService pointService;


    //포인트 조회 /point/{id}
    @Test
    void 포인트_조회(){

        //given
        long userId = 1L;
        long existingPoint = 5000L;
        UserPoint existing = new UserPoint(userId, existingPoint, System.currentTimeMillis());
        Mockito.when(userPointTable.selectById(userId)).thenReturn(existing);

        // when
        long result = pointService.getPoint(userId); // 실제 호출

        // then
        // 결과 검증
        assertThat(result).isEqualTo(existingPoint);//Stub

        // mock이 실제 호출됐는지 검증
        Mockito.verify(userPointTable, Mockito.times(1)).selectById(userId);


    }

    //포인트 충전 /point/{id}/charge
    @Test
    void 포인트_충전_증가(){
        // given
        long userId = 1L;
        long chargeAmount = 3000L;
        long existingPoint = 5000L;
        long now = System.currentTimeMillis();

        UserPoint existing = new UserPoint(userId, existingPoint, now);
        // 고정된 ID 1L에 대해 항상 이 UserPoint를 줘야 할 때
        Mockito.when(userPointTable.selectById(userId)).thenReturn(existing);

        // 저장할 때마다 들어오는 객체가 다를 수 있음 → 그대로 리턴
       Mockito.when(userPointTable.insertOrUpdate(Mockito.anyLong(), Mockito.anyLong()))
               .thenAnswer(invocation -> {
                   long id = invocation.getArgument(0);
                   long amount = invocation.getArgument(1);
                   return new UserPoint(id, amount, System.currentTimeMillis());
               });


        // when
        UserPoint result = pointService.pointCharge(userId, chargeAmount);

        // then
        assertThat(result.point()).as("기존 포인트에 충전 금액이 더해져야 함")
                .isEqualTo(existingPoint + chargeAmount);//Stub

        // findById()가 정확히 1번 호출됐는지 검증
        Mockito.verify(userPointTable, Mockito.times(1)).selectById(userId);

        // save()도 정확히 1번 호출됐는지 검증
        Mockito.verify(userPointTable, Mockito.times(1)).insertOrUpdate(Mockito.anyLong(), Mockito.anyLong());
    }

    //포인트 사용 /point/{id}/use
    @Test
    void 포인트_사용_감소(){

        //given
        long userId = 1L;
        long usingPoint = 2000L;
        long existingPoint = 5000L;
        UserPoint existing = new UserPoint(userId, existingPoint, System.currentTimeMillis());
        Mockito.when(userPointTable.selectById(userId)).thenReturn(existing);


        // 저장할 때마다 들어오는 객체가 다를 수 있음 → 그대로 리턴
        Mockito.when(userPointTable.insertOrUpdate(Mockito.anyLong(), Mockito.anyLong()))
                .thenAnswer(invocation -> {
                    long id = invocation.getArgument(0);
                    long amount = invocation.getArgument(1);
                    return new UserPoint(id, amount, System.currentTimeMillis());
                });


        // when
        UserPoint result = pointService.pointUse(userId, usingPoint);

        // then
        assertThat(result.point()).as("기존 포인트에 사용 금액이 빼져야 함")
                .isEqualTo(existingPoint - usingPoint);//Stub

        // findById()가 정확히 1번 호출됐는지 검증
        Mockito.verify(userPointTable, Mockito.times(1)).selectById(userId);

        // save()도 정확히 1번 호출됐는지 검증
        Mockito.verify(userPointTable, Mockito.times(1)).insertOrUpdate(Mockito.anyLong(), Mockito.anyLong());

    }


    //포인트 사용내역 조회 /point/{id}/histories
    @Test
    void 포인트_사용내역_조회(){

        //given
        long userId = 1L;
        List<PointHistory> pointHistories = List.of(
                new PointHistory(1L, userId, -2000L, TransactionType.USE, System.currentTimeMillis()),
                new PointHistory(1L, userId, 5000L, TransactionType.CHARGE, System.currentTimeMillis())
        );
        Mockito.when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(pointHistories);

        // when
        List<PointHistory> result = pointService.getPointHistories(userId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).type()).isEqualTo(TransactionType.USE);
        assertThat(result.get(0).amount()).isEqualTo(-2000L);
        assertThat(result.get(1).type()).isEqualTo(TransactionType.CHARGE);
        assertThat(result.get(1).amount()).isEqualTo(5000L);

        // verify
        Mockito.verify(pointHistoryTable).selectAllByUserId(userId);
    }

}