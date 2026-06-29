package com.marketplace.notification;

public interface EmailService {

    void send(String to, String subject, String body);
}
