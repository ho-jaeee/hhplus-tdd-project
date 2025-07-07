package io.hhplus.tdd.point;


import io.hhplus.tdd.point.repository.PointRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;


@ExtendWith(MockitoExtension.class)
public class PointServiceTest {



    @Mock
    PointRepository pointRepository;

    @InjectMocks
    PointService pointService;

    //포인트 조회 /point/{id}
    //포인트 충전 /point/{id}/charge
    @Test
    void 포인트_충전_증가(){
        // given
        long userId = 1L;
        long existingPoint = 5000L;
        long chargeAmount = 3000L;
        long now = System.currentTimeMillis();

        UserPoint existing = new UserPoint(userId, existingPoint, now);
        Mockito.when(pointRepository.findById(userId)).thenReturn(Optional.of(existing));

        // save() 호출 시 그대로 리턴하도록 설정
        Mockito.when(pointRepository.save(Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));


        // when
        UserPoint result = pointService.pointCharge(userId, chargeAmount);

        // then
        Assertions.assertThat(result.point()).as("기존 포인트에 충전 금액이 더해져야 함")
                .isEqualTo(existingPoint + chargeAmount);//Stub

        // findById()가 정확히 1번 호출됐는지 검증
        Mockito.verify(pointRepository, Mockito.times(1)).findById(userId);

        // save()도 정확히 1번 호출됐는지 검증
        Mockito.verify(pointRepository, Mockito.times(1)).save(Mockito.any(UserPoint.class));
    }

    //포인트 사용 /point/{id}/use
    //포인트 사용내역 조회 /point/{id}/histories
    //잔고가 부족 할 경우, 포인트 사용은 실패


}