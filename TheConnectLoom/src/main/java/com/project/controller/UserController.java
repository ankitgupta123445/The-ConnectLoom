package com.project.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.project.entities.Contact;
import com.project.entities.User;
import com.project.helper.Message;
import com.project.service.UserService;

@Controller
@Transactional
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	UserService userService;
	
	//@Autowired
	//private UserRepository userRepository;
	
	//@Autowired
	//private ContactRepository contactRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	
	/*
	 * Common data retrieved for all handler methods, it runs for every handler like for "/index"or "/add-contact"
	 * */
	@ModelAttribute
	public void AddCommonData(Model model,Principal principal) {
		String userName = principal.getName();
		System.out.println("Logged In Username :"+userName);
		User currentLogInUserDetails = userService.findUserByEmail(userName);
		System.out.println(currentLogInUserDetails);
		model.addAttribute("user", currentLogInUserDetails);
		
	}
	
	@GetMapping("/index")
	public String dashboard(Model model,Principal principal) {
		model.addAttribute("title", "User Dashboard");
		return "user/user_dashboard";
	}
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add contact : The ConnectLoom");
		model.addAttribute("contact",new Contact());	
		return "user/add_contact_form";
	}
	
	/**
	 *Add contact-form-data processing to store in
	 *respective users account
	 **/
	@PostMapping("/process-contact")
	public String processAddContact(@Valid @ModelAttribute Contact contact, BindingResult result ,@RequestParam("profileImage") MultipartFile mpFile,Principal principal, Model model) {
		
		User currentLogInUserDetails = this.userService.findUserByEmail(principal.getName());
		Path destPath = null;
		String originalFilename = null;
		
		 /** making image name unique*/
		String currDateTime= (LocalDateTime.now()+"").replace(":", "-");
		
		/**
		 * Here we added contact to respective user list to get list of contact using method
		 * First we retrieve current user
		 * next add current user into his contact fields
		 * next add this contact info retrieved from form data into users contact List
		 * send this updated contact form data to user contact-list
		 * */
		
		try {
		/**
		 * Setting explicitly retrieving image data using @RequestParam, first save image into /resource/static/image folder then  
		 * save this image unique name into database as a Url string 
		 * */
			
			if(mpFile.isEmpty()) {
				System.out.println("file is empty");
			//	throw new Exception("Image file must not selected..!!");
				originalFilename = "contact_profile.png";
			}else {
				originalFilename = currDateTime+"@"+mpFile.getOriginalFilename();
			}	
				/** retrieve current class-path resource folder relative path */
				 File savedFile = new ClassPathResource("/static/image").getFile();
			 
				 destPath = Paths.get(savedFile.getAbsolutePath().replace("target\\classes", "src\\main\\resources")+File.separator+originalFilename);
				 System.out.println("Image path :"+destPath);
				 
				contact.setImage(originalFilename);
			
			/** first complete contact form setting all attributes details */
			contact.setUser(currentLogInUserDetails);
			
			/** Retrieving current users all contact list and again add current retrieved contact info into the list */
			currentLogInUserDetails.getContacts().add(contact);	
			
		/** save this updated or added contacts  user into database  */
		User addedContactResult = userService.addContactInUser(currentLogInUserDetails);
	
		
		if(addedContactResult !=null) {
			
			/** Now actual storing image into given path, when first Registered this file path location into DB then now*/
			Files.copy(mpFile.getInputStream(), destPath, StandardCopyOption.REPLACE_EXISTING);
			System.out.println("After successful contact added : "+addedContactResult);
		}
		
		/** success message alert */
		model.addAttribute("message", new Message("Contact saved successfully.....!!", "alert-success"));
		model.addAttribute("contact", new Contact());
		return "user/add_contact_form";
		
		}catch(Exception e) {
			
			System.out.println("Error : "+e);
			e.printStackTrace();
			model.addAttribute("contact", contact);

			/** failure message alert */
			model.addAttribute("message", new Message("Something went wrong !!, please try again.....!! "+e.getMessage(), "alert-danger"));
			return "user/add_contact_form";
		}
	}
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model model,Principal principal) {
		model.addAttribute("title", "Show contacts - The ConnectLoom");
		User user =this.userService.findUserByEmail(principal.getName());
		
		/** First we will get Pageable objects to store current page index and number of records to show end user 
		 * 	
		 * - Current page index is - 0, --> page
		 * - Number of contacts per page = 5 ---> 
		 * */
		Pageable pageable = PageRequest.of(page, 5);
		
		
		Page<Contact> contacts = this.userService.getContactsList(user.getId(), pageable);
		model.addAttribute("contacts",contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());

		
		return "user/show_contacts";
	}
	
	@GetMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal,HttpSession session) {
		System.out.println("CID : "+cId);
		
		String currentUser = principal.getName();
		model.addAttribute("title", "Contact details : The ConnectLoom");
		
		/** Retrieving user contact details */
		Contact contact = this.userService.getContactDetail(cId);
		
		/** checking if url miss-leading happen then check both current login-user and contact user name is same */
		if(! currentUser.equals(contact.getUser().getEmail())) 
			model.addAttribute("alertmessage", new Message("alert ...!! You are not an authorized user for this contact", "alert-danger"));
		else
			model.addAttribute("contact", contact);
		return "user/contact_detail";
	}
	
	@GetMapping("/delete-contact/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Principal principal, Model model,HttpSession session ) {
		
		/** first take current login user */
		String currentUser = principal.getName();
		
		/** take contact details by using its ID */
		Contact contact = this.userService.getContactById(cId);
		
		
		/** compare both userID for avoiding url miss-leading purpose  */
		if(currentUser.equals(contact.getUser().getEmail())) { 
			
			/**Remove contact from given user List*/
			//contact.setUser(null);
			this.userService.deleteContact(cId);
			//contactRepository.deleteByIdQuery(cId);
			session.setAttribute("message", new Message("Contact Deleted successfully", "alert-success"));
			
			
		}else {
			session.setAttribute("message", new Message("You are not an authorized user for this contact", "alert-danger"));
		}
		return "redirect:/user/show-contacts/0";
	}
	
	@GetMapping("/update-contact/{cid}")
	public String updateContact(@PathVariable("cid") Integer cid,Model model) {
		model.addAttribute("title", "Update Contact : The ConnectLoom");
		model.addAttribute("subTitle", "Update your Contact");
		Contact contact = this.userService.getContactById(cid);
		model.addAttribute("contact", contact);
		return "user/update_contact";
	}
	
	/** Process the update contact form  */
	@PostMapping("/process-update-contact")
	public String processUpdateContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Model model, Principal principal,HttpSession session) {
		
		 /** old contact details */
		 Contact oldContact = this.userService.getContactById(contact.getcId());
		
		try {
		
		/** we need current user details to set inside contact entity fields */
		 User currentUser = this.userService.findUserByEmail(principal.getName());
				 
		 /** now set this user into passed contact list*/
		 contact.setUser(currentUser);
		
		 /** multi-part file writer */
		 File saveFile = new ClassPathResource("/static/image").getFile();
		 
		 /** creating unique file-name for each image */
			String uniqueImageName = (LocalDateTime.now()+"").replace(":", "-")+"@"+file.getOriginalFilename();
		 
		/** If file is not empty */
		if(!file.isEmpty()) {
			
			/** means user send updated photo*/
			
			// first delete previous photo from DB and from saved folder
			if(oldContact.getImage() !=null && !oldContact.getImage().equals("contact_profile.png") ) {
				File deleteFile = new File(saveFile.getAbsolutePath().replace("target\\classes", "src\\main\\resources"), oldContact.getImage());
				deleteFile.delete();
			}
			
			// Update new image into our folder location
			Path path = Paths.get(saveFile.getAbsolutePath().replace("target\\classes", "src\\main\\resources")+File.separator+uniqueImageName);
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			/** now copied file name must be save with same name inside DATABASE */
			contact.setImage(uniqueImageName);
			
		}else {
			
			
			/** mean user not update photo, then set it's previous/old photo*/
			if(oldContact.getImage() !=null)
				contact.setImage(oldContact.getImage());
			else
				contact.setImage("contact_profile.png");
			
		}
			
		
			/** here contact is saved if exist then update otherwise it saved as new contact */
			Contact updatedContact = this.userService.updateContactInUser(contact);
			session.setAttribute("message", new Message("Contact successfully updated", "alert-success"));
		} catch (Exception e) {
			model.addAttribute("message", new Message("Contact updation failed ", "alert-danger"));
			e.printStackTrace();
			session.setAttribute("contact", oldContact);
			return "redirect:/user/update-contact/"+contact.getcId();
		}
		
		
		System.out.println("contact name : "+contact.getName());
		System.out.println("contact ID : "+contact.getcId());
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	/** user profile hander */
	@GetMapping("/profile")
	public String showUserProfile(Model model) {
		model.addAttribute("title", "User Profile : The ConnectLoom");
		return "user/profile";
	}
	
	@GetMapping("/setting")
	public String  openSetting() {
		return "user/setting";
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword, Principal principal,Model model,HttpSession session) {
		System.out.println("Old Password : " + oldPassword);
		System.out.println("New Password : " + newPassword);
		User user = this.userService.findUserByEmail(principal.getName());
		String existingPassword = user.getPassword();
		System.out.println(existingPassword);
		if (passwordEncoder.matches(oldPassword, existingPassword)) {
			user.setPassword(passwordEncoder.encode(newPassword));
			//this.userRepository.save(user);
			this.userService.addContactInUser(user);
			session.setAttribute("message", new Message("Password is updated successfully...", "alert-success"));
		} else {
			session.setAttribute("message", new Message("Wrong old password !!!", "alert-danger"));
			return "redirect:/user/setting";
		}
		return "redirect:/user/index";
	}
	
	@GetMapping("/update-profile")
	public String updateProfileHandler(Model model) {
		model.addAttribute("title", "Edit Profile");
		return "user/update_profile_form";
	}
	
	@PostMapping("/process-update-profile")
	public String processEditProfileForm(@RequestParam("name") String name, @RequestParam("about") String about, @RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) throws IOException  {
		
		System.out.println(name);
		System.out.println(about);
		if(!file.isEmpty()) {
			System.out.println(file.getOriginalFilename());
		}
		
		String userName = principal.getName();
		User user = this.userService.findUserByEmail(userName);
		
		user.setName(name);
		user.setAbout(about);
		
		if(!file.isEmpty()) {
			
			
			String uniqueImageName = (LocalDateTime.now()+"").replace(":", "-")+"@"+file.getOriginalFilename();
				user.setImageUrl(uniqueImageName);
				
				File saveFile = new ClassPathResource("/static/image").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath().replace("target\\classes", "src\\main\\resources")+File.separator+uniqueImageName);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
			
			
		}
		
		this.userService.addContactInUser(user);
		session.setAttribute("message", new Message("Profile updated successfully ...", "alert-success"));
		return "redirect:/user/profile";
	}

	
	
	
	
	
	
	
	
	
}
