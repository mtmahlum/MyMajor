package com.madison.mymajorapp.models;

import java.io.IOException;
import java.util.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * An instance class for course categories like "MATH" or "E C E" and store a static course map
 */
public class CourseCategory {
  // data fields
  private String title; // title of category
  private String keyword; // keyword (eg: MATH, E C E, COMP SCI, etc.)
  private String url; // link to category page of courses
  protected static HashMap<String, Course> courseMap = new HashMap<>(); // maps course categories to a course ranked by least prereqs to most
  
  /**
   * Constructs a new course category with its keyword and url
   */
  public CourseCategory(String title, String keyword, String url) {
    this.title = title;
    this.keyword = keyword;
    this.url = "https://guide.wisc.edu" + url;
  }

  public String getTitle() {
    return this.title;
  }

  /**
   * Accessor for keyword
   * 
   * @return keyword
   */
  public String getKeyword() {
    return this.keyword;
  }

  /**
   * Accessor for url
   * 
   * @return url
   */
  public String getURL() {
    return this.url;
  }

  /**
   * Parses all courses and their prereqs from the url field
   */
  public void loadCourseData() {
    try {
      // Set up connection with no-cache headers
      Connection connection = Jsoup.connect(this.url)
                                   .header("Cache-Control", "no-cache")
                                   .header("Pragma", "no-cache")
                                   .header("Expires", "0")
                                   .timeout(999999999);
      
      // Connect to the website
      Document doc = connection.get();
      
      // System.out.println("Fetching data for URL: " + this.url);  // Log the URL
      // System.out.println("Fetched HTML length: " + input.length());  // Log the length of fetched HTML
      
      String courseNumber = "";
      String title = "";
      String credits = "";
      
      Elements courseBlocks = doc.select("div.courseblock "); // select all courseblocks
      
      // iterate over all courses in the given category
      for (Element courseBlock : courseBlocks) {
        // (1) extract course number
        Element courseNumberElement = courseBlock.selectFirst("span.courseblockcode");
        courseNumber = courseNumberElement != null ? courseNumberElement.text().replace("\u00a0", " ") : "";
      
        // (2) extract course title
        Element courseTitleElement = courseBlock.selectFirst("p.courseblocktitle strong");
        if (courseTitleElement != null) {
          String[] titleParts = courseTitleElement.text().split("—", 2);
          if (titleParts.length > 1) {
              title = titleParts[1].trim();
          }
        }
        
        // (3) extract credits
        Element creditsElement = courseBlock.selectFirst("p.courseblockcredits");
        if (creditsElement != null) {
          String[] creditsParts = creditsElement.text().split(" ");
          if (creditsParts.length > 0) { 
            credits = creditsParts[0];
          }
        }
        
        // (4) extract requesite information
        Element requisiteElement = courseBlock.selectFirst("div.cb-extras p.courseblockextra:contains(Requisites) span.cbextra-data");
        
        // only store undergraduate courses numbered <700
        String number = courseNumber.replaceAll("\\D+", "");
        if (Integer.parseInt(number) < 700) {
          // NOW store our Course object
          Course course = new Course(courseNumber, title, credits, requisiteElement);
          
          courseMap.put(courseNumber, course); // now put it in our map 
        }
      }            
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  /**
   * Loads prereqs for each course object after initializing all courses
   */
  public void loadCourseRequisites() {
    for (Course course : courseMap.values()) {
      course.addRequisites(courseMap);  
    } 
  }
  
  @Override
  public String toString() {
    StringBuilder toReturn = new StringBuilder(this.keyword + ": " + this.title + "\n\n");
    
    for (Course course : courseMap.values()) {
      toReturn.append(course.toString());
    }
    
    return toReturn.toString();
  }
  
  public String requisitesForAll() {
    StringBuilder toReturn = new StringBuilder("");
    for (Course course : courseMap.values()) {
      toReturn.append(course.getCourseNumber() + ": \n" + "Requisites: " + course.requisiteElement.text() + "\n\n");
    }
    
    return toReturn.toString();
  }
  
  public static HashMap<String, Course> getCourseMap() {
    return courseMap;
  }
}
