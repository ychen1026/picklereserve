package edu.sjsu.cmpe172.picklereserve.service;

import edu.sjsu.cmpe172.picklereserve.error.ConflictException;
import edu.sjsu.cmpe172.picklereserve.error.NotFoundException;
import edu.sjsu.cmpe172.picklereserve.integration.AppointmentNotificationEvent;
import edu.sjsu.cmpe172.picklereserve.integration.NotificationType;
import edu.sjsu.cmpe172.picklereserve.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingTransactionService {
    private static final Logger log = LoggerFactory.getLogger(BookingTransactionService.class);
    private final BookingRepository booking;
    private final ApplicationEventPublisher events;

    public BookingTransactionService(BookingRepository booking, ApplicationEventPublisher events) {
        this.booking = booking;
        this.events = events;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public long bookOnce(long userId, long slotId, long serviceId) {
        var slot = booking.findSlot(slotId).orElseThrow(() -> new NotFoundException("Slot not found"));
        if (!"OPEN".equals(slot.status())) throw new ConflictException("Slot is not available");
        if (slot.serviceId() != serviceId) throw new IllegalArgumentException("Selected service does not match the slot");
        if (booking.claimVersion(slotId, slot.version()) != 1) throw new ConflictException("Slot was booked by another request");
        try {
            long appointmentId = booking.insertAppointment(userId, slotId, serviceId);
            events.publishEvent(new AppointmentNotificationEvent(appointmentId, NotificationType.BOOKING_CONFIRMED));
            log.info("Booking transaction ready to commit appointmentId={} userId={} slotId={}",
                    appointmentId, userId, slotId);
            return appointmentId;
        } catch (RuntimeException exception) {
            throw new ConflictException("Slot was already booked", exception);
        }
    }
}
