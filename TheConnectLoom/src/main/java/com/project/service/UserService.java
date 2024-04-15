package com.project.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.dao.ContactRepository;
import com.project.dao.UserRepository;
import com.project.entities.Contact;
import com.project.entities.User;

@Service
public class UserService {
	
	@Autowired
	UserRepository userRepository;
	@Autowired
	ContactRepository contactRepository;
	
	
	public User userRegister(User user) {
		System.out.println("userService : "+user );
		return userRepository.save(user);
	}
	
	public User findUserByEmail(String email) {
		User resultUser = userRepository.getUserByUserName(email);
		return resultUser;
	}
	
	public User addContactInUser(User user ) {
		User result = userRepository.save(user);
		return result;
	}
	
	/** get all contacts list with respective users UserID */
	public Page<Contact> getContactsList(int userId, Pageable pageable){
		Page<Contact> listContactsByUser = this.contactRepository.findContactsByUser(userId, pageable);
		return listContactsByUser;
	}
	
	/** getting respective contact details */
	public Contact getContactDetail(int cId) {
		Optional<Contact> optionalContact =  this.contactRepository.findById(cId);
		Contact contact = optionalContact.get();
		return contact;
	}
	
	/** find contact info by using user ID */
	public Contact getContactById(int cId) {
		Optional<Contact> optionalContact = this.contactRepository.findById(cId);
		Contact contact = optionalContact.get();
		return contact;
	}
	
	public Contact updateContactInUser(Contact contact) {
		Contact saveContact = this.contactRepository.save(contact);
		return saveContact;
	}
	
	public void deleteContact(int cId) {
		this.contactRepository.deleteByIdQuery(cId);
	}

}
