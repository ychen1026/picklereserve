package edu.sjsu.cmpe172.picklereserve.service;

import edu.sjsu.cmpe172.picklereserve.error.ConflictException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class BookingService {
    private static final int MAX_ATTEMPTS = 3;
    private final BookingTransactionService transactionService;
    public BookingService(BookingTransactionService transactionService) { this.transactionService = transactionService; }

    public long book(long userId, long slotId, long serviceId) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try { return transactionService.bookOnce(userId, slotId, serviceId); }
            catch (DataAccessException e) {
                if (attempt == MAX_ATTEMPTS) throw new ConflictException("Database was busy; please retry", e);
                try { Thread.sleep(40L * attempt); } catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); throw new ConflictException("Booking interrupted", interrupted); }
            }
        }
        throw new ConflictException("Booking failed");
    }
}
