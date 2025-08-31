package com.example.taskmanager.hello;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple health/sanity check endpoint.
 * You can curl http://localhost:8080/api/hello and expect a static message.
 *
 * Useful for testing that the app boots and routes correctly.
 */
@RestController
public class HelloController {

    @GetMapping("/api/hello")
    public String hello() {
        return "Task Manager backend is up and running 🚀";
    }
}