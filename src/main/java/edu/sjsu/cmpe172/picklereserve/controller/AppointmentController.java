package edu.sjsu.cmpe172.picklereserve.controller;

import java.util.List;
import java.util.Map;
import edu.sjsu.cmpe172.picklereserve.dto.AppointmentResponse;
import edu.sjsu.cmpe172.picklereserve.dto.BookAppointmentRequest;
import edu.sjsu.cmpe172.picklereserve.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AuthService auth; private final BookingService booking; private final AppointmentService appointments;
    public AppointmentController(AuthService auth,BookingService booking,AppointmentService appointments){this.auth=auth;this.booking=booking;this.appointments=appointments;}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Map<String,Long> book(@Valid @RequestBody BookAppointmentRequest r,HttpSession s){ var u=auth.requirePlayer(s); return Map.of("appointmentId",booking.book(u.userId(),r.slotId(),r.serviceId())); }
    @GetMapping("/mine") public List<AppointmentResponse> mine(HttpSession s){ return appointments.forUser(auth.requirePlayer(s).userId()); }
    @PostMapping("/{id}/cancel") @ResponseStatus(HttpStatus.NO_CONTENT) public void cancel(@PathVariable long id,HttpSession s){appointments.cancel(id,auth.requirePlayer(s).userId());}
}
