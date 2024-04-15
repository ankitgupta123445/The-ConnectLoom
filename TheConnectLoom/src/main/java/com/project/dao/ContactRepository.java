 package com.project.dao;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.entities.Contact;
import com.project.entities.User;


public interface ContactRepository extends JpaRepository<Contact, Integer> {
	
	
	@Query("from Contact as c where c.user.id =:userId")
	public Page<Contact> findContactsByUser(@Param("userId") int userId,Pageable pageable);
	
	@Modifying
	@Transactional
	@Query(value="delete from Contact c where c.cId = ?1")
	void deleteByIdQuery(Integer cId);
	
	public List<Contact> findByNameContainingAndUser(String namekeyword, User user);
}
