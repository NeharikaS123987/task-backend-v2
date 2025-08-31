package com.example.taskmanager.notifications;

import com.example.taskmanager.task.TaskStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Stage 3: Reminder for tasks due in the next 24 hours.
 * Runs with fixed delay (configurable) to make dev testing fast.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DueDateReminderJob {

    @PersistenceContext
    private EntityManager em;

    private final MailService mail;

    /**
     * Poll using reminders.poll-ms (defaults to 1h in prod if not set).
     * Example dev config in application.yml:
     *   reminders:
     *     poll-ms: 5000
     */
    @Scheduled(fixedDelayString = "${reminders.poll-ms:3600000}")
    public void run() {
        LocalDate start = LocalDate.now();   // inclusive
        LocalDate end = start.plusDays(1);   // inclusive
        log.debug("Running due-date reminder (poll) for window {}..{}", start, end);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createQuery("""
            select t.id, t.title, t.dueDate,
                   b.id, b.name,
                   u.email
            from Task t
            join t.list l
            join l.board b
            join t.assignees u
            where t.status <> :done
              and t.dueDate between :start and :end
        """)
                .setParameter("done", TaskStatus.DONE)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        int sent = 0;
        Set<String> dedupe = new HashSet<>();
        for (Object[] r : rows) {
            Long taskId = (Long) r[0];
            String title = (String) r[1];
            LocalDate due = (LocalDate) r[2];
            Long boardId = (Long) r[3];
            String boardName = (String) r[4];
            String to = (String) r[5];

            String key = to + "|" + taskId;
            if (!dedupe.add(key)) continue;

            mail.sendDueSoonReminder(to, new ReminderPayload(taskId, title, due, boardId, boardName));
            sent++;
        }

        log.info("DueDateReminderJob sent {} reminder email(s).", sent);
    }
}