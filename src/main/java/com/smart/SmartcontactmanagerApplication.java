package com.smart;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.smart.service.EmailService;

@SpringBootApplication
public class SmartcontactmanagerApplication {
	
	@Autowired
	private EmailService emailService;
	
	public static void main(String[] args) {
		SpringApplication.run(SmartcontactmanagerApplication.class, args);
		
		//System.out.println("Sending Mail....");
		//String message1="Helooo";
		//String subject="Testing";
		//String to="amar.kamtam@gmail.com";
		//String from = "amar114611@gmail.com";
		
	}
	
	/*
	 * @EventListener(ApplicationReadyEvent.class) public void sendMail() {
	 * emailService.sendEmail("amar.kamtam@gmail.com", "Hello",
	 * "THis is body of the mail"); }
	 */
	 
}
