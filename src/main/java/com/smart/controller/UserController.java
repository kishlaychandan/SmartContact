package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.MyOrderRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.MyOrder;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import com.razorpay.*;
@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private MyOrderRepository myOrderRepository;
	
	
	
	//method for adding common data to a;;
	@ModelAttribute
	public void addCommonData(Model m,Principal principal) {
		String userName=principal.getName();
		System.out.println("USERNAME "+userName);
		//get the user using user email
		User user=userRepository.getUserByUserName(userName);
		System.out.println("user "+user);
		m.addAttribute("user", user);
	}
	
	//dashboard home
	@RequestMapping("/index")
	public String dashboard(Model m,Principal principal)
	{	
		m.addAttribute("title", "User Dasboard");
		return "normal/user_dashboard.html";
	}
	
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model m) {	
		m.addAttribute("title", "Add Contact");
		m.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file, Principal principal,HttpSession session) {
		
		try {
		
//			processing and uploading file
			if(file.isEmpty()) {
				System.out.println("file is empty");
				contact.setImage("contact.png");
			}
			else {
				contact.setImage(file.getOriginalFilename());
				File saveFile=new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image uploaded");
			}
			
			
		String name=principal.getName();
		User user =this.userRepository.getUserByUserName(name);
		
		user.getContacts().add(contact);
		contact.setUser(user);
		
		
		this.userRepository.save(user);
		
		System.out.println("Data" +contact);
		System.out.println("Added to database");
		
		
		//message added
		session.setAttribute("message",new Message("your conatct is added ... Add More !", "success") );
		}
		
		catch (Exception e) {
			System.out.println("ERROR "+e.getMessage());
			//error
			session.setAttribute("message",new Message("Something went wrong... try again !", "danger") );
		}
		return "normal/add_contact_form";
		
	}
	
	public void removeMessageFromSession() {
		try {
			
			System.out.println("removing mesaage grom sesion");
			HttpSession session = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest().getSession();
			session.removeAttribute("message");
		}catch (Exception e) { 
			// TODO: handle exception
		}
	}
	
	
	//show contact
	// per page n number
	@GetMapping("/show-contacts/{page}")
	public String showContact(@PathVariable("page") Integer page,Model m,Principal p) {
		
		m.addAttribute("title", "show user contacts");
		
		String userName=p.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		
		//current page
		//contact per page
		Pageable pageable = PageRequest.of(page, 5);
		
		Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(),pageable);
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages",contacts.getTotalPages());
	
		return "normal/show_contact";
	}
	
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId")Integer cId,Model m,Principal p) {
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		//
		String userName = p.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId())
			{m.addAttribute("contact", contact);
			m.addAttribute("title", contact.getName());
			}
		return "normal/contact_details";
	}
	
	
	//delete conatct handler
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId,Model m,HttpSession session,Principal principal) {
		
		System.out.println("cid"+cId);
		
		Contact contact = this.contactRepository.findById(cId).get();
		
//		contact.setUser(null);
		
		System.out.println("contact"+contact.getcId());
	
//		this.contactRepository.delete(contact);
		User user=this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		
		this.userRepository.save(user);
		
		
		System.out.println("deleted.......");
		
		session.setAttribute("message", new Message("contact deleted successfully", "success"));
		return "redirect:/user/show-contacts/0";
		
	}
	
	
	//open update form handler
	
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid,Model m,Principal p) {
		m.addAttribute("title", "update contact");
		Contact contact = this.contactRepository.findById(cid).get();
		m.addAttribute("contact", contact);
		
		return "normal/update_form";
	}
	
	
	//update post handler
	
	@PostMapping("/process-update")
	public String updateHandaler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model m,HttpSession session,Principal p) {
		
		try {
			
			//old contact detail
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
			
			if(!file.isEmpty()) {
		
//				delete old photo
				File deleteFile=new ClassPathResource("static/img").getFile();
				File file1= new File(deleteFile,oldContactDetail.getImage());
				file1.delete();
				
				
				
				//update new phonto
				File saveFile=new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}
			
			else {
				contact.setImage(oldContactDetail.getImage());
			}
			
			User user=this.userRepository.getUserByUserName(p.getName());
			
			contact.setUser(user);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.contactRepository.save(contact);
		session.setAttribute("message", new Message("your contact is updated", "success"));
		System.out.println("contact" + contact.getName());
		System.out.println("contact id :" + contact.getcId());
		return "redirect:/user/"+contact.getcId()+"/contact";
	}

	//You profile handler
	
	@GetMapping("/profile")
	public String yourProfile(Model m) {
		m.addAttribute("title", "profile page");
		return "normal/profile";
	}
	
	//creating order for payment
	
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data,Principal principal) throws Exception {
		System.out.println("order function executed");
		System.out.println(data);
		int amt=Integer.parseInt(data.get("amount").toString());
		
		var client=new RazorpayClient("rzp_test_w14dnADMuDa97D","XyFy5SkE6ZnfscRHW7c2CLNc");
		
		JSONObject ob=new JSONObject();
		ob.put("amount", amt*100);
		ob.put("currency", "INR");
		ob.put("receipt","txn_235425");
		//creating new order
		
		Order order = client.Orders.create(ob);
		System.out.println(order);
		//save the order in database
		
		MyOrder myOrder = new MyOrder();
		myOrder.setAmount(order.get("amount")+"");
		myOrder.setOrderId(order.get("id"));
		myOrder.setPaymentId(null);
		myOrder.setStatus("created");
		myOrder.setUser(this.userRepository.getUserByUserName(principal.getName()));
		myOrder.setReceipt(order.get("receipt"));
		
		this.myOrderRepository.save(myOrder);
		
		return order.toString();
	}
	
	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data){
		
		MyOrder myOrder= this.myOrderRepository.findByOrderId(data.get("order_id").toString());
		myOrder.setPaymentId(data.get("payment_id").toString());
		myOrder.setStatus(data.get("status").toString());
		this.myOrderRepository.save(myOrder);
		
		System.out.println(data);
		return ResponseEntity.ok(Map.of("msg","updated"));
	}

	
}
