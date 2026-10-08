package edu.sjsu.cmpe172.picklereserve;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import edu.sjsu.cmpe172.picklereserve.service.BookingService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class BookingConcurrencyTest {
    @Autowired BookingService booking;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach void reset(){ jdbc.update("DELETE FROM appointments"); jdbc.update("UPDATE availability_slots SET version=0,status='OPEN' WHERE id=1"); }

    @Test
    void exactlyOneConcurrentBookingSucceeds() throws Exception {
        ExecutorService pool=Executors.newFixedThreadPool(2);
        CountDownLatch ready=new CountDownLatch(2), start=new CountDownLatch(1);
        AtomicInteger successes=new AtomicInteger();
        Callable<Void> first=attempt(1,ready,start,successes);
        Callable<Void> second=attempt(4,ready,start,successes);
        Future<Void> a=pool.submit(first), b=pool.submit(second);
        assertThat(ready.await(2,TimeUnit.SECONDS)).isTrue(); start.countDown();
        a.get(5,TimeUnit.SECONDS); b.get(5,TimeUnit.SECONDS); pool.shutdownNow();
        Integer booked=jdbc.queryForObject("SELECT COUNT(*) FROM appointments WHERE slot_id=1 AND status='BOOKED'",Integer.class);
        assertThat(successes.get()).isEqualTo(1); assertThat(booked).isEqualTo(1);
    }
    private Callable<Void> attempt(long user,CountDownLatch ready,CountDownLatch start,AtomicInteger successes){return ()->{ready.countDown();start.await();try{booking.book(user,1,1);successes.incrementAndGet();}catch(RuntimeException expected){}return null;};}
}
