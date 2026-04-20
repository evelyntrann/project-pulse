package com.projectpulse.invitation;

import com.projectpulse.invitation.dto.InstructorInviteRequest;
import com.projectpulse.invitation.dto.InstructorInviteResponse;
import com.projectpulse.invitation.dto.StudentInviteRequest;
import com.projectpulse.invitation.dto.StudentInviteResponse;
import com.projectpulse.section.SectionRepository;
import com.projectpulse.user.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public InvitationService(InvitationRepository invitationRepository,
                             SectionRepository sectionRepository,
                             UserRepository userRepository,
                             JavaMailSender mailSender) {
        this.invitationRepository = invitationRepository;
        this.sectionRepository = sectionRepository;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    @Transactional
    public StudentInviteResponse inviteStudents(StudentInviteRequest request, Long adminId) {
        var section = sectionRepository.findById(request.sectionId())
                .orElseThrow(() -> new NoSuchElementException("Section not found"));

        var admin = userRepository.findById(adminId)
                .orElseThrow(() -> new NoSuchElementException("Admin not found"));

        String adminName = admin.getFirstName() + " " + admin.getLastName();
        String adminEmail = admin.getEmail();

        List<String> sent = new ArrayList<>();

        for (String email : request.emails()) {
            String token = UUID.randomUUID().toString();

            InvitationEntity invite = new InvitationEntity();
            invite.setEmail(email);
            invite.setRole("STUDENT");
            invite.setSection(section);
            invite.setToken(token);
            invite.setInvitedBy(admin);
            invite.setExpiresAt(LocalDateTime.now().plusDays(7));
            invitationRepository.save(invite);

            String registrationLink = baseUrl + "/register?token=" + token;
            String body = buildEmailBody(adminName, adminEmail, registrationLink, request.customMessage());

            sendEmail(email, "Welcome to The Peer Evaluation Tool - Complete Your Registration", body);
            sent.add(email);
        }

        return new StudentInviteResponse(sent.size(), sent);
    }

    @Transactional
    public InstructorInviteResponse inviteInstructors(InstructorInviteRequest request, Long adminId) {
        var admin = userRepository.findById(adminId)
                .orElseThrow(() -> new NoSuchElementException("Admin not found"));

        String adminName = admin.getFirstName() + " " + admin.getLastName();
        String adminEmail = admin.getEmail();

        List<String> sent = new ArrayList<>();

        for (String email : request.emails()) {
            String token = UUID.randomUUID().toString();

            InvitationEntity invite = new InvitationEntity();
            invite.setEmail(email);
            invite.setRole("INSTRUCTOR");
            invite.setToken(token);
            invite.setInvitedBy(admin);
            invite.setExpiresAt(LocalDateTime.now().plusDays(7));
            invitationRepository.save(invite);

            String registrationLink = baseUrl + "/register?token=" + token;
            String body = buildEmailBody(adminName, adminEmail, registrationLink, request.customMessage());

            sendEmail(email, "Welcome to The Peer Evaluation Tool - Complete Your Registration", body);
            sent.add(email);
        }

        return new InstructorInviteResponse(sent.size(), sent);
    }

    private String buildEmailBody(String adminName, String adminEmail,
                                  String registrationLink, String customMessage) {
        if (customMessage != null && !customMessage.isBlank()) {
            return customMessage;
        }
        return "Hello,\n\n" +
               adminName + " has invited you to join The Peer Evaluation Tool. " +
               "To complete your registration, please use the link below:\n\n" +
               registrationLink + "\n\n" +
               "If you have any questions or need assistance, feel free to contact " +
               adminEmail + " or our team directly.\n\n" +
               "Please note: This email is not monitored, so do not reply directly to this message.\n\n" +
               "Best regards,\n" +
               "Peer Evaluation Tool Team";
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email to " + to + ": " + e.getMessage(), e);
        }
    }
}
