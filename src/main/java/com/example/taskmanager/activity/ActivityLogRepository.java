package com.example.taskmanager.activity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    Page<ActivityLog> findByBoardIdOrderByCreatedAtDesc(Long boardId, Pageable pageable);
    void deleteByBoardId(Long boardId);
}