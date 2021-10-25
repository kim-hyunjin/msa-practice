package com.example.userservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class UsersController {

    @Value("${greeting.message}")
    private String message;

    @GetMapping("/health-check")
    public String status() { return "It's working"; }

    @GetMapping("/welcome")
    public String welcome() {
        return message;
    }
}
