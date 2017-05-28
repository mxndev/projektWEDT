package com.projektWEDT.projektWEDT.controller;

import java.awt.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
			
			ArrayList<String> mar = new ArrayList<>();
			
			mar = findPartsWithEmptyLine(text);
			
		//	parser = new PDFParser(new FileInputStream(pdfFile));
			
			//parser.parse();
	      //  cdoc = parser.getDocument();
	      //  stripper = new PDFTextStripper();
	      //  pdoc = new PDDocument(cdoc);
	      //  text = stripper.getText(pdoc);
	        
			

		} catch (IOException e) {
			e.printStackTrace();
		}
		return "redirect:/";
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
	private ArrayList<String> findPartsWithEmptyLine(String text){
		
		ArrayList<String> partedString = new ArrayList<>();
		
		String[] lines = text.split(System.getProperty("line.separator"));
		
		System.out.println(lines[1]);
		System.out.println(lines[3]);
		
		return partedString;
		
		
		
	}
}
