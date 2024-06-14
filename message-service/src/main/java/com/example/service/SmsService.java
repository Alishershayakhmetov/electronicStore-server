package com.example.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    @Value("${twilio.phone.number}")
    private String fromNumber;

    public void sendSms(String to, String body) {
        Message message = Message.creator(
                        new PhoneNumber(formatPhoneNumber(to)),
                        new PhoneNumber(fromNumber),
                        body)
                .create();
    }
    private String formatPhoneNumber(String to) {
        // Remove any whitespace or other non-digit characters from the phone number
        String cleanedToNumber = to.replaceAll("\\s+", "");
        if (!cleanedToNumber.startsWith("+")) {
            cleanedToNumber = "+" + cleanedToNumber;
        }
        return cleanedToNumber;
    }
}

