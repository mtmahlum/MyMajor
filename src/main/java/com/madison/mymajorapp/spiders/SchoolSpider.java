package com.madison.mymajorapp.spiders;

import com.madison.mymajorapp.models.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A spider to create School objects and establish their majors and certificates and implicitly build the Tries for all.
 */
public class SchoolSpider {
  public static List<School> schools = new ArrayList<>();
  
  // gets the names of every course and populates the majorNames arrayList
  public void loadSchoolData() {
    try {
      // Connect to the website
      Document doc = Jsoup.connect("https://guide.wisc.edu/undergraduate/").get();

      String htmlContent = doc.html();
      
      String regex =
          "<li class=\\\"isparent\\\"><a href=\\\"([^\\\"]+)\\\">((College of Agricultural and Life Sciences)|(College of Engineering)|(College of Letters &amp;​ Science)|(Gaylord Nelson Institute for Environmental Studies)|(School of Business)|(School of Education)|(School of Human Ecology)|(School of Nursing)|(School of Pharmacy))</a></li>";

      // Compile the pattern
      Pattern pattern = Pattern.compile(regex);

      // Create a matcher to find matches in the input string
      Matcher matcher = pattern.matcher(htmlContent);

      while (matcher.find()) {
        // create our new School object
        String url = matcher.group(1);
        String schoolName = matcher.group(2).replaceAll("&amp;", "&");
        School school = new School(schoolName, url);
        
        // for each school, populate its given majors
        school.loadMajorData();
        
        // now add the school with its list of majors attached to it
        schools.add(school);
      }

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public static School getSchoolByName(String name) {
    for (School school : schools) {
      if (school.getName().equals(name)) {
        return school;
      } else {
        // work around for L & S issues
        if (name.contains("Letters") && school.getName().contains("Letters")) {
          return school;
        }
      }
    }
    return null;
  }

  public void printSchoolData() {
    for (int i = 0; i < schools.size(); i++) {
      System.out.println(schools.get(i).toString());
    }
  }

  public School getIndex(int index) {
    return schools.get(index);
  }
}
