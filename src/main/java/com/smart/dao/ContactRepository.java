package com.smart.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;
import com.smart.entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

	@Query("from Contact as c where c.user.id = :userId")
	//passing current page(i.e. page) and per page count(5) to Pageble
	public Page<Contact> findContactsByUserId(@Param("userId") int userId, Pageable pePageble);
	
	//search query
	public List<Contact> findByNameContainingAndUser(String name, User user);
}
