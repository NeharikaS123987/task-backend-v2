package com.example.taskmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // for DueDateReminderJob
public class TaskmanagerApplication {
	public static void main(String[] args) {
		SpringApplication.run(TaskmanagerApplication.class, args);
	}
}