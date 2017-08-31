package edu.cs.ucla.preprocess;

import java.io.IOException;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;

public class FetchSourceCode {
	public static String fetchCodeByUrl(String url) {
		try {
			Document doc = Jsoup.connect(url).get();
			Elements elems = doc.getElementsByAttributeValue("itemprop", "text");
			Elements trs = elems.select("tr");
			StringBuilder sb = new StringBuilder();
			for(Element e : trs) {
				sb.append(e.text() + System.lineSeparator());
			}
			String formatted = new Formatter().formatSource(sb.toString());
			
			return formatted;
		} catch (IOException e) {
			if(e instanceof HttpStatusException && ((HttpStatusException)e).getStatusCode() == 404) {
				// 404 error: url may be broken, html page may be deleted from the server, etc.
				System.err.println("404 Not Found: " + url);
			} else {
				e.printStackTrace();
			}
		} catch (FormatterException e1) {
			e1.printStackTrace();
		}
		
		return null;
	}
	
	public static void main(String[] args) {
		String url = "https://github.com/programmingmind/Mandelbrot/blob/master/client/src/Validation.java";
		String code = fetchCodeByUrl(url);
		System.out.println(code);
	}
}
