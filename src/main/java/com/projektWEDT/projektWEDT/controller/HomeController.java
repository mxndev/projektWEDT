package com.projektWEDT.projektWEDT.controller;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class HomeController {

	@RequestMapping("/")
	public String greeting(@RequestParam(value = "name", required = false, defaultValue = "World") String name,
			Model model) {
		model.addAttribute("name", name);
		return "index";
	}

	@PostMapping("/upload")
	public String retrieveFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {

		if (file.isEmpty()) {
			System.out.println("File is empty!");
			redirectAttributes.addFlashAttribute("error", "true");
			return "redirect:/";
		}

		try {

			// Get the file and save it somewhere
			byte[] bytes = file.getBytes();
			System.out.println("File is uploaded well: " + file.getOriginalFilename());
			redirectAttributes.addAttribute("error", "false");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "redirect:/";
	}
}
