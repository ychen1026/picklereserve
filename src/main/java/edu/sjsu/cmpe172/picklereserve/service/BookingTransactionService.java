package edu.sjsu.cmpe172.picklereserve.service;

import edu.sjsu.cmpe172.picklereserve.error.ConflictException;
import edu.sjsu.cmpe172.picklereserve.error.NotFoundException;
import edu.sjsu.cmpe172.picklereserve.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingTransactionService {
    private final BookingRepository booking;
    public BookingTransactionService(BookingRepository booking) { this.booking = booking; }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public long bookOnce(long userId, long slotId, long serviceId) {
        var slot = booking.findSlot(slotId).orElseThrow(() -> new NotFoundException("Slot not found"));
        if (!"OPEN".equals(slot.status())) throw new ConflictException("Slot is not available");
        if (slot.serviceId() != serviceId) throw new IllegalArgumentException("Selected service does not match the slot");
        if (booking.claimVersion(slotId, slot.version()) != 1) throw new ConflictException("Slot was booked by another request");
        try {
            return booking.insertAppointment(userId, slotId, serviceId);
        } catch (RuntimeException exception) {
            throw new ConflictException("Slot was already booked", exception);
        }
    }
}
