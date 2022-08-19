package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

@Controller
public class ForgotPasswordController {
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	Random random = new Random(1000);
	
	//Handler to opne forgot password form
	@RequestMapping("/forgot")
	public String forgotPasswordForm(Model model) {
		model.addAttribute("title", "Forgot Password");		
		return "forgot_form";
	}
	
	//Handler for sending OTP
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email, HttpSession session){
		try {
			System.out.println("Email: "+email);			
			
			//check given email exist in db or not
			User user = this.userRepository.getUserByUserName(email);
			
			if(user == null) {
				throw new Exception("Email entered is incorrect. Please enter correct email !!");
			}
			
			//generate otp
			int otp = random.nextInt(9999);
			System.out.println("Generated OTP: "+otp);
			//store otp and send it to email 
			
			String Subject = "OTP From SCM";
			String Message1 = "OTP: "+otp;
			String toEmail = email;
			
			
			boolean flag = this.emailService.sendEmail(toEmail, Subject, Message1);

			if(flag){
				session.setAttribute("oldOTP", otp);
				session.setAttribute("email", email);
				session.setAttribute("message", new Message("OTP sent to email. Please check your email !!", "alert-success"));
				return "verify_otp";
			}else{
				throw new Exception("Mail sending failed due internal server issue.. Please try again later !!");
			}

		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message(""+e.getMessage(), "alert-danger"));
			//session.setAttribute("message", "Something went wrong !! " +e.getMessage());
			return "forgot_form";
		}
	}
	
	//Handler for Verify OTP
	@PostMapping("/verify-otp")
	public String verifyOTP(@RequestParam("otp") int otp, HttpSession session) {
		try {
			//get otp from session
			int oldOTP = (int)session.getAttribute("oldOTP");
			String email = (String)session.getAttribute("email");
			
			if(oldOTP == otp) {
				//allow change password
				return "change_password_form";
			}else {
				//error message
				//session.setAttribute("message", "Entered OTP is wrong !!");
				session.setAttribute("message", new Message("Entered OTP is wrong !! ", "alert-danger"));
				return "verify_otp";
			}
		} catch (Exception e) {
			session.setAttribute("message", "Something wentwrong !! "+e.getMessage());
			return "verify_otp";
		}
	}
	
	//Handler for changePassword-process 
	@PostMapping("/changePassword-process")
	public String changePassword(@RequestParam("newPassword") String newPassword, HttpSession session) {
		try {
			//get user from session
			String email = (String)session.getAttribute("email");
			User user = this.userRepository.getUserByUserName(email);
			//set new password
			user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			//save user
			this.userRepository.save(user);
			session.setAttribute("message", new Message("Password changed successfully !! Try Logging In..", "alert-success"));
			return "redirect:/signin";
		} catch (Exception e) {
			session.setAttribute("message", new Message("Something went wrong !!", "alert-danger"));
			return "verify_otp";
		}
	}
}
