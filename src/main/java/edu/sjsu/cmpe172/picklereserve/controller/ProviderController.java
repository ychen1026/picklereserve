package edu.sjsu.cmpe172.picklereserve.controller;

import java.util.List;
import java.util.Map;
import edu.sjsu.cmpe172.picklereserve.dto.AppointmentResponse;
import edu.sjsu.cmpe172.picklereserve.dto.CreateSlotRequest;
import edu.sjsu.cmpe172.picklereserve.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/provider")
public class ProviderController {
    private final AuthService auth; private final ProviderService provider; private final AppointmentService appointments;
    public ProviderController(AuthService auth,ProviderService provider,AppointmentService appointments){this.auth=auth;this.provider=provider;this.appointments=appointments;}
    @PostMapping("/slots") @ResponseStatus(HttpStatus.CREATED) public Map<String,Long> create(@Valid @RequestBody CreateSlotRequest r,HttpSession s){var u=auth.requireAdmin(s);return Map.of("slotId",provider.createSlot(u.userId(),r.serviceId(),r.startsAt(),r.endsAt()));}
    @DeleteMapping("/slots/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void remove(@PathVariable long id,HttpSession s){provider.removeSlot(auth.requireAdmin(s).userId(),id);}
    @GetMapping("/appointments") public List<AppointmentResponse> booked(HttpSession s){var u=auth.requireAdmin(s);return appointments.forProvider(provider.providerId(u.userId()));}
}
