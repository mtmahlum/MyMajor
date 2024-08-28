package com.madison.mymajorapp.spiders;

import com.madison.mymajorapp.models.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.util.*;

/**
 * Class to spider every course at UW-Madison by searching into each
 */
public class CourseCategorySpider {
  // data fields
  protected HashMap<String, CourseCategory> categories; // "MATH" will map to its CourseCategory

  public CourseCategorySpider() {
    this.categories = new HashMap<>();
  }

  /**
   * Loads each course category and builds the course objects associated with each as well as their prereq's
   */
  public void loadCategoryData() {
    try {
      // Connect to the website
      Document doc = Jsoup.connect("https://guide.wisc.edu/courses/").get();

      // Select the relevant elements containing the course categories and URLs
      Elements courseElements = doc.select("#cl-navigation .nav.levelone li a");

      for (Element element : courseElements) {
        String url = element.attr("href"); // store url link for each category
        String name = element.text();
        
        String title = null;
        String keyword = null;
        
        // regex course category keyword
        String regex1 = "\\(([^)]+)\\)(?=[^(]*$)";
        Pattern pattern1 = Pattern.compile(regex1);
        Matcher matcher1 = pattern1.matcher(name);

        if (matcher1.find()) {
          keyword = matcher1.group(1);
        }
        
        // regex course category title
        String regex2 = "(.*)\\(";
        Pattern pattern2 = Pattern.compile(regex2);
        Matcher matcher2 = pattern2.matcher(name);

        if (matcher2.find()) {
          title = matcher2.group(1);
          title = title.strip();
        }
        CourseCategory category = new CourseCategory(title, keyword, url);
        
        // LOAD OUR COURSE DATA
        category.loadCourseData();
        categories.put(keyword, category); // now we can add it
      }
      
      // now we must establish prereq's for all courses over all categories (only do it once because the courseMap is static   
      categories.get("ACCT I S").loadCourseRequisites();

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void printCategoryData() {
    for (CourseCategory category : categories.values()) {
      System.out.println(category.toString());
    }
  }

}
