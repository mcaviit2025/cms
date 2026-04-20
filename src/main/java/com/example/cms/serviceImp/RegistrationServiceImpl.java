package com.example.cms.serviceImp;

import com.example.cms.entity.Registration;
import com.example.cms.repository.RegistrationRepository;
import com.example.cms.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    @Autowired
    private RegistrationRepository repo;

    @Override
    public Registration save(Registration reg) {

        // 🔥 ONLY validate for NEW registration
        if (reg.getId() == 0) {

            int eventId = reg.getEventId();
            String leaderEmail = reg.getLeaderEmail();

            // ✅ Leader duplicate check
            if (repo.existsByEventIdAndLeaderEmail(eventId, leaderEmail)) {
                throw new RuntimeException("You already registered for this event!");
            }

            // ✅ Leader already in another team
            if (repo.existsByEventIdAndMemberEmailsContaining(eventId, leaderEmail)) {
                throw new RuntimeException("You are already part of a team in this event!");
            }

            // ✅ Member validation
            if (reg.getMemberEmails() != null && !reg.getMemberEmails().isEmpty()) {

                String[] emails = reg.getMemberEmails().split(",");

                for (String email : emails) {

                    email = email.trim();

                    if (email.isEmpty()) continue;

                    // check if member is leader somewhere
                    if (repo.existsByEventIdAndLeaderEmail(eventId, email)) {
                        throw new RuntimeException("Member " + email + " already registered!");
                    }

                    // check if member already in another team
                    if (repo.existsByEventIdAndMemberEmailsContaining(eventId, email)) {
                        throw new RuntimeException("Member " + email + " already in another team!");
                    }
                }
            }

            // ✅ Default status for new registration
            reg.setStatus("PENDING");
            reg.setEmailStatus("NOT_SENT");
        }

        // ✅ ADMIN FLOW (no validation)
        return repo.save(reg);
    }

    @Override
    public Registration getById(int id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    public List<Registration> getByEvent(int eventId) {
        return repo.findByEventId(eventId);
    }

    @Override
    public void delete(int id) {
        repo.deleteById(id);
    }

    @Override
    public boolean exists(String email, int eventId) {
        return repo.existsByLeaderEmailAndEventId(email, eventId);
    }

    @Override
    public List<Registration> getApprovedByEvent(int eventId) {
        return repo.findByEventIdAndStatus(eventId, "APPROVED");
    }

    @Override
    public List<Registration> getRegistrationsByEventId(int eventId) {
        return repo.findByEventId(eventId);
    }

    @Override
    public Registration getRegistrationById(int id) {
        Optional<Registration> registration = repo.findById(id);
        return registration.orElse(null);
    }
}