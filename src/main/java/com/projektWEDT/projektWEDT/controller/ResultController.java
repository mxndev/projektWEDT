package com.projektWEDT.projektWEDT.controller;

import java.util.ArrayList;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ResultController {

	@RequestMapping("/result")
    public String greeting(@RequestParam(value="name", required=false, defaultValue="World") String name, Model model,
    		@ModelAttribute("endedString") String endedString) {
        
		
        model.addAttribute("endedString", endedString);
        
        return "result";
    }
}