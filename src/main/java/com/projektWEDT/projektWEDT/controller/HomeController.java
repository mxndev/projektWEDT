package com.projektWEDT.projektWEDT.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {
	
	PDFParser parser = null;
    String text = "";
    PDFTextStripper stripper = null;
    PDDocument pdoc = null;
    COSDocument cdoc = null;
    RandomAccess scratchFile = null;

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
			System.out.println("File is uploaded well: " + file.getOriginalFilename());
			redirectAttributes.addFlashAttribute("error", "false");
			File pdfFile = convertFile(file);
			
			
			pdoc = PDDocument.load(pdfFile);
			stripper = new PDFTextStripper();
			text = stripper.getText(pdoc);
			System.out.println(text);
			pdoc.close();
			
			ArrayList<String> listOfParagrafs = new ArrayList<>();
			String endedString = "";
			
			listOfParagrafs = divideText(text);
			listOfParagrafs = filterTitles(listOfParagrafs);
			listOfParagrafs = connectLines(listOfParagrafs);
			listOfParagrafs = filterLines(listOfParagrafs);
			endedString = endingFormatting(listOfParagrafs);
			
			System.out.println(endedString);
			
			redirectAttributes.addFlashAttribute("endedString", endedString);
			
		//	parser = new PDFParser(new FileInputStream(pdfFile));
			
			//parser.parse();
	      //  cdoc = parser.getDocument();
	      //  stripper = new PDFTextStripper();
	      //  pdoc = new PDDocument(cdoc);
	      //  text = stripper.getText(pdoc);
	        
			

		} catch (IOException e) {
			e.printStackTrace();
		}
		return "redirect:/result";
	}

	private File convertFile(MultipartFile file) throws IOException {

		File convFile = new File(file.getOriginalFilename());
		convFile.createNewFile();
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		fos.close();
		return convFile;

	}
	
	/**
	 * Bierze tekst wprost z pdf i wyszukuje części pooddzielanych pustymi liniami
	 * 
	 * @param text
	 * @return
	 */
	private ArrayList<String> divideText(String text){
		
		ArrayList<String> partedString = new ArrayList<>();
		
		String[] lines = text.split(System.getProperty("line.separator"));
		
		for(String line : lines)
		{
			partedString.add(line);
		}
		
		return partedString;
	}
	
	private ArrayList<String> connectLines(ArrayList<String> lines)
	{
		ArrayList<String> partedString = new ArrayList<>();
		
		String newLine = "";
		for(String line : lines)
		{
			line = line.trim();
			if((line.lastIndexOf(".") == (line.length() - 1)) || (line.lastIndexOf("!") == (line.length() - 1)) || (line.lastIndexOf("?") == (line.length() - 1)))
			{
				newLine += (" " + line);
				partedString.add(newLine);
				newLine = "";
			}
			else
			{
				newLine += (" " + line);
			}
		}
		if(!(newLine.equals("")))
		{
			partedString.add(newLine);
		}
		return partedString;
	}
	
	private ArrayList<String> filterLines(ArrayList<String> texts)
	{
		ArrayList<String> filteredString = new ArrayList<>();
		
		for(String text : texts)
		{
			int lastPos = 0;
			int countDot = 0;
			int countQuestion = 0;
			int countExclamation = 0;
			do
			{
				lastPos = text.indexOf(".", lastPos + 1);
				if(lastPos != -1)
				{
					countDot++;
				}
			}
			while(lastPos != -1);
			lastPos = 0;
			do
			{
				lastPos = text.indexOf("?", lastPos + 1);
				if(lastPos != -1)
				{
					countQuestion++;
				}
			}
			while(lastPos != -1);
			lastPos = 0;
			do
			{
				lastPos = text.indexOf("!", lastPos + 1);
				if(lastPos != -1)
				{
					countExclamation++;
				}
			}
			while(lastPos != -1);
			if((countDot + countQuestion + countExclamation) > 1)
			{
				filteredString.add(text);
			}
			else if((countDot + countQuestion + countExclamation) == 1)
			{
				if(filteredString.size() > 0)
				{
					String test = (filteredString.get(filteredString.size() - 1) + text);
					filteredString.set(filteredString.size() - 1, test);
				}
			}
		}
		return filteredString;
	}
	
	private ArrayList<String> filterTitles(ArrayList<String> texts)
	{
		ArrayList<String> filterTitles = new ArrayList<>();
		
		for(String text : texts)
		{
			text = text.trim();
			if(!text.matches("[0-9].*") && !text.matches("(Rys).*") && !text.matches("(Tab).*"))
			{
				filterTitles.add(text);
			}
		}
		
		return filterTitles;
	}
	
	private String endingFormatting(ArrayList<String> texts){
		
		String endedString = "";
		
		for(String element: texts){
			element = "<paragraf>" + element + "</paragraf>" + "\n";
			endedString += element;
		}
		
		
		return endedString;
	}
}
