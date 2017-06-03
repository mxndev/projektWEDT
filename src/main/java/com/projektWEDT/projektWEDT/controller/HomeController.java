package com.projektWEDT.projektWEDT.controller;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

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
	public String retrieveFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes,
			@RequestParam("type") String type) {

		if (file.isEmpty()) {
			System.out.println("File is empty!");
			redirectAttributes.addFlashAttribute("error", "true");
			return "redirect:/";
		}

		if (type.equals("pdf")) {
			System.out.println("ZAZNACZYLES PDF");
			try {
				System.out.println("File is uploaded well: " + file.getOriginalFilename());
				redirectAttributes.addFlashAttribute("error", "false");
				File pdfFile = convertFile(file);

				pdoc = PDDocument.load(pdfFile);
				stripper = new PDFTextStripper();
				text = stripper.getText(pdoc);
				pdoc.close();

				ArrayList<String> listOfParagrafs = new ArrayList<>();
				String endedString = "";

				listOfParagrafs = divideText(text);
				listOfParagrafs = filterTitles(listOfParagrafs);
				listOfParagrafs = connectLines(listOfParagrafs);
				listOfParagrafs = filterLines(listOfParagrafs);
				endedString = endingFormatting(listOfParagrafs);

				redirectAttributes.addFlashAttribute("endedString", endedString);

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (type.equals("html")) {
			System.out.println("ZAZNACZYLES HTML");
			try {
				System.out.println("File is uploaded well: " + file.getOriginalFilename());
				redirectAttributes.addFlashAttribute("error", "false");
				File htmlFile = convertFile(file);

				FileInputStream fstream = new FileInputStream(htmlFile);

				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine = "", text = "";

				while ((strLine = br.readLine()) != null) {
					text += strLine;
				}
				
				System.out.println(text);

				ArrayList<String> listOfParagrafs = new ArrayList<>();
				
				listOfParagrafs = parseHTMLFile(text);
				System.out.println(listOfParagrafs);
				String endedString = endingFormatting(listOfParagrafs);
				
				redirectAttributes.addFlashAttribute("endedString", endedString);
				
			} catch (IOException e) {
				e.printStackTrace();
			}

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

	private ArrayList<String> divideText(String text) {

		ArrayList<String> partedString = new ArrayList<>();

		String[] lines = text.split(System.getProperty("line.separator"));

		for (String line : lines) {
			partedString.add(line);
		}

		return partedString;
	}

	private ArrayList<String> connectLines(ArrayList<String> lines) {
		ArrayList<String> partedString = new ArrayList<>();

		String newLine = "";
		for (String line : lines) {
			line = line.trim();
			if ((line.lastIndexOf(".") == (line.length() - 1)) || (line.lastIndexOf("!") == (line.length() - 1))
					|| (line.lastIndexOf("?") == (line.length() - 1))) {
				newLine += (" " + line);
				partedString.add(newLine);
				newLine = "";
			} else {
				newLine += (" " + line);
			}
		}
		if (!(newLine.equals(""))) {
			partedString.add(newLine);
		}
		return partedString;
	}

	private ArrayList<String> filterLines(ArrayList<String> texts) {
		ArrayList<String> filteredString = new ArrayList<>();

		for (String text : texts) {
			int lastPos = 0;
			int countDot = 0;
			int countQuestion = 0;
			int countExclamation = 0;
			do {
				lastPos = text.indexOf(".", lastPos + 1);
				if (lastPos != -1) {
					countDot++;
				}
			} while (lastPos != -1);
			lastPos = 0;
			do {
				lastPos = text.indexOf("?", lastPos + 1);
				if (lastPos != -1) {
					countQuestion++;
				}
			} while (lastPos != -1);
			lastPos = 0;
			do {
				lastPos = text.indexOf("!", lastPos + 1);
				if (lastPos != -1) {
					countExclamation++;
				}
			} while (lastPos != -1);
			if ((countDot + countQuestion + countExclamation) > 1) {
				filteredString.add(text);
			} else if ((countDot + countQuestion + countExclamation) == 1) {
				if (filteredString.size() > 0) {
					String test = (filteredString.get(filteredString.size() - 1) + text);
					filteredString.set(filteredString.size() - 1, test);
				}
			}
		}
		return filteredString;
	}

	private ArrayList<String> filterTitles(ArrayList<String> texts) {
		ArrayList<String> filterTitles = new ArrayList<>();

		for (String text : texts) {
			text = text.trim();
			if (!text.matches("[0-9].*") && !text.matches("(Rys).*") && !text.matches("(Tab).*")) {
				filterTitles.add(text);
			}
		}

		return filterTitles;
	}

	private String endingFormatting(ArrayList<String> texts) {

		String endedString = "";

		for (String element : texts) {
			element = "<paragraf>" + element + "</paragraf>" + "\n";
			endedString += element;
		}

		return endedString;
	}
	
	private ArrayList<String> parseHTMLFile(String html) {
		ArrayList<String> parsedHTML = new ArrayList<>();
		ArrayList<String> HTMLAfterCleaning = new ArrayList<>();
		ArrayList<String> returnedHTML = new ArrayList<>();

		// część pierwsza, wstępne przetwarzanie wartości <p> i pustych linii
		
		int lastPos = 0;
		do
		{
			lastPos = html.indexOf("<p>", lastPos);
			if(lastPos != -1 )
			{
				parsedHTML.add(html.substring(lastPos + 3, html.indexOf("</p>", lastPos + 1)));
				lastPos = html.indexOf("</p>", lastPos + 1);
			}
			int tempPos = lastPos;
			tempPos = html.indexOf("<p>", tempPos + 1);
			if(tempPos != -1)
			{
				if(html.indexOf("<h1>", lastPos + 1) < tempPos && html.indexOf("<h1>", lastPos + 1) != -1 )
				{
					parsedHTML.add("");
				}
				else if(html.indexOf("<h2>", lastPos + 1) < tempPos && html.indexOf("<h2>", lastPos + 1 )!= -1 )
				{
					parsedHTML.add("");
				}
				else if(html.indexOf("<h3>", lastPos + 1) < tempPos && html.indexOf("<h3>", lastPos + 1) != -1)
				{
					parsedHTML.add("");
				}
				else if(html.indexOf("<h4>", lastPos + 1) < tempPos && html.indexOf("<h4>", lastPos + 1) != -1)
				{
					parsedHTML.add("");
				}
				else if(html.indexOf("<h5>", lastPos + 1) < tempPos && html.indexOf("<h5>", lastPos + 1) != -1)
				{
					parsedHTML.add("");
				}
				else if(html.indexOf("<h6>", lastPos + 1) < tempPos && html.indexOf("<h6>", lastPos + 1) != -1)
				{
					parsedHTML.add("");
				}
				else if(html.indexOf("</div>", lastPos + 1) < tempPos && html.indexOf("</div>", lastPos + 1) != -1)
				{
					parsedHTML.add("");
				}
			}
			else
			{
				lastPos = tempPos;
			}
		}
		while(lastPos != -1);
		
		for (String element : parsedHTML)
		{
			// usuwanie a href
			int lastPos2 = 0;
			do
			{
				lastPos2 = element.indexOf("<a ", lastPos2);
				if(lastPos2 != -1 )
				{
					element = element.substring(0, lastPos2) + element.substring(element.indexOf("</a>", lastPos2 + 1) + 4, element.length());
					lastPos2 = element.indexOf("</a>", lastPos2 + 1);
				}
			}
			while(lastPos2 != -1);
			
			// usuwanie strong
			int lastPos3 = 0;
			do
			{
				lastPos3 = element.indexOf("<strong>", lastPos3);
				if(lastPos3 != -1 )
				{
					element = element.substring(lastPos3 + 8, element.indexOf("</strong>", lastPos3 + 1));
					lastPos3 = element.indexOf("</strong>", lastPos3 + 1);
				}
			}
			while(lastPos3 != -1);
			
			// usuwanie br
			int lastPos4 = 0;
			do
			{
				lastPos4 = element.indexOf("<br", lastPos4);
				if(lastPos4 != -1 )
				{
					element = element.substring(lastPos4 + 3, element.indexOf("/>", lastPos4 + 1));
					lastPos4 = element.indexOf("/>", lastPos4 + 1);
				}
			}
			while(lastPos4 != -1);
			HTMLAfterCleaning.add(element);
		}
		
		String newLine = "";
		// część druga, łączenie w odpowiednie paragrafy
		for (String element : HTMLAfterCleaning)
		{
			if(element.equals(""))
			{
				if(!newLine.equals(""))
				{
					returnedHTML.add(newLine);
					newLine = "";
				}
			}
			else
			{
				newLine += element;
			}
		}
		if(!newLine.equals(""))
		{
			returnedHTML.add(newLine);
		}
		return returnedHTML;
	}
}
