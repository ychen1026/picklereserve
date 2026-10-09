package edu.sjsu.cmpe172.picklereserve.controller;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import edu.sjsu.cmpe172.picklereserve.auth.AuthenticatedUser;
import edu.sjsu.cmpe172.picklereserve.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebController {
    private final HomeService home; private final SlotService slots; private final AuthService auth; private final BookingService booking; private final AppointmentService appointments; private final ProviderService provider;
    public WebController(HomeService home,SlotService slots,AuthService auth,BookingService booking,AppointmentService appointments,ProviderService provider){this.home=home;this.slots=slots;this.auth=auth;this.booking=booking;this.appointments=appointments;this.provider=provider;}
    private AuthenticatedUser current(HttpSession s){Object u=s.getAttribute(AuthService.SESSION_USER);return u instanceof AuthenticatedUser a?a:null;}
    @GetMapping("/") public String home(Model m,HttpSession s){m.addAttribute("home",home.getHome());m.addAttribute("user",current(s));return "home";}
    @GetMapping("/login") public String loginPage(){return "login";}
    @PostMapping("/login") public String login(@RequestParam String username,@RequestParam String password,HttpSession s,RedirectAttributes r){try{auth.login(username,password,s);return "redirect:/";}catch(RuntimeException e){r.addFlashAttribute("error",e.getMessage());return "redirect:/login";}}
    @PostMapping("/logout") public String logout(HttpSession s){s.invalidate();return "redirect:/";}
    @GetMapping("/slots") public String slotPage(@RequestParam(required=false) Long providerId,@RequestParam(required=false) Long serviceId,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate date,@RequestParam(defaultValue="0") int page,Model m,HttpSession s){m.addAttribute("slots",slots.findAvailableSlots(providerId,serviceId,date,page,20));m.addAttribute("user",current(s));m.addAttribute("providerId",providerId);m.addAttribute("serviceId",serviceId);m.addAttribute("date",date);return "slots";}
    @PostMapping("/book") public String book(@RequestParam long slotId,@RequestParam long serviceId,HttpSession s,RedirectAttributes r){var u=auth.requirePlayer(s);long id=booking.book(u.userId(),slotId,serviceId);return "redirect:/confirmation?id="+id;}
    @GetMapping("/confirmation") public String confirmation(@RequestParam long id, Model m, HttpSession s){auth.requirePlayer(s);m.addAttribute("appointmentId",id);m.addAttribute("user",current(s));return "confirmation";}
    @GetMapping("/appointments") public String myAppointments(Model m,HttpSession s){var u=auth.requirePlayer(s);m.addAttribute("user",u);m.addAttribute("appointments",appointments.forUser(u.userId()));return "appointments";}
    @PostMapping("/appointments/{id}/cancel") public String cancel(@PathVariable long id,HttpSession s,RedirectAttributes r){appointments.cancel(id,auth.requirePlayer(s).userId());r.addFlashAttribute("message","Appointment cancelled");return "redirect:/appointments";}
    @GetMapping("/provider") public String providerPage(Model m,HttpSession s){var u=auth.requireAdmin(s);m.addAttribute("user",u);long providerId=provider.providerId(u.userId());m.addAttribute("appointments",appointments.forProvider(providerId));m.addAttribute("slots",slots.findAvailableSlots(providerId,null,null,0,100).content());return "provider";}
    @PostMapping("/provider/slots") public String createSlot(@RequestParam long serviceId,@RequestParam String startsAt,@RequestParam String endsAt,HttpSession s,RedirectAttributes r){provider.createSlot(auth.requireAdmin(s).userId(),serviceId,OffsetDateTime.parse(startsAt),OffsetDateTime.parse(endsAt));r.addFlashAttribute("message","Availability slot created");return "redirect:/provider";}
    @PostMapping("/provider/slots/{id}/remove") public String removeSlot(@PathVariable long id,HttpSession s,RedirectAttributes r){provider.removeSlot(auth.requireAdmin(s).userId(),id);r.addFlashAttribute("message","Availability slot removed");return "redirect:/provider";}
}
