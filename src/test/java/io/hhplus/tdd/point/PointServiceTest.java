package io.hhplus.tdd.point;


import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.PointRepository;

//import org.junit.jupiter.api.Assertions;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


@ExtendWith(MockitoExtension.class)
public class PointServiceTest {



    @Mock
    PointRepository pointRepository;

    @Mock
    PointHistoryRepository pointHistoryRepository;

    @InjectMocks
    PointService pointService;


    //포인트 조회 /point/{id}
    @Test
    void 포인트_조회(){

        //given
        long userId = 1L;
        long existingPoint = 5000L;
        UserPoint existing = new UserPoint(userId, existingPoint, System.currentTimeMillis());
        Mockito.when(pointRepository.findById(userId)).thenReturn(Optional.of(existing));

        // when
        long result = pointService.getPoint(userId); // 실제 호출

        // then
        // 결과 검증
        assertThat(result).isEqualTo(existingPoint);//Stub

        // mock이 실제 호출됐는지 검증
        Mockito.verify(pointRepository, Mockito.times(1)).findById(userId);


    }

    //포인트 충전 /point/{id}/charge
    @Test
    void 포인트_충전_증가(){
        // given
        long userId = 1L;
        long existingPoint = 5000L;
        long chargeAmount = 3000L;
        long now = System.currentTimeMillis();

        UserPoint existing = new UserPoint(userId, existingPoint, now);
        // 고정된 ID 1L에 대해 항상 이 UserPoint를 줘야 할 때
        Mockito.when(pointRepository.findById(userId)).thenReturn(Optional.of(existing));

        // 저장할 때마다 들어오는 객체가 다를 수 있음 → 그대로 리턴
       Mockito.when(pointRepository.save(Mockito.any()))
               .thenAnswer(invocation -> invocation.getArgument(0));


        // when
        UserPoint result = pointService.pointCharge(userId, chargeAmount);

        // then
        assertThat(result.point()).as("기존 포인트에 충전 금액이 더해져야 함")
                .isEqualTo(existingPoint + chargeAmount);//Stub

        // findById()가 정확히 1번 호출됐는지 검증
        Mockito.verify(pointRepository, Mockito.times(1)).findById(userId);

        // save()도 정확히 1번 호출됐는지 검증
        Mockito.verify(pointRepository, Mockito.times(1)).save(Mockito.any(UserPoint.class));
    }

    //포인트 사용 /point/{id}/use
    @Test
    void 포인트_사용_감소(){

        //given
        long userId = 1L;
        long existingPoint = 5000L;
        long usingPoint = 2000L;
        long expectedPoint = 3000L;
        UserPoint existing = new UserPoint(userId, existingPoint, System.currentTimeMillis());
        Mockito.when(pointRepository.findById(userId)).thenReturn(Optional.of(existing));

        // when
        pointService.pointUse(userId, usingPoint); // 실제 호출

        // then
        ArgumentCaptor<UserPoint> captor = ArgumentCaptor.forClass(UserPoint.class);
        Mockito.verify(pointRepository).save(captor.capture());

        UserPoint saved = captor.getValue();
        assertThat(saved.point()).as("포인트 사용 후 잔액은 기존 - 사용금액이어야 한다")
                .isEqualTo(expectedPoint);

    }

    //잔고가 부족 할 경우, 포인트 사용은 실패
    @Test
    void 포인트_사용_실패_잔고부족() {
        // given
        long userId = 1L;
        long existingPoint = 5000L;
        long usePoint = 10000L;

        UserPoint existing = new UserPoint(userId, existingPoint, System.currentTimeMillis());
        Mockito.when(pointRepository.findById(userId)).thenReturn(Optional.of(existing));

        // when & then
        assertThatThrownBy(() -> pointService.pointUse(userId, usePoint))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("포인트 부족");

        // verify (저장 안 됐는지 확인도 가능)
        Mockito.verify(pointRepository, Mockito.never()).save(Mockito.any());
        //Mockito.verify(pointHistoryRepository, Mockito.never()).save(Mockito.any());
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
        Mockito.when(pointHistoryRepository.findByUserId(userId)).thenReturn(pointHistories);

        // when
        List<PointHistory> result = pointService.getPointHistories(userId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).type()).isEqualTo(TransactionType.USE);
        assertThat(result.get(0).amount()).isEqualTo(-2000L);
        assertThat(result.get(1).type()).isEqualTo(TransactionType.CHARGE);
        assertThat(result.get(1).amount()).isEqualTo(5000L);

        // verify
        Mockito.verify(pointHistoryRepository).findByUserId(userId);

    }

    //포인트 사용, 충전 시 0원 사용은 안된다.(이건 근데 서비스 보다는 controller에서 입력값을 아예 안받는게 더 좋은거 같음(서비스의 분기문이 너무 많아짐)
    @Test
    void 포인트_0원_충전_불가(){

        //given
        long userId = 1L;
        long chargeAmount = 0L;
        long now = System.currentTimeMillis();

        UserPoint charging = new UserPoint(userId, chargeAmount, now);
        Mockito.when(pointRepository.findById(userId)).thenReturn(Optional.of(charging));


        //when/then
        assertThatThrownBy(()-> pointService.pointCharge(userId, chargeAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("충전 금액은 0원보다 커야 합니다.");


        // 저장이 호출되지 않았는지도 검증
        Mockito.verify(pointRepository, Mockito.never()).save(Mockito.any());


    }

    @Test
    void 포인트_0원_사용_불가(){

        //given
        long userId = 1L;
        long useAmount = 0L;
        long now = System.currentTimeMillis();

        UserPoint charging = new UserPoint(userId, useAmount, now);
        Mockito.when(pointRepository.findById(userId)).thenReturn(Optional.of(charging));


        //when/then
        assertThatThrownBy(()-> pointService.pointUse(userId, useAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용 금액은 0원보다 커야 합니다.");


        // 저장이 호출되지 않았는지도 검증
        Mockito.verify(pointRepository, Mockito.never()).save(Mockito.any());


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
        Mockito.when(pointRepository.findById(userId)).thenReturn(Optional.of(charging));

        //when/then
        assertThatThrownBy(()-> pointService.pointCharge(userId, chargeAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("충전금액은 1,000원 단위로 충전해주시기 바랍니다.");

        // 저장이 호출되지 않았는지도 검증
        Mockito.verify(pointRepository, Mockito.never()).save(Mockito.any());

    }
}