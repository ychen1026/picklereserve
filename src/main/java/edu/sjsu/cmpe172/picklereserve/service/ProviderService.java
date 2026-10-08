package edu.sjsu.cmpe172.picklereserve.service;

import java.time.OffsetDateTime;
import edu.sjsu.cmpe172.picklereserve.error.ConflictException;
import edu.sjsu.cmpe172.picklereserve.error.ForbiddenException;
import edu.sjsu.cmpe172.picklereserve.error.NotFoundException;
import edu.sjsu.cmpe172.picklereserve.repository.ProviderRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProviderService {
    private final ProviderRepository providers;
    public ProviderService(ProviderRepository providers) { this.providers = providers; }
    public long providerId(long userId){ return providers.providerIdForUser(userId).orElseThrow(() -> new ForbiddenException("Provider profile required")); }
    @Transactional public long createSlot(long userId, long serviceId, OffsetDateTime start, OffsetDateTime end) {
        if (!end.isAfter(start)) throw new IllegalArgumentException("End time must be after start time");
        long providerId=providerId(userId);
        if (!providers.ownsService(providerId, serviceId)) throw new ForbiddenException("Service does not belong to this provider");
        try { return providers.createSlot(providerId, serviceId, start, end); }
        catch (DataAccessException e) { throw new ConflictException("A slot already exists at that time", e); }
    }
    @Transactional public void removeSlot(long userId, long slotId) {
        long providerId=providerId(userId);
        if (providers.removeOwnedSlot(slotId, providerId) != 1) throw new ConflictException("Slot cannot be removed because it is missing, booked, or not yours");
    }
}
