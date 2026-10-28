package edu.sjsu.cmpe172.picklereserve;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import edu.sjsu.cmpe172.picklereserve.error.ForbiddenException;
import edu.sjsu.cmpe172.picklereserve.integration.AppointmentNotificationEvent;
import edu.sjsu.cmpe172.picklereserve.integration.NotificationType;
import edu.sjsu.cmpe172.picklereserve.repository.AppointmentRepository;
import edu.sjsu.cmpe172.picklereserve.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

class AppointmentServiceTest {
    @Test void ownerCanCancel(){ AppointmentRepository r=mock(AppointmentRepository.class); ApplicationEventPublisher e=mock(ApplicationEventPublisher.class); when(r.cancelOwned(7,1)).thenReturn(1); new AppointmentService(r,e).cancel(7,1); verify(r).cancelOwned(7,1); ArgumentCaptor<Object> event=ArgumentCaptor.forClass(Object.class); verify(e).publishEvent(event.capture()); assertThat(event.getValue()).isEqualTo(new AppointmentNotificationEvent(7,NotificationType.CANCELLATION_CONFIRMED)); }
    @Test void nonOwnerIsRejected(){ AppointmentRepository r=mock(AppointmentRepository.class); ApplicationEventPublisher e=mock(ApplicationEventPublisher.class); when(r.cancelOwned(7,1)).thenReturn(0); when(r.exists(7)).thenReturn(true); assertThatThrownBy(()->new AppointmentService(r,e).cancel(7,1)).isInstanceOf(ForbiddenException.class); verifyNoInteractions(e); }
}
