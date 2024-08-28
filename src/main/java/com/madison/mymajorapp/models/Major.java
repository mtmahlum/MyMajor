package com.madison.mymajorapp.models;

import com.madison.mymajorapp.utils.Trie;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Instance class for majors. Contains the url, name, Trie, and course set.
 */
public class Major {
  private String name;
  private String url;
  private String degreeType; // eg: BS, BS, BBA, etc.
  private Set<Course> courses;
  
  // a trie of courses organized in prerequisite fashion
  @JsonIgnore
  protected Trie trie;

  // base constructor
  public Major(String name, String url, String degreeType) {
    this.name = name;
    this.url = "https://guide.wisc.edu" + url;
    this.degreeType = degreeType;
    this.trie = new Trie();
    this.courses = new HashSet<Course>();
  }

  // gets name
  public String getName() {
    return this.name;
  }

  // gets url
  public String getUrl() {
    return this.url;
  }

  // get degree type
  public String getDegreeType() {
    return this.degreeType;
  }
  
  // access the trie
  public Trie getTrie() {
    return this.trie;
  }

  /**
   * Loads all of the given courses in a web page for a major into the Trie
   */
  public void loadCourseRequirements() {
    try {
      // Set up connection with no-cache headers
      Connection connection = Jsoup.connect(this.url).header("Cache-Control", "no-cache")
          .header("Pragma", "no-cache").header("Expires", "0");

      // Connect to the website
      Document doc = connection.get();

      // select the container with course info
      Element requirementsContainer = doc.selectFirst("#requirementstextcontainer");

      if (requirementsContainer != null) {
        Elements headings = requirementsContainer.select("h3");
        
        for (Element heading : headings) {
          String text = heading.text().toLowerCase();
          if (text.contains("honors")) {
            continue; // skip next sc_courselist
          }

          Element courseTable = heading.nextElementSibling();
          if (courseTable != null && courseTable.hasClass("sc_courselist")) {
            processCourseTable(courseTable);
          }
        }

        // now handle sc_courselist elements not preceded by a h3 tag in a toggle
        Elements plainTables = requirementsContainer.select("table.sc_courselist");

        for (Element table : plainTables) {
          if (table.previousElementSibling() != null && !table.previousElementSibling().tagName().equals("h3")) {
            processCourseTable(table);
          }
        }
      }


    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Private helper method to process
   * 
   * @param courseTable
   */
  private void processCourseTable(Element courseTable) {
    Elements courseRows = courseTable.select("tbody tr");

    // Process each row
    for (Element row : courseRows) {
      Element codeCol = row.selectFirst("td.codecol");
      if (codeCol != null) {
        // Extract course number
        Element courseLink = codeCol.selectFirst("a");
        if (courseLink != null) {
          String courseNumber = courseLink.text().replaceAll("\u00a0", " ").trim();
          Course course = CourseCategory.courseMap.get(courseNumber);
          
          if (course != null && !courses.contains(course)) {
            courses.add(course);
            trie.insert(course);
          }
        }
      }
    }
  }

}
