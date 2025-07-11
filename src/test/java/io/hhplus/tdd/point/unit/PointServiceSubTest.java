package io.hhplus.tdd.point.unit;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class PointServiceSubTest {//제약조건

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @InjectMocks
    PointService pointService;
    //잔고가 부족 할 경우, 포인트 사용은 실패
    @Test
    void 포인트_사용_실패_잔고부족() {
        // given
        long userId = 1L;
        long existingPoint = 5000L;
        long usePoint = 10000L;

        UserPoint existing = new UserPoint(userId, existingPoint, System.currentTimeMillis());
        Mockito.when(userPointTable.selectById(userId)).thenReturn(existing);

        // when & then
        assertThatThrownBy(() -> pointService.pointUse(userId, usePoint))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("포인트 부족");

        // verify (저장 안 됐는지 확인도 가능)
        Mockito.verify(userPointTable, Mockito.never()).insertOrUpdate(Mockito.anyLong(), Mockito.anyLong());

    }

    //포인트 사용, 충전 시 0원 사용은 안된다.(이건 근데 서비스 보다는 controller에서 입력값을 아예 안받는게 더 좋은거 같음(서비스의 분기문이 너무 많아짐)
    @Test
    void 포인트_0원_충전_불가(){

        //given
        long userId = 1L;
        long chargeAmount = 0L;
        long now = System.currentTimeMillis();

        UserPoint charging = new UserPoint(userId, chargeAmount, now);
        Mockito.when(userPointTable.selectById(userId)).thenReturn(charging);


        //when/then
        assertThatThrownBy(()-> pointService.pointCharge(userId, chargeAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("충전 금액은 0원보다 커야 합니다.");


        // 저장이 호출되지 않았는지도 검증
        Mockito.verify(userPointTable, Mockito.never()).insertOrUpdate(Mockito.anyLong(), Mockito.anyLong());


    }

    @Test
    void 포인트_0원_사용_불가(){
        //given
        long userId = 1L;
        long useAmount = 0L;
        long now = System.currentTimeMillis();

        UserPoint charging = new UserPoint(userId, useAmount, now);
        Mockito.when(userPointTable.selectById(userId)).thenReturn(charging);


        //when/then
        assertThatThrownBy(()-> pointService.pointUse(userId, useAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용 금액은 0원보다 커야 합니다.");


        // 저장이 호출되지 않았는지도 검증
        Mockito.verify(userPointTable, Mockito.never()).insertOrUpdate(Mockito.anyLong(), Mockito.anyLong());
    }

    //1회 사용, 충전 시 최대 포인트는 100,000원으로 한정(100,000원 이상 입력을 못하게 해야할듯, 이것도 controller가 좋을듯)
    //포인트 충전은 1,000원 단위로 하는게 좋을듯
    @Test
    void 포인트_충전_단위_1000(){

        //given
        long userId = 1L;
        long chargeAmount = 15200L;
        long now = System.currentTimeMillis();
        UserPoint charging = new UserPoint(userId, chargeAmount, now);
        Mockito.when(userPointTable.selectById(userId)).thenReturn(charging);

        //when/then
        assertThatThrownBy(()-> pointService.pointCharge(userId, chargeAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("충전금액은 1,000원 단위로 충전해주시기 바랍니다.");

        // 저장이 호출되지 않았는지도 검증
        Mockito.verify(userPointTable, Mockito.never()).insertOrUpdate(Mockito.anyLong(), Mockito.anyLong());

    }

}
