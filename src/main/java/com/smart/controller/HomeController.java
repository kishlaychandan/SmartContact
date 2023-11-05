package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository userRepository;
	
	@RequestMapping("/")
	public String home(Model m) {
		m.addAttribute("title", "smart cotact manager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model m) {
		m.addAttribute("title", "smart cotact manager");
		return "about";
	}
	
	@RequestMapping("/signup")
	public String signUp(Model m) {
		m.addAttribute("signup", "smart cotact manager");
		m.addAttribute("user", new User());
		return "signup";
	}
	
	//this is handler for register user
	
	@RequestMapping(value="/do_register",method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result1,@RequestParam(value="agreement",defaultValue="false") boolean agreement,Model m,HttpSession session) 
	{
		
		try {
			
			if(!agreement) {
				System.out.println("you have not agreered to terms and condition");
				throw new Exception("you have not agreered to terms and condition");
			}
			
			if(result1.hasErrors()) {
				System.out.println("error"+result1.toString());
				m.addAttribute("user", user);
				return "signup";
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageurl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println("Agreement "+agreement);
			System.out.println("user"+user);
			
			User result = this.userRepository.save(user);
			
			m.addAttribute("user", new User());
			session.setAttribute("message",new Message("succefully registered", "alert-success") );
			return "signup";
			
		}
		
		
		catch (Exception e) {
			e.printStackTrace();
			m.addAttribute("user", user);
			session.setAttribute("message",new Message("something went wrong"+e.getMessage(), "alert-danger") );
			return "signup";
			
		}
		

	}
	
	//handler for custom login
	@GetMapping("/signin")
	public String CustomLogin(Model m) {
		m.addAttribute("title", "login page");
		
		return "login";
	}
	
}
