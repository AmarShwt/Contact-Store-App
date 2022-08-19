package com.smart.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Service
public class EmailService {
	
	@Autowired
	private JavaMailSender mailSender;
	
	/*
	 * public void sendEmail(String toEmail, String subject, String message1) {
	 * 
	 * SimpleMailMessage message = new SimpleMailMessage();
	 * message.setFrom("amarcabcs@gmail.com"); message.setTo(toEmail);
	 * message.setText(message1); message.setSubject(subject);
	 * 
	 * mailSender.send(message);
	 * 
	 * System.out.println("Mail sent successfully...");
	 * 
	 * }
	 */
	
	public boolean sendEmail(String toEmail, String subject, String message1) {
		boolean f = false;
		try {
			System.out.println("Mail sent successfully...1:" + f);
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom("amarcabcs@gmail.com");
			message.setTo(toEmail);
			message.setText(message1);
			message.setSubject(subject);

			mailSender.send(message);

			System.out.println("Mail sent successfully...2:" + f);

			f = true;
			System.out.println("Mail sent successfully...3:" + f);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return f;
	}
	 
	
	
	

	/*
	 * public boolean sendEmail(String subject, String message1, String to) {
	 * 
	 * boolean f = false; //from String from = "amar114611@gmail.com"; //variable
	 * for gmail //String host= "smtp.gmail.com";
	 * 
	 * //get system properties Properties properties = System.getProperties();
	 * System.out.println("System Properties: "+properties);
	 * 
	 * //host set properties.put("mail.smtp.host", "smtp.gmail.com");
	 * properties.put("mail.smtp.port", "465");
	 * properties.put("mail.smtp.ssl.enable", "true");
	 * properties.put("mail.smtp.auth", "true");
	 * 
	 * //Step:1 to get session object Session session =
	 * Session.getInstance(properties, new Authenticator() {
	 * 
	 * @Override protected PasswordAuthentication getPasswordAuthentication() {
	 * return new PasswordAuthentication(from, "Amar@1234554321"); } });
	 * 
	 * session.setDebug(true);
	 * 
	 * try { //Setp:2 compose message MimeMessage message = new
	 * MimeMessage(session); //from email message.setFrom(from); //adding receipient
	 * message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
	 * 
	 * //adding subject to message message.setSubject(subject); //adding text to
	 * message message.setText(message1);
	 * 
	 * //Step:3 send email using Transport class
	 * 
	 * Transport.send(message);
	 * 
	 * System.out.println("Mail sent successfully");
	 * 
	 * f = true;
	 * 
	 * } catch (Exception e) { e.printStackTrace(); }
	 * 
	 * return f; }
	 */
}
