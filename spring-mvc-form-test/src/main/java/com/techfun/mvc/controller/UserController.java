package com.techfun.mvc.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.techfun.mvc.model.User;
import com.techfun.mvc.service.UserService;
import com.techfun.mvc.validator.UserFormValidator;

//import javax.validation.Valid;

//http://www.tikalk.com/redirectattributes-new-feature-spring-mvc-31/
//https://en.wikipedia.org/wiki/Post/Redirect/Get
//http://www.oschina.net/translate/spring-mvc-flash-attribute-example
@Controller
public class UserController {

	private final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	UserFormValidator userFormValidator;

	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(userFormValidator);
	}

	private UserService userService;

	@Autowired
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView index() {
		logger.debug("index()");
		return new ModelAndView("redirect:/users");
	}

	// list page
	@RequestMapping(value = "/users", method = RequestMethod.GET)
	public ModelAndView showAllUsers() {

		ModelAndView mav = new ModelAndView("users/list");

		List<User> users = userService.findAll();

		mav.addObject("users", users);

		return mav;

	}

	// save or update user
	@RequestMapping(value = "/users", method = RequestMethod.POST)
	public ModelAndView saveOrUpdateUser(@ModelAttribute("userForm") @Validated User user, BindingResult result,
			Model model, final RedirectAttributes redirectAttributes) {

		logger.debug("saveOrUpdateUser() : {}", user);
		ModelAndView mav = new ModelAndView();
		if (result.hasErrors()) {
			mav = new ModelAndView("users/userform");
			populateDefaultModel(mav);
			return mav;
		} else {

			redirectAttributes.addFlashAttribute("css", "success");
			if (user.isNew()) {
				redirectAttributes.addFlashAttribute("msg", "User added successfully!");
			} else {
				redirectAttributes.addFlashAttribute("msg", "User updated successfully!");
			}

			userService.saveOrUpdate(user);

			// POST/REDIRECT/GET
			mav = new ModelAndView("redirect:/users/" + user.getId());
			return mav;

			// POST/FORWARD/GET
			// return "user/list";

		}

	}

	// show add user form
	@RequestMapping(value = "/users/add", method = RequestMethod.GET)
	public ModelAndView showAddUserForm() {

		logger.debug("showAddUserForm()");

		User user = new User();

		// set default value
		user.setName("mkyong123");
		user.setEmail("test@gmail.com");
		user.setAddress("abc 88");
		// user.setPassword("123");
		// user.setConfirmPassword("123");
		user.setNewsletter(true);
		user.setSex("M");
		user.setFramework(new ArrayList<String>(Arrays.asList("Spring MVC", "GWT")));
		user.setSkill(new ArrayList<String>(Arrays.asList("Spring", "Grails", "Groovy")));
		user.setCountry("SG");
		user.setNumber(2);

		ModelAndView mav = new ModelAndView("users/userform");
		mav.addObject("userForm", user);

		populateDefaultModel(mav);

		return mav;

	}

	// show update form
	@RequestMapping(value = "/users/{id}/update", method = RequestMethod.GET)
	public ModelAndView showUpdateUserForm(@PathVariable("id") int id) {

		logger.debug("showUpdateUserForm() : {}", id);

		User user = userService.findById(id);

		ModelAndView mav = new ModelAndView("users/userform");
		mav.addObject("userForm", user);

		populateDefaultModel(mav);
		return mav;
	}

	// delete user
	@RequestMapping(value = "/users/{id}/delete", method = RequestMethod.POST)
	public ModelAndView deleteUser(@PathVariable("id") int id, final RedirectAttributes redirectAttributes) {

		logger.debug("deleteUser() : {}", id);

		userService.delete(id);

		redirectAttributes.addFlashAttribute("css", "success");
		redirectAttributes.addFlashAttribute("msg", "User is deleted!");

		return new ModelAndView("redirect:/users");

	}

	// show user
	@RequestMapping(value = "/users/{id}", method = RequestMethod.GET)
	public ModelAndView showUser(@PathVariable("id") int id) {

		logger.debug("showUser() id: {}", id);

		ModelAndView mav = new ModelAndView();
		User user = userService.findById(id);
		if (user == null) {
			mav.addObject("css", "danger");
			mav.addObject("msg", "User not found");
		}
		mav.addObject("user", user);

		mav = new ModelAndView("users/show");
		return mav;

	}

	private void populateDefaultModel(ModelAndView mav) {

		List<String> frameworksList = new ArrayList<String>();
		frameworksList.add("Spring MVC");
		frameworksList.add("Struts 2");
		frameworksList.add("JSF 2");
		frameworksList.add("GWT");
		frameworksList.add("Play");
		frameworksList.add("Apache Wicket");
		mav.addObject("frameworkList", frameworksList);

		Map<String, String> skill = new LinkedHashMap<String, String>();
		skill.put("Hibernate", "Hibernate");
		skill.put("Spring", "Spring");
		skill.put("Struts", "Struts");
		skill.put("Groovy", "Groovy");
		skill.put("Grails", "Grails");
		mav.addObject("javaSkillList", skill);

		List<Integer> numbers = new ArrayList<Integer>();
		numbers.add(1);
		numbers.add(2);
		numbers.add(3);
		numbers.add(4);
		numbers.add(5);
		mav.addObject("numberList", numbers);

		Map<String, String> country = new LinkedHashMap<String, String>();
		country.put("US", "United Stated");
		country.put("CN", "China");
		country.put("SG", "Singapore");
		country.put("MY", "Malaysia");
		mav.addObject("countryList", country);

	}

	@ExceptionHandler(EmptyResultDataAccessException.class)
	public ModelAndView handleEmptyData(HttpServletRequest req, Exception ex) {

		logger.debug("handleEmptyData()");
		logger.error("Request: {}, error ", req.getRequestURL(), ex);

		ModelAndView model = new ModelAndView();
		model.setViewName("user/show");
		model.addObject("msg", "user not found");

		return model;

	}

}