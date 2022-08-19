package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;
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

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@ModelAttribute
	public void addCommonData(Model model, Principal principle){
		String userName = principle.getName();
		System.out.println("USER : "+userName);
		//get user details from databse
		User user = this.userRepository.getUserByUserName(userName);
		System.out.println("USER Details: "+user.toString());
		model.addAttribute("user", user);
	}
	
	//User dashboard handler
	@RequestMapping("/index")
	public String dashboard(Model model) {
		model.addAttribute("title",	"User Dashboard");
		return "normal/user_dashboard";
	}
	
	//Add contact form handler
	@GetMapping("/add-contact")
	public String addContactForm(Model model){
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	@PostMapping("/process-contact")
	public String saveContact(@Valid @ModelAttribute("contact") Contact contact, BindingResult result,
			@RequestParam("profileImage") MultipartFile file, Principal principal, Model model, HttpSession session) {

		try {

			/*
			 * String pNumber = contact.getPhone(); if(pNumber.length() > 10) { throw new
			 * Exception("Mobile Number must be of 10 digits long !!"); }
			 */

			if (result.hasErrors()) {
				System.out.println("ERROR: " + result.toString());
				model.addAttribute("contact", contact);
				return "normal/add_contact_form";
			}

			System.out.println("Contact Add started....");
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			// add contact to user
			user.getContacts().add(contact);
			// set user in contact
			contact.setUser(user);

			// file upload
			if (file.isEmpty()) {
				System.out.println("File is not selected !!");
				contact.setImage("contact.png");
			} else {
				// set filename to contact table
				contact.setImage(file.getOriginalFilename());
				// savefile
				File saveFile = new ClassPathResource("static/img").getFile();
				// get exact path of the file
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("File copied successfully !!");

			}

			User result1 = this.userRepository.save(user);
			model.addAttribute("contact", new Contact());
			session.setAttribute("message", new Message("Contact added successfully !!", "alert-success"));
			return "normal/add_contact_form";
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message",
					new Message("Something went wrong !! Try again after sometime.." + e.getMessage(), "alert-danger"));
			return "normal/add_contact_form";
		}
	}

	
	@GetMapping("/show-contacts/{page}")
	//current page = page
	//per page = 5
	public String showContacts(@PathVariable("page") int page, Model model, Principal principal){
		model.addAttribute("title", "View Contacts");
		//get loggedin user
		String userName = principal.getName();
		//from userName get user details
		User user = this.userRepository.getUserByUserName(userName);
		
		//current page=page
		//per page = 5
		Pageable pageable = PageRequest.of(page, 5);
		
		//get all contacts by loggedin user
		Page<Contact> contacts =  this.contactRepository.findContactsByUserId(user.getId(), pageable);
		//send contacts to form to show
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	//handler for getting specific contact details
	@GetMapping("/{id}/contact")
	public String showContactDetail(@PathVariable("id") Integer cId, Model model, Principal principal){
		System.out.println("CID: "+cId);
		
		//get contact details by contact ID
		Optional<Contact> optionalContact = this.contactRepository.findById(cId);
		Contact contact = optionalContact.get();
		System.out.println("Contact: "+contact.toString());
		
		//get logged in user
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId() == contact.getUser().getId()) {
			model.addAttribute("title", "Contact Details");
			model.addAttribute("contact", contact);
		}
		
		return "normal/contact-details";
	}
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId, Model model, Principal principal, HttpSession session) {
		try {
			System.out.println("Deleting CID: "+cId);
			
			Contact contact = this.contactRepository.findById(cId).get();
			
			String userName = principal.getName();
			User user = this.userRepository.getUserByUserName(userName);
			if(user.getId() == contact.getUser().getId()) {
				//delete file from folder
				String fileName = contact.getImage();
				
				System.out.println("File Name: "+fileName);
				
				File saveFile = new ClassPathResource("static/img").getFile();
				// get exact path of the file
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + contact.getImage());
				System.out.println(path);
				String nonSelectedImage = "contact.png";
				File file1= new File(contact.getImage());
				File file2 = new File("contact.png");
				
				if(file1.equals(file2)) {
					//Files.delete(path);	
					System.out.println("Profile image file is not deleted as user has not selected it..");
					user.getContacts().remove(contact);
					this.userRepository.save(user);
				}else {
					Files.delete(path);	
					System.out.println("Profile image is also deleted successfully..");
					user.getContacts().remove(contact);
					this.userRepository.save(user);
				}	
			}else {
				throw new Exception("You dont have permission to delete this contact !!");
			}
			session.setAttribute("message", new Message(contact.getName()+" Contact deleted successfully !!","alert-success"));
			return "redirect:/user/show-contacts/0";
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong !!"+e.getMessage(), "alert-danger"));
			return "redirect:/user/show-contacts/0";
		}	
	}
	
	//Handler for opening update contact details screen
	@PostMapping("/update-contact/{cid}")
	public String updateContactDetails(@PathVariable("cid") Integer cId, Model model){
		model.addAttribute("title", "Update Contact");
		
		//get contact by cId
		Contact contact = this.contactRepository.findById(cId).get();
		System.out.println("Contact to be updated: "+contact);
		model.addAttribute("contact", contact);
		return "normal/update_contact";
	}
	
	//Handler for updating contact details
	@PostMapping("/process-update")
	public String updateContact(@ModelAttribute("contact") Contact contact, @RequestParam("profileImage") MultipartFile file, Principal principal, Model model, HttpSession session){
		try {
			System.out.println("Contact Name: "+contact.getName());
			System.out.println("Contact ID: "+contact.getcId());
			//get old contact image before update
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
			if(!file.isEmpty()) {
				//delete old image
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile, oldContactDetail.getImage());
				file1.delete();
				System.out.println("Old image file deleted successfully !!");
				
				//upload new image
				File saveFile = new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				
				System.out.println("New image uploaded successyully !!");
			}else {
				//set old image as it is 
				contact.setImage(oldContactDetail.getImage());
			}
			//set user to contact before update
			String userName = principal.getName();
			User user = this.userRepository.getUserByUserName(userName);
			contact.setUser(user);
			
			//save contact
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Contact Update successfully !!", "alert-success"));
			
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong !! Try after sometime..", "alert-danger"));
		}
		return "redirect:/user/"+contact.getcId()+"/contact";		
	}
	
	//Handler for Your Profile
	@GetMapping("/view-profile")
	public String viewProfile(Model model) {
		model.addAttribute("title","Profile Page");
		return "normal/view_profile";
	}
	
	//Handler for showing update screen for Logged in user
	@PostMapping("/user-profile-update/{id}")
	public String updateUserDetails(@PathVariable("id") Integer id, Model model, HttpSession session){
		try {
			model.addAttribute("title", "Update User Details");
			//get user details by user ID
			User user = this.userRepository.findById(id).get();
			System.out.println("Logged In User Details: "+user);
			return "/normal/update-profile";
			
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong !! Try again after sometime.."+e.getMessage(), "alert-danger"));
			return "/normal/view-profile";
		}
	}
	
	//Handler for updating User details
	@PostMapping("/updateUserDetail-process")
	public String updateUserDetailProcess(@ModelAttribute("user") User user, Model model, @RequestParam("userProfile") MultipartFile file, HttpSession session){
		try {
			System.out.println("Updated User Details: "+user);
			//get old image
			User oldUserDetails = this.userRepository.findById(user.getId()).get();
			System.out.println("Old User Details123: "+oldUserDetails);
			
			if(!file.isEmpty()) {
				//delete old image
				File deleteFile = new ClassPathResource("static/img/").getFile();
				File file1 = new File(deleteFile, oldUserDetails.getImageUrl());
				file1.delete();
				System.out.println("Old image deleted successfully !!");
				
				//upload new image
				File saveFile = new ClassPathResource("static/img/").getFile();
				Path path= Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				user.setImageUrl(file.getOriginalFilename());
				System.out.println("Profile Image successfully changed !!");
				
			}else {
				user.setImageUrl(oldUserDetails.getImageUrl());
			}
				this.userRepository.save(user);
				System.out.println("Details updated successfully !!");
				//session.setAttribute("message", new Message("Contact Update successfully !!", "alert-success"));
				session.setAttribute("message", new Message("Details updated successfully !!", "alert-success"));
				return "redirect:/user/view-profile";
		} catch (Exception e) {
			session.setAttribute("message", new Message("Something went wrong !!", "alert-danger"));
			return "redirect:/user/user-profile-update/"+user.getId();
		}
		
	}

	//Handler for Setting screen
	@RequestMapping("/settings")
	public String openSettingScreen(Model model){
		model.addAttribute("title", "Settings");
		return"normal/settings";
	}
	
	//Handler for change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, Principal principal, HttpSession session){
		try {
			System.out.println("Old Password: "+oldPassword);
			System.out.println("New Password: "+newPassword);
			//get user
			User currentUser = this.userRepository.getUserByUserName(principal.getName());
			//check password enter and db password are same or not
			if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
				//Change password
				currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
				this.userRepository.save(currentUser);
				session.setAttribute("message", new Message("Password changed successfully..", "alert-success"));
			}else {
				//throw error wrong password
				throw new Exception("Please enter correct old password !!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong !! "+e.getMessage(), "alert-danger"));
			return "redirect:/user/settings";
		}
		return "redirect:/user/index";
	}
}
