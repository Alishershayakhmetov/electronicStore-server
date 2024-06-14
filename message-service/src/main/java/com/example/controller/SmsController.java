package com.example.controller;

import com.example.service.UserCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sms")
public class SmsController {

    @Autowired
    private UserCodeService userCodeService;

    @PostMapping("/send")
    public ResponseEntity<String> sendCode(@RequestParam String phoneNumber) {
        userCodeService.sendCode(phoneNumber);
        return ResponseEntity.ok("Code sent successfully.");
    }

    @PostMapping("/confirm")
    public ResponseEntity<String> confirmCode(@RequestParam String phoneNumber, @RequestParam String code) {
        boolean isValid = userCodeService.confirmCode(phoneNumber, code);
        if (isValid) {
            return ResponseEntity.ok("Code confirmed successfully.");
        } else {
            return ResponseEntity.status(400).body("Invalid or expired code.");
        }
    }
}


