package com.example.taskmanager.analytics;

import com.example.taskmanager.analytics.dto.AvgCompletionPerBoardResponse;
import com.example.taskmanager.analytics.dto.BoardTaskCountsResponse;
import com.example.taskmanager.analytics.dto.MostActiveUserResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    @PersistenceContext
    private EntityManager em;

    /** Totals of TODO / IN_PROGRESS / DONE per board. */
    @Transactional(readOnly = true)
    public List<BoardTaskCountsResponse> boardTaskCounts() {
        // JPQL with CASE; portable across Postgres/MySQL
        var q = em.createQuery("""
            select b.id, b.name,
                   sum(case when t.status = com.example.taskmanager.task.TaskStatus.TODO then 1 else 0 end),
                   sum(case when t.status = com.example.taskmanager.task.TaskStatus.IN_PROGRESS then 1 else 0 end),
                   sum(case when t.status = com.example.taskmanager.task.TaskStatus.DONE then 1 else 0 end)
            from Task t
            join t.list l
            join l.board b
            group by b.id, b.name
        """, Object[].class);

        List<Object[]> rows = q.getResultList();
        List<BoardTaskCountsResponse> out = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            out.add(new BoardTaskCountsResponse(
                    (Long) r[0], (String) r[1],
                    ((Number) r[2]).longValue(),
                    ((Number) r[3]).longValue(),
                    ((Number) r[4]).longValue()
            ));
        }
        return out;
    }

    /** Average completion time per board, computed in Java for portability. */
    @Transactional(readOnly = true)
    public List<AvgCompletionPerBoardResponse> avgCompletionPerBoard() {
        var rows = em.createQuery("""
            select b.id, b.name, t.createdAt, t.completedAt
            from Task t
            join t.list l
            join l.board b
            where t.completedAt is not null
        """, Object[].class).getResultList();

        record Acc(long count, long totalSeconds, String name) {}
        Map<Long, Acc> m = new HashMap<>();

        for (Object[] r : rows) {
            Long boardId = (Long) r[0];
            String boardName = (String) r[1];
            Instant created = (Instant) r[2];
            Instant completed = (Instant) r[3];
            long seconds = Duration.between(created, completed).getSeconds();
            var acc = m.get(boardId);
            if (acc == null) acc = new Acc(0,0, boardName);
            acc = new Acc(acc.count + 1, acc.totalSeconds + Math.max(0, seconds), boardName);
            m.put(boardId, acc);
        }

        List<AvgCompletionPerBoardResponse> out = new ArrayList<>(m.size());
        for (var e : m.entrySet()) {
            double avgHours = e.getValue().count == 0 ? 0.0
                    : (e.getValue().totalSeconds / (double) e.getValue().count) / 3600.0;
            out.add(new AvgCompletionPerBoardResponse(e.getKey(), e.getValue().name, avgHours));
        }
        // sort by slowest first (optional)
        out.sort(Comparator.comparingDouble(AvgCompletionPerBoardResponse::avgHours).reversed());
        return out;
    }

    /** Top-N users by number of activity log entries (across all boards). */
    @Transactional(readOnly = true)
    public List<MostActiveUserResponse> mostActiveUsers(int limit) {
        var q = em.createQuery("""
            select u.id, u.email, count(a)
            from ActivityLog a
            join a.actor u
            group by u.id, u.email
            order by count(a) desc
        """, Object[].class).setMaxResults(Math.max(1, Math.min(limit, 100)));

        List<Object[]> rows = q.getResultList();
        List<MostActiveUserResponse> out = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            out.add(new MostActiveUserResponse(
                    (Long) r[0], (String) r[1], ((Number) r[2]).longValue()
            ));
        }
        return out;
    }
}