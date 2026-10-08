package edu.sjsu.cmpe172.picklereserve;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import edu.sjsu.cmpe172.picklereserve.error.ConflictException;
import edu.sjsu.cmpe172.picklereserve.model.BookableSlot;
import edu.sjsu.cmpe172.picklereserve.repository.BookingRepository;
import edu.sjsu.cmpe172.picklereserve.service.BookingTransactionService;
import org.junit.jupiter.api.Test;

class BookingTransactionServiceTest {
    @Test void rejectsMismatchedService(){
        BookingRepository repo=mock(BookingRepository.class); when(repo.findSlot(1)).thenReturn(Optional.of(new BookableSlot(1,1,2,"OPEN",0)));
        assertThatThrownBy(()->new BookingTransactionService(repo).bookOnce(1,1,1)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void optimisticVersionFailureBecomesConflict(){
        BookingRepository repo=mock(BookingRepository.class); when(repo.findSlot(1)).thenReturn(Optional.of(new BookableSlot(1,1,1,"OPEN",3))); when(repo.claimVersion(1,3)).thenReturn(0);
        assertThatThrownBy(()->new BookingTransactionService(repo).bookOnce(1,1,1)).isInstanceOf(ConflictException.class);
    }
}
