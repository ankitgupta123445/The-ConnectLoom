package com.project.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.dao.UserRepository;
import com.project.entities.User;
import com.project.helper.Message;
import com.project.service.EmailService;
import com.project.service.UserService;

@Controller
public class ForgetController {
	
	
	@Autowired
	EmailService emailService;
	@Autowired
	UserService userService;
	@Autowired
	BCryptPasswordEncoder passwordEncoder;
	
	@GetMapping("/forgot")
	public String Forget() {
		return "forgot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOTPHandler(@RequestParam("email") String email, Model model, HttpSession session) {
		model.addAttribute("title", "Verify OTP");
		System.out.println("Email : " + email);
        int min = 1000; 
        int max = 9999;
        Random random =new Random();
        int otp = random.nextInt(max - min + 1) + min;
		System.out.println("OTP : " + otp);
		String from = "admin_connect@gmail.com";
		String to = email;
		String message = "<h2>Your OTP is here : <b>" + otp + "</b></h2>";
		String subject = "OTP From The ConnectLoom Application";
		boolean flag = this.emailService.sendEmail(message, subject, to, from);
		//flag =true;
		if(flag) {
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			session.setAttribute("message", new Message("OTP has been sent successfully ...", "alert-success"));
			model.addAttribute("title", "Verify OTP");
			return "verify_otp";
		}
		else {
			session.setAttribute("message", new Message("Email id is incorrect !!!","alert-danger"));
			model.addAttribute("title", "Forgot Password");
			return "forgot_email_form";
		}
		
		
	}
	
	@PostMapping("/verify-otp")
	public String verifyOTPFromEmailHandler(@RequestParam("otp") int otp, HttpSession session, Model model) {
		int myOtp = (int)session.getAttribute("myotp");
		String email = (String)session.getAttribute("email");
		if(otp == myOtp) {
			User user = this.userService.findUserByEmail(email);
			if(user == null) {
				model.addAttribute("title", "Email Verification");
				session.setAttribute("message", new Message("User doesn't exist with this email !!!","alert-danger"));
				return "forgot_email_form";
			}
			else {
				this.userService.addContactInUser(user);
				model.addAttribute("title", "Login - The ConnectLoom");
				return "password_change_form";
			}
		}
		else {
			model.addAttribute("title", "Verify OTP");
			session.setAttribute("message", new Message("You have entered wrong otp !!!", "alert-danger"));
			return "verify_otp";
		}
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newPassword") String newPassword, HttpSession session) {
		String email = (String)session.getAttribute("email");
		User user = this.userService.findUserByEmail(email);
		user.setPassword(this.passwordEncoder.encode(newPassword));
		this.userService.addContactInUser(user);
		session.setAttribute("message", new Message("Password changed successfully ...","alert-success"));
		return "login";
	}

}
