package com.madison.mymajorapp.models;

import java.util.*;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Instance Class for a given course. Contains a course number ("eg: CS400"), and
 */
public class Course implements Comparable<Course> {
  private String courseNumber;
  private String title;
  private String credits;
  protected Element requisiteElement; // Element of HTML containing our requesite information
  protected List<PriorityQueue<Course>> requisitePaths; // a list of PriorityQueue's holding paths
                                                        // to reach this course (pq's sorted by
                                                        // least req's to most)
  // protected PriorityQueue<Course> prerequisites; // use a priority queue organizing the prereqs
  // of a course by their number of preqs
  // from most to least, those with equal

  /**
   * Constructs a new course object containing its course number, title, url link, credits, and
   * priority queue of prereq's The priority queue is organized from least prereq's to most
   * 
   * @param courseNumber
   */
  public Course(String courseNumber, String title, String credits, Element requisiteElement) {
    this.courseNumber = courseNumber;
    this.title = title;
    this.credits = credits;
    this.requisiteElement = requisiteElement;
    this.requisitePaths = new ArrayList<PriorityQueue<Course>>();
  }

  public Course() {
    this.courseNumber = "ROOT";
    this.credits = "0";
  }

  /**
   * Loads a list of requisite paths (priority queue of courses) for the Course object
   * 
   * @param courseMap - our map matching
   */
  public void addRequisites(Map<String, Course> courseMap) {
    // (1) if no requisites, just return!
    if (this.requisiteElement == null || this.requisiteElement.text().equalsIgnoreCase("None")) {
      return;
    }

    //if (this.courseNumber.substring(0, 4).equals("MATH")) {
      // (2) filter our text
      String requisiteText = this.requisiteElement.text();
      List<String> parts = splitRequisites(requisiteText, courseMap);

      // 3) parse the requisite text and build requisite paths
      this.requisitePaths = buildRequisitePaths(parts, courseMap);
      
      /*
      if (this.courseNumber.substring(0, 4).equals("MATH")) {
        System.out.println(requisitePathsToString() + "\n");
      }
      */
    //}
  }

  /**
   * Helper method to split our requisite String into a list of key terms/characters
   * 
   * @param text
   * @param courseMap
   * @return
   */
  private List<String> splitRequisites(String text, Map<String, Course> courseMap) {
    List<String> parts = new ArrayList<>();
    Elements links = this.requisiteElement.select("a");
    List<String> courseNumbersWithLinks = new ArrayList<>(); // key courses, category or number

    for (Element link : links) {
      String courseNumber = link.text(); // could be just a number, gonna need retrieval of category
      // if (this.courseNumber.equals("E C E/COMP SCI 561")) { System.out.println(courseNumber); }

      String nonNumeric = courseNumber.replaceAll("[0-9]", "").trim();
      String numeric = courseNumber.replaceAll("[^0-9]", "").trim();

      // only add courses numbered less than 700 (undergrad only)
      if (!numeric.matches("[789]\\d*")) {

        // if (this.courseNumber.equals("ECON 301")) { System.out.println("nonNumeric: " +
        // nonNumeric + "\nNumeric: " + numeric); }

        if (!nonNumeric.isBlank()) {
          courseNumbersWithLinks.add(nonNumeric.trim()); // E C E, MATH, COMP SCI, etc.
        }
        courseNumbersWithLinks.add(numeric.trim()); // number
      }
    }

    StringBuilder part = new StringBuilder();
    String lastCategory = null;
    //System.out.println(text);
    
    boolean addCourse = true;
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);

      // we've reached the end of a key part
      if (c == ' ' || c == '.' || c == ';') {
        String partText = part.toString().trim();
        if (!partText.isEmpty()) {
          if (courseNumbersWithLinks.contains(partText) && addCourse) {
            if (partText.matches("\\d+")) {
              parts.add(lastCategory + " " + partText); // extract numbers and append their category
              // System.out.println("added: " + lastCategory + partText);
            } else {
              lastCategory = partText; // extract non-digits (eg: MATH, COMP SCI, E C E
              // System.out.println("last category: " + lastCategory);
            }
          }

          else if (partText.equals("and") || partText.equals("or")) { // extract keywords
            parts.add(partText); // will have to retrieve categories from solo numbers
          }

          else if (partText.equalsIgnoreCase("Not")) { // "Not open to.. " --> no further prereqs
                                                       // will follow, just RETURN
            if (parts.size() == 1 && parts.get(0).equals("(")) {
              parts.clear();
            }
            return parts;
          }

          else if (partText.equalsIgnoreCase("placement")) { 
            // "placement into.." --> consider it as no requisites ONLY if we don't have "much" data
            // always return afterwards to avoid duplicate courses
            if (parts.size() < 10) {
              parts.clear();
              return parts;
            }
            // always skip past the course after it to avoid duplicate courses
            addCourse = false;
          }

          // append spaces when we're in the middle of a category key word
          else if (partText.substring(partText.length() - 1).matches("[A-Z]")) {
            part.append(c);
            continue; // don't reset StringBuilder
            // System.out.println("appended a space to " + partText.toString());
          }

          part = new StringBuilder(); // we've reached the end of a key part, reset
        }

        if (c == '.') { // further information is usually not necessary and will only corrupt our
                        // data (eg: some courses "do not fulfill...")
          return parts;
        }

        // key characters -> store them and
      } else if (c == '(' || c == ')' || c == ',' || c == '[' || c == ']') {
        if (part.length() > 0) {
          String partText = part.toString().trim();
          if (!partText.isEmpty()) {
            if (courseNumbersWithLinks.contains(partText) && addCourse) {
              if (partText.matches("\\d+")) {
                parts.add(lastCategory + " " + partText);
              }
            } else if (partText.equals("and") || partText.equals("or")) {
              parts.add(partText);
            }
          }
          part = new StringBuilder();
        }

        if (c != ',') {
          parts.add(String.valueOf(c));
          part = new StringBuilder();
        }

      } else { // regular char or number -> add it to the StringBuilder "part"
        part.append(c);
        // System.out.println(part);
        // must catch the last characters with key information
        if (i == text.length() - 1) {
          String partText = part.toString();
          if (courseNumbersWithLinks.contains(partText) && partText.matches("\\d+")) {
            parts.add(lastCategory + " " + partText);
          }
        }

      }
    }

    return parts;
  }

  /**
   * Recursive helper method to build our paths of prereqs. All permutations are included so that they are
   * repeats. But when built into a trie, we will only search further when there is unfound and
   * common courses.
   * 
   * @param parts
   * @param currentPath
   * @param courseMap
   */
  private List<PriorityQueue<Course>> buildRequisitePaths(List<String> parts, Map<String, Course> courseMap) {
    // (1) Create a stack for grouped elements
    Stack<List<PriorityQueue<Course>>> groupedPaths = new Stack<>();
    
    // (2) Create a List of Lists containing courses. We will add to this as we find courses and recurse back downwards
    List<PriorityQueue<Course>> currentPaths = new ArrayList<>(); // re-usable object

    
    // (3) Traverse our parts, noting when we're inside a bracket, or parenthesis to add to the stack
    for (int i = 0; i < parts.size(); i++) {
      // use a switch statement for all possible contents ("[]", "()", "and", "or", or a course
      // we will default that each course in requisite parts is an "or", when an "and" is reached, we will
      // modify accordingly
      String part = parts.get(i); // part to examine     
      switch (part) {
        // recurse downwards with our inner contents
        case "[":
        case "(":
          int closingIndex = findClosingIndex(parts, i + 1, part.equals("(") ? ")" : "]");
          List<String> inner = parts.subList(i + 1, closingIndex); // create a sublist
          
          // store a list of lists in our stack to add to our current paths
          groupedPaths.push(buildRequisitePaths(inner, courseMap));
          
          i = closingIndex; // advance to where we left off
          break;
        
        case "or":
          // (1) look ahead for a parentheses or bracket
          if (i < parts.size() - 1) {
            String next = parts.get(i + 1);
            if (next.equals("(") || next.equals("[")) {
              int endIndex = findClosingIndex(parts, i + 2, next.equals("(") ? ")" : "]");
              List<String> subParts = parts.subList(i + 2, endIndex); // create a sublist
              
              // store a list of lists in our stack to add to our current paths
              groupedPaths.push(buildRequisitePaths(subParts, courseMap));
              i = endIndex; // skip over the next element
            }
          }
          
          // (2) handle our stack by appending its resulting path to currentPaths
          if (!groupedPaths.isEmpty()) {
            while (!groupedPaths.isEmpty()) {
              List<PriorityQueue<Course>> paths = groupedPaths.pop();
              for (PriorityQueue<Course> path : paths) {
                currentPaths.add(path);
              }
            }
          }
          break;
          
        case "and":
          // (1) look ahead for a parentheses or bracket
          if (i < parts.size() - 1) {
            String next = parts.get(i + 1);
            if (next.equals("(") || next.equals("[")) {
              int endIndex = findClosingIndex(parts, i + 2, next.equals("(") ? ")" : "]");
              List<String> subParts = parts.subList(i + 2, endIndex); // create a sublist
              
              // store a list of lists in our stack to add to our current paths
              groupedPaths.push(buildRequisitePaths(subParts, courseMap));
              i = endIndex; // skip over the next element
            }
          }
          
          // (2) handle our stack creating all permutations of the lists of lists within it
          if (!groupedPaths.isEmpty()) {
            List<PriorityQueue<Course>> permutations = new ArrayList<>(currentPaths);
            
            if (permutations.isEmpty()) { permutations.add(new PriorityQueue<>()); }
            
            while (!groupedPaths.isEmpty()) {
              List<PriorityQueue<Course>> current = groupedPaths.pop();
              List<PriorityQueue<Course>> newPermutations = new ArrayList<>(); // re-usable
              
              for (PriorityQueue<Course> perm : permutations) {
                for (PriorityQueue<Course> list : current) {
                    PriorityQueue<Course> newPerm = new PriorityQueue<>(perm);
                    newPerm.addAll(list);
                    newPermutations.add(newPerm);
                }
              }
              permutations = newPermutations;
            }
            // permutations now is a list of requisite paths, add this to current paths
            currentPaths = permutations;
          }
          break;
          
        case ")":
        case "]":
        // irrelevant cases, just break  
          break;
          
        default:
          // we are defaulting to "or" logic by adding single element lists to our List within the Stack
          Course course = courseMap.get(part);
          
          if (course != null) {
            // correct our "and" logic BEHIND us when necessary
            if (i > 0) {
              String prevPart = parts.get(i - 1);
              
              if (prevPart.equals("and")) {
                // correct our previous paths as necessary
                int index = currentPaths.size() - 1;
                PriorityQueue<Course> combinedPath = new PriorityQueue<>();
                combinedPath.add(course);
                while (index >= 0) {
                  // combine one element lists, 
                  if (currentPaths.get(index).size() == 1) {
                    combinedPath.add(currentPaths.get(index).peek()); // add preceding one element
                    currentPaths.remove(index); // remove that previous one course path
                  }
                  // just append to long paths
                  else if (currentPaths.get(index).size() > 1) {
                    currentPaths.get(index).add(course); // add to preceding list
                  }
                  index--;
                }
                
                // if we created a combined list of more than one thing, add the combined list
                if (combinedPath.size() > 1) {
                  currentPaths.add(combinedPath);
                }
                
                continue; // skip the following code
              }
            }
            
            // default "or," add a new 1 element list
            PriorityQueue<Course> innerList = new PriorityQueue<>(); // initialize a new inner list
            innerList.add(course); // add the course to it
            currentPaths.add(innerList); // add our single element inner list to the     
          }
          
      }       
    }
    
    return currentPaths;

  }
  
  /**
   * Helper method to find the first closing index of an open bracket or parentheses
   * 
   * @param parts - the list of key String elements
   * @param startIndex - index after our opening bracket or parentheses
   * @param closingSymbol - ")" or "]"
   * @return the index of the first ")" or "]" accordingly
   */
  private int findClosingIndex(List<String> parts, int startIndex, String closingSymbol) {
    int nestedCount = 0;
    for (int i = startIndex; i < parts.size(); i++) {
        String part = parts.get(i);
        if (part.equals(closingSymbol)) {
          if (nestedCount == 0) {
            return i;
          } else {
            nestedCount--;
          }
        } else if (part.equals(closingSymbol.equals(")") ? "(" : "[")) {
          nestedCount++;
        }
    }
    System.out.println(this.courseNumber);
    for (String part : parts) {System.out.println(part); }
    throw new NoSuchElementException("Could not find symbol: \"" + closingSymbol+"\""); // should never happen if the input is well-formed

  }
  
  /**
   * Depicts the requisite paths in String format
   * 
   * @return all paths
   */
  public String requisitePathsToString() {
    StringBuilder str = new StringBuilder(this.courseNumber + ": " + this.title + "\n");
    int path = 1;
    for (PriorityQueue<Course> pq : requisitePaths) {
      str.append("Path " + path + ": ");
      for (Course course : pq) {
        str.append(course.courseNumber + ", ");
      }
      str.deleteCharAt(str.length() - 2); // delete the last comma
      path++;
      str.append("\n");
    }
    return str.toString();
  }

  /**
   * Accessor for the course number
   * 
   * @return course number
   */
  public String getCourseNumber() {
    return courseNumber;
  }

  /**
   * Accessor for course title
   * 
   * @return the course title
   */
  public String getTitle() {
    return this.title;
  }

  /**
   * Accessor for credits associated with class
   * 
   * @return number of credits
   */
  public String getCredits() {
    return this.credits;
  }

  public Element listRequisites() {
    return this.requisiteElement;
  }

  public String toString() {
    return courseNumber + ": " + title + "\n" + credits + " credits" + "\n";
  }

  /**
   * Accessor for prereqs
   * 
   * @return the priority queue of prereqs ranked from least prereqs to most
   */
  public List<PriorityQueue<Course>> getPreReqPaths() {
    return this.requisitePaths;
  }

  protected PriorityQueue<Course> helpFindLongestPath() {
    PriorityQueue<Course> longest = null;
    int max = -1;
    for (int i = 0; i < requisitePaths.size(); i++) {
      if (this.requisitePaths.get(i).size() > max) {
        longest = requisitePaths.get(i);
      }
    }
    return longest;
  }

  /**
   * Compares a given course to another based on their number of prereq's. Positive if we're larger
   * (advanced, more prereq's).
   * 
   * @param c - the course to be compared with
   * @return positive if this course has more prereq's than then other (more advanced), 0 if same,
   *         negative if less
   */
  @Override
  public int compareTo(Course c) {
    // (1) both contain prereq's
    if (this.requisitePaths.size() > 0 && c.requisitePaths.size() > 0) {
      return this.helpFindLongestPath().size() - c.helpFindLongestPath().size();
    }
    // (2) we don't, but the other does, we are LESS than the other course
    else if (this.requisitePaths.size() == 0 && c.requisitePaths.size() > 0) {
      return -1;
    }
    // (3) we do, but the other doesn't, we are GREATER than the other course
    else if (this.requisitePaths.size() > 0 && c.requisitePaths.size() == 0) {
      return +1;
    }
    // (4) neither of us contain prereq's
    else {
      return 0;
    }
  }

}
