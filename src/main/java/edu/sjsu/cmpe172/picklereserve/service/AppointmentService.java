package edu.sjsu.cmpe172.picklereserve.service;

import java.util.List;
import edu.sjsu.cmpe172.picklereserve.dto.AppointmentResponse;
import edu.sjsu.cmpe172.picklereserve.error.ForbiddenException;
import edu.sjsu.cmpe172.picklereserve.error.NotFoundException;
import edu.sjsu.cmpe172.picklereserve.integration.AppointmentNotificationEvent;
import edu.sjsu.cmpe172.picklereserve.integration.NotificationType;
import edu.sjsu.cmpe172.picklereserve.repository.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {
    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);
    private final AppointmentRepository appointments;
    private final ApplicationEventPublisher events;
    public AppointmentService(AppointmentRepository appointments, ApplicationEventPublisher events) {
        this.appointments = appointments;
        this.events = events;
    }
    @Transactional(readOnly=true) public List<AppointmentResponse> forUser(long userId){ return appointments.findForUser(userId); }
    @Transactional(readOnly=true) public List<AppointmentResponse> forProvider(long providerId){ return appointments.findForProvider(providerId); }
    @Transactional public void cancel(long appointmentId, long userId) {
        if (appointments.cancelOwned(appointmentId, userId) == 1) {
            events.publishEvent(new AppointmentNotificationEvent(appointmentId, NotificationType.CANCELLATION_CONFIRMED));
            log.info("Cancellation transaction ready to commit appointmentId={} userId={}", appointmentId, userId);
            return;
        }
        if (!appointments.exists(appointmentId)) throw new NotFoundException("Appointment not found");
        throw new ForbiddenException("You can only cancel your own booked appointments");
    }
}
