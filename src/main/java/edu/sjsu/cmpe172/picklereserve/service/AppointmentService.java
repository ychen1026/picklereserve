package edu.sjsu.cmpe172.picklereserve.service;

import java.util.List;
import edu.sjsu.cmpe172.picklereserve.dto.AppointmentResponse;
import edu.sjsu.cmpe172.picklereserve.error.ForbiddenException;
import edu.sjsu.cmpe172.picklereserve.error.NotFoundException;
import edu.sjsu.cmpe172.picklereserve.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {
    private final AppointmentRepository appointments;
    public AppointmentService(AppointmentRepository appointments) { this.appointments = appointments; }
    @Transactional(readOnly=true) public List<AppointmentResponse> forUser(long userId){ return appointments.findForUser(userId); }
    @Transactional(readOnly=true) public List<AppointmentResponse> forProvider(long providerId){ return appointments.findForProvider(providerId); }
    @Transactional public void cancel(long appointmentId, long userId) {
        if (appointments.cancelOwned(appointmentId, userId) == 1) return;
        if (!appointments.exists(appointmentId)) throw new NotFoundException("Appointment not found");
        throw new ForbiddenException("You can only cancel your own booked appointments");
    }
}
