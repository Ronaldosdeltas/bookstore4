package com.apicrud3.bookstore4.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Welcome to the Bookstore!";
    }
    @GetMapping("/admin/home")
    public String getAdminHome() {
        return "Welcome to the Admin Home!";
    }
    @GetMapping("/user/home")
    public String getUserHome() {
        return "Welcome to the User Home!";
    }
}
