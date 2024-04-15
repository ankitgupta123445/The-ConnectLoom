package com.project.controller;


import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.entities.User;
import com.project.helper.Message;
import com.project.service.UserService;

@Controller
public class HomeController {
	
	@Autowired
	private UserService userService;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@GetMapping(value={"/", "/home"})
	public String home(Model model) {
		model.addAttribute("title", " Home : The ConnectLoom");
		return "home";
	}

	@GetMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", " About : The ConnectLoom");
		return "about";
		
	}
	
	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", " Register : The ConnectLoom");
		model.addAttribute("user",new User());
		return "signup";
	}
	
	@PostMapping("/register")
	public String registerUser(@Valid @ModelAttribute("user") User user, @RequestParam(value = "agreement",defaultValue = "false") boolean agreement,Model model,BindingResult result1,HttpSession session ) {
		
		try {
			if(!agreement) {
				System.out.println("You have not agreed the terms and conditions");
				throw new Exception("\n You have not agreed the terms and conditions");
			}
			
			if(result1.hasErrors()) {
				System.out.println("ERROR "+result1.toString());
				model.addAttribute("user",user);
				return "signup";
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println("Agreement "+agreement);
			System.out.println("User "+user);
			
			User result = this.userService.addContactInUser(user);
			
			model.addAttribute("user",new User());
			model.addAttribute("message", new Message("Successfully Registered!! ", "alert-success"));

			//session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));
			
			return "signup";
			
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			model.addAttribute("message", new Message("Something went wrong !! "+e.getMessage(), "alert-danger"));

			//session.setAttribute("message", new Message("Something went wrong !!"+e.getMessage(), "alert-danger"));
			
			return "signup";
		}
	}
	
	
	@GetMapping("/signin")
	public String login(Model model) {
		model.addAttribute("title", "Login : The ConnectLoom");
		return "login";
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}
