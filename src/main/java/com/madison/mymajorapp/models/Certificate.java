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
 * Instance class for certificates. Contains the url, name, Trie, and course set.
 */
public class Certificate {
	// data fields
	private String name;
	private String url;
	private Set<Course> courses;
	@JsonIgnore
	protected Trie trie;
	
	public Certificate(String name, String url) {
	  this.name = name;
	  this.url = "https://guide.wisc.edu" + url;
	  this.trie = new Trie();
	  this.courses = new HashSet<Course>();
	}

	/**
	 * Accessor for name of Certificate
	 * 
	 * @return name of certificate
	 */
	public String getName() {
	  return this.name;
	}
	
	/**
	 * Accessor for url associated with Certificate
	 * 
	 * @return the url link for the certificate
	 */
	public String getURL() {
	  return this.url;
	}
	
	  // access the trie
	  public Trie getTrie() {
	    return this.trie;
	  }
	
	  /**
	   * Loads all of the given courses in a web page for a certificate into the Trie
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

	        // now handle sc_courselist elements not preceded by a h3 tag in a toggle
	        Elements plainTables = requirementsContainer.select("table.sc_courselist");

	        for (Element table : plainTables) {
	          processCourseTable(table);
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
