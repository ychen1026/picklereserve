package edu.sjsu.cmpe172.picklereserve;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import edu.sjsu.cmpe172.picklereserve.error.ForbiddenException;
import edu.sjsu.cmpe172.picklereserve.repository.AppointmentRepository;
import edu.sjsu.cmpe172.picklereserve.service.AppointmentService;
import org.junit.jupiter.api.Test;

class AppointmentServiceTest {
    @Test void ownerCanCancel(){ AppointmentRepository r=mock(AppointmentRepository.class); when(r.cancelOwned(7,1)).thenReturn(1); new AppointmentService(r).cancel(7,1); verify(r).cancelOwned(7,1); }
    @Test void nonOwnerIsRejected(){ AppointmentRepository r=mock(AppointmentRepository.class); when(r.cancelOwned(7,1)).thenReturn(0); when(r.exists(7)).thenReturn(true); assertThatThrownBy(()->new AppointmentService(r).cancel(7,1)).isInstanceOf(ForbiddenException.class); }
}
