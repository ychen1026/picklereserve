package edu.sjsu.cmpe172.picklereserve.ai;

import java.time.LocalDate;
import java.util.stream.Collectors;
import edu.sjsu.cmpe172.picklereserve.dto.AppointmentResponse;
import edu.sjsu.cmpe172.picklereserve.dto.SlotResponse;
import edu.sjsu.cmpe172.picklereserve.error.ConflictException;
import edu.sjsu.cmpe172.picklereserve.service.AppointmentService;
import edu.sjsu.cmpe172.picklereserve.service.BookingService;
import edu.sjsu.cmpe172.picklereserve.service.SlotService;
import org.springframework.ai.tool.annotation.Tool;

/** Tools exposed to Gemini. The user id is supplied by the authenticated session. */
public class PickleReserveTools {
    private final long userId;
    private final String conversationId;
    private final SlotService slots;
    private final AppointmentService appointments;
    private final BookingService booking;
    private final PendingActionStore pending;

    public PickleReserveTools(long userId, String conversationId, SlotService slots,
            AppointmentService appointments, BookingService booking, PendingActionStore pending) {
        this.userId = userId;
        this.conversationId = conversationId;
        this.slots = slots;
        this.appointments = appointments;
        this.booking = booking;
        this.pending = pending;
    }

    @Tool(description = "Check open PickleReserve slots, optionally filtered by date, provider, or service.")
    public String checkAvailability(String date, Long providerId, Long serviceId) {
        LocalDate parsedDate = date == null || date.isBlank() ? null : LocalDate.parse(date);
        var result = slots.findAvailableSlots(providerId, serviceId, parsedDate, 0, 10);
        if (result.content().isEmpty()) return "No open slots matched those filters.";
        return result.content().stream().map(this::slotText).collect(Collectors.joining("\n"));
    }

    @Tool(description = "List the authenticated player's appointments, including booked and cancelled appointments.")
    public String listMyAppointments() {
        var result = appointments.forUser(userId);
        if (result.isEmpty()) return "You do not have any appointments.";
        return result.stream().map(this::appointmentText).collect(Collectors.joining("\n"));
    }

    @Tool(description = "Book a slot. Always call with confirmed=false first to show the user a summary. Only call with confirmed=true after the user explicitly confirms the exact pending booking.")
    public String bookAppointment(long slotId, long serviceId, boolean confirmed) {
        SlotResponse slot = slots.findAvailableSlots(null, serviceId, null, 0, 100).content().stream()
                .filter(candidate -> candidate.slotId() == slotId).findFirst().orElse(null);
        if (slot == null) return "That slot is no longer available. Use checkAvailability to offer alternatives.";

        String summary = "Book " + slot.serviceName() + " with " + slot.providerName() + " from "
                + slot.startsAt() + " to " + slot.endsAt() + " for $" + slot.priceDollars() + ".";
        String key = actionKey();
        if (!confirmed) {
            pending.put(key, new PendingActionStore.PendingAction("BOOK", slotId, serviceId, summary));
            return "CONFIRMATION_REQUIRED: " + summary + " Ask the user to confirm before booking.";
        }

        PendingActionStore.PendingAction action = pending.remove(key);
        if (action == null || !"BOOK".equals(action.type()) || action.appointmentOrSlotId() != slotId
                || action.serviceId() != serviceId) {
            return "I cannot book this yet because there is no matching confirmed request. Present the summary first.";
        }
        try {
            long appointmentId = booking.book(userId, slotId, serviceId);
            return "BOOKED: appointment " + appointmentId + " was created. A confirmation notification will be sent.";
        } catch (ConflictException exception) {
            return "The booking could not be completed because the slot was taken. Use checkAvailability to offer alternatives.";
        }
    }

    @Tool(description = "Cancel an appointment owned by the authenticated player. Always call with confirmed=false first, then require explicit confirmation before confirmed=true.")
    public String cancelAppointment(long appointmentId, boolean confirmed) {
        AppointmentResponse appointment = appointments.forUser(userId).stream()
                .filter(candidate -> candidate.appointmentId() == appointmentId
                        && "BOOKED".equals(candidate.status())).findFirst().orElse(null);
        if (appointment == null) return "That appointment was not found or is not an active appointment owned by you.";
        String summary = "Cancel " + appointment.serviceName() + " with " + appointment.providerName()
                + " on " + appointment.startsAt() + ".";
        String key = actionKey();
        if (!confirmed) {
            pending.put(key, new PendingActionStore.PendingAction("CANCEL", appointmentId, 0, summary));
            return "CONFIRMATION_REQUIRED: " + summary + " Ask the user to confirm before cancelling.";
        }
        var action = pending.remove(key);
        if (action == null || !"CANCEL".equals(action.type()) || action.appointmentOrSlotId() != appointmentId) {
            return "I cannot cancel this yet because there is no matching confirmed request.";
        }
        appointments.cancel(appointmentId, userId);
        return "CANCELLED: appointment " + appointmentId + " was cancelled.";
    }

    private String actionKey() {
        return userId + ":" + conversationId;
    }

    private String slotText(SlotResponse slot) {
        return "slotId=" + slot.slotId() + ", service=" + slot.serviceName() + ", provider="
                + slot.providerName() + ", starts=" + slot.startsAt() + ", ends=" + slot.endsAt()
                + ", price=$" + slot.priceDollars();
    }

    private String appointmentText(AppointmentResponse appointment) {
        return "appointmentId=" + appointment.appointmentId() + ", status=" + appointment.status()
                + ", service=" + appointment.serviceName() + ", provider=" + appointment.providerName()
                + ", starts=" + appointment.startsAt();
    }
}
