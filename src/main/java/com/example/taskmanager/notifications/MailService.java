package com.example.taskmanager.notifications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Thin mail facade. If JavaMailSender is not configured,
 * we log the outgoing email so the app still works.
 *
 * To enable real email, set typical Spring Mail properties (e.g. in application.yml):
 *
 * spring:
 *   mail:
 *     host: smtp.example.com
 *     port: 587
 *     username: YOUR_USER
 *     password: YOUR_PASS
 *     properties:
 *       mail.smtp.auth: true
 *       mail.smtp.starttls.enable: true
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    /** Nullable so the app runs even without mail config. */
    @Nullable private final JavaMailSender mail;

    public void sendTaskAssigned(String to, String taskTitle, String boardName) {
        String subject = "[Task Assigned] " + taskTitle;
        String body = "You were assigned to task: " + taskTitle +
                (boardName != null ? (" (Board: " + boardName + ")") : "") + ".";
        send(to, subject, body);
    }

    public void sendDueSoonReminder(String to, ReminderPayload payload) {
        String subject = "[Task Due Soon] " + payload.taskTitle();
        String body = "Reminder: \"" + payload.taskTitle() + "\" is due on " + payload.dueDate() +
                (payload.boardName() != null ? (" (Board: " + payload.boardName() + ")") : "") +
                ".\n\nPlease review and complete it.";
        send(to, subject, body);
    }

    private void send(String to, String subject, String body) {
        if (mail == null) {
            log.info("[MAIL-DRYRUN] to={}, subject={}, body={}", to, subject, body);
            return;
        }
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        try {
            mail.send(msg);
        } catch (Exception e) {
            // Never explode the app because SMTP is flaky
            log.warn("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}