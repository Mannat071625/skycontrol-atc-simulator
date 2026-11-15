
package com.atc.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/api/status")
    public String getStatus() {
        return "Air Traffic Control System is Running!";
    }
}
