package com.madison.mymajorapp.models;

import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
/**
 * Instance class for a given school. Contains its name, url, and list of majors within it.
 */
public class School {
	// instance fields
	private String name;
	private final String url;
	private List<Major> majors;
	private List<Certificate> certificates;
	
	public School(String name, String url) {
		this.name = name;
		this.url = "https://guide.wisc.edu" + url;
		this.majors = new ArrayList<>();
		this.certificates = new ArrayList<>();
	}
	
	  // gets the names of every course and populates the majorNames arrayList
	  public void loadMajorData() {
	    try {
	      // Connect to the website
	      Document doc = Jsoup.connect(this.url).get();
	      
          Elements majorElements = doc.select("li:has(a[href] h3):not(:contains(Certificate))");
          Elements certifElements = doc.select("li:has(a[href] h3:contains(Certificate))");  // Selects <li> elements that have an <a> with an <h3> inside
          
          // select major elements
          for (Element majorElement : majorElements) {
            Element link = majorElement.selectFirst("a");
            Element h3 = link.selectFirst("h3");
            
            if (h3 != null) {
              String majorName = h3.text();
              String degreeType = null;
              
              String regex = ", ([B|J][A-Z]+)";
              Pattern pattern = Pattern.compile(regex);
              Matcher matcher = pattern.matcher(majorName);
              if (matcher.find()) {
                degreeType = matcher.group(1);
              }
              
              String majorURL = link.attr("href");
              Major major = new Major(majorName, majorURL, degreeType);
              

              major.loadCourseRequirements();
              
              majors.add(major);
            }
          }
          
          // select certificate elements
          for (Element certifElement : certifElements) {
              Element link = certifElement.selectFirst("a");
              Element h3 = link.selectFirst("h3");

              if (h3 != null && h3.text().contains("Certificate")) {
                  String certifName = h3.text();
                  String certifURL = link.attr("href");
                  Certificate certif = new Certificate(certifName, certifURL);
                  
                  certif.loadCourseRequirements();
                  certificates.add(certif);
              }
          }      
	    } catch (IOException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	    }
	  }
	  
	  public List<Major> getMajors() {
	    return this.majors;
	  }
	  
	  public List<Certificate> getCertifs() {
	    return this.certificates;
	  }
	  
	  public String getURL() {
	    return this.url;
	  }
	  
	  public String getName() {
	    return this.name;
	  }
	  
	  public void printMajorList() {
	    for (Major major : majors) {
	      System.out.println(major.getName());
	    }
	  }
	  
	  public void printCertifList() {
	    for (Certificate certif : certificates) {
	      System.out.println(certif.getName());
	    }
	  }
	  
	  
	  public Major getIndex(int index) {
	    return majors.get(index);
	  } 
	  
	  public String toString() {
	    return this.name;
	  }
	  
}
