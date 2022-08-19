package com.smart.controller;

import java.text.DecimalFormat;
import java.util.Random;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passworEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private EmailService emailService;
	
	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title","Home- Smart Contact Manager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title","About- Smart Contact Manager");
		return "about";
	}
	
	@RequestMapping("/signup")
	public String test(Model model) {
		model.addAttribute("title", "Register-Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}
	
	
	//Handler for signUp-OTP
	@PostMapping("/signUp-OTP")
	public String sendSignUpOTP(@Valid @ModelAttribute("user") User user, BindingResult result1, Model model, @RequestParam(value = "agreement", defaultValue = "false") Boolean agreement, HttpSession session) {
		
		try {
			if(!agreement) {
				System.out.println("You have not agreed terms and conditions!!");
				throw new Exception("You have not agreed terms and conditions!!");
			}
			if(result1.hasErrors()) {
				System.out.println("ERROR: "+result1.toString());
				model.addAttribute("user", user);
				return "signup";
			}
			
			//Generate OTP
			String otp= new DecimalFormat("000000").format(new Random().nextInt(999999));
			System.out.println("Generated OTP: "+otp);
			
			//send otp on email
			String subject= "SCM Registration Co;nfirmation !!";
			String messasge1= "OTP: "+otp;
			String toEmail = user.getEmail();
			
			boolean flag = this.emailService.sendEmail(toEmail, subject, messasge1);
			if(flag) {
				model.addAttribute("title", "SignUp Confirmation");
				session.setAttribute("user", user);
				session.setAttribute("agreement", agreement);
				session.setAttribute("oldOTP", otp);
				session.setAttribute("message", new Message("OTP sent to your email !!", "alert-success"));
				return "signUp_cofirmation";
			}else {
				throw new Exception("Mail sending failed due internal server issue.. Please try again later !!");
			}
			
		} catch (Exception e) {
			session.setAttribute("message", new Message("Something went wrong !!"+e.getMessage(), "alert-danger"));
			return "signup";
		}
		
	}
	
	
	//register new user
	@PostMapping("/do_register")
	public String register(@RequestParam("newOTP") int newOTP, Model model, HttpSession session){
		
		try {
			/*
			 * if(!agreement) {
			 * System.out.println("You have not agreed term and conditions."); throw new
			 * Exception("You have not agreed term and conditions"); }
			 * 
			 * if(result1.hasErrors()) { System.out.println("ERROR "+result1.toString());
			 * model.addAttribute("user", user); return "signup"; }
			 */
			
			//Get user entered otp
			System.out.println("User Entered OTP: "+newOTP);
			//Get otp from session
			String oldOTP = (String)session.getAttribute("oldOTP");
			int systemOTP = Integer.parseInt(oldOTP);
			
			System.out.println("System Generated OTP: "+systemOTP);
			//get user details from session
			User user = (User) session.getAttribute("user");
			System.out.println("User Details from session: "+user);
			
			//get agreement from session
			boolean user_agreement = (boolean) session.getAttribute("agreement");
			
			//check user otp and system otp are same or not
			if(newOTP == systemOTP) {
				//Register user
				user.setRole("ROLE_USER");
				user.setEnabled(true);
				user.setPassword(passworEncoder.encode(user.getPassword()));
				System.out.println("Agreement: "+user_agreement);
				//System.out.println("User: "+user);
				User result = this.userRepository.save(user);
				
				model.addAttribute("user",new User());
				session.setAttribute("message", new Message("Successfully registered !!", "alert-success"));
				return "signup";
			}else {
				//Show error
				//throw new Exception("Incorrect OTP...Please your email again !!");
				session.setAttribute("message", new Message("Incorrect OTP...Please check your email again !!", "alert-danger"));
				return "signUp_cofirmation";
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong !! "+e.getMessage() , "alert-danger"));
			return "signup";
		}
	}

	
	//handler for custom login
	@RequestMapping("/signin")
	public String login(Model model) {
		model.addAttribute("title", "Login Page - Smart Contact App");
		return "login";
	}

}
