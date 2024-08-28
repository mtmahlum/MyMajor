package com.madison.mymajorapp.utils;

import com.madison.mymajorapp.models.*;
import java.util.*;

/**
 * A custom Trie object for each major and certificate in order to optimize cross comparison searches using a BFS.
 */
public class Trie {
  // data fields
  protected final CourseTrieNode<Course> ROOT = new CourseTrieNode<Course>(new Course());
  
  public CourseTrieNode<Course> getRoot() {
    return this.ROOT;
  }

  /**
   * Inner helper class for a trie node
   * 
   * @param <T> - the type of data in each trie node (Course)
   */
  public static class CourseTrieNode<T extends Comparable<T>>
      implements Comparable<CourseTrieNode<Course>> {
    // data fields
    private Course course;
    protected List<CourseTrieNode<T>> children;

    /**
     * Constructs a new trie node with it's list of children
     * 
     * @param course - the course to be stored in this node
     */
    public CourseTrieNode(Course course) {
      this.course = course;
      this.children = new ArrayList<CourseTrieNode<T>>();
    }

    public Course getCourse() {
      return this.course;
    }

    public List<CourseTrieNode<T>> getChildren() {
      return this.children;
    }

    @Override
    public int compareTo(CourseTrieNode<Course> node) {
      return this.course.compareTo(node.course);
    }
  }

  /**
   * Inserts a new trie node containing a Course object at the end of each path containing its
   * requisites (ancestors)
   * 
   * @param node - the course node to insert
   */
  public void insert(Course course) {
    // directly attach courses without requisites to the root
    if (course.getPreReqPaths().isEmpty()) {
      ROOT.children.add(new CourseTrieNode<Course>(course));
    }

    // otherwise, follow each path down from the root - we assume we will maintain our trie as
    // intended because we will be adding courses linearly down the requirements page for each major
    for (PriorityQueue<Course> path : course.getPreReqPaths()) {
      insertHelper(course, path);
    }
    
  }

  private void insertHelper(Course course, PriorityQueue<Course> path) {
    // Use a queue to conduct a bfs essentially. Look for existing courses in the Trie containing
    // our requisites and as soon as our current root node does not contain a requisite of our
    // course to insert, just add the course there.
    Queue<CourseTrieNode<Course>> queue = new LinkedList<>();

    // How do we know what course in the path we are trying to add throughout the bfs?
    // Examine the first node in the requisite path
    //Course currentRequisite = path.poll();
    
    // List of stacks for paths to the first requisite with the least requisite node on top of each stack
    List<Stack<CourseTrieNode<Course>>> pathStack = new ArrayList<>();
    for (Course requisite : path) {
      List<Stack<CourseTrieNode<Course>>> toAdd = findAndTraceBack(ROOT, requisite);
      if (toAdd != null) { pathStack.addAll(toAdd); }
    }
    
    // Start with the special root node
    queue.add(ROOT);
    
    while (!queue.isEmpty()) {
      boolean found = false;
      // remove our node from the queue and examine children each time
      CourseTrieNode<Course> currentRoot = queue.poll();
      
      // Some paths to our course to insert may be obscure because of the one requisite that
      // requires another requisite scenario, so we will create a stack using another helper method
      // to find any or all nodes we could search for.
      
      // Traverse the children of every node we've added to the queue
      for (CourseTrieNode<Course> child : currentRoot.children) {
        
        // if we find our course to insert ALREADY in the Trie under the root, just return and don't insert duplicates
        if (child.course.equals(course)) {
          return;
        }
        
        for (int i = 0; i < pathStack.size(); i++) {
          Stack<CourseTrieNode<Course>> stack = pathStack.get(i);
          
          if (!stack.isEmpty() && child.course.equals(stack.peek().course)) {
            stack.pop(); // remove the node from the stack
            queue.add(child); // enqueue our node that leads us to our requisite
          }
          
          // remove empty stacks
          if (stack.isEmpty()) {
            pathStack.remove(stack);
            i--;
            
            CourseTrieNode<Course> existingChild = findChild(child, course);
            
            if (existingChild == null && !found) { //
              child.children.add(new CourseTrieNode<Course>(course));
              found = true;
            }
            
          }
        }
        
      }

      // didn't find an existing requisite in our current node's children, just add the course here
      if (queue.isEmpty() && !found) {
        currentRoot.children.add(new CourseTrieNode<Course>(course));
      }

    }
  }

  /**
   * Private helper method to find a requisite node and trace back to the root
   *
   * @param root      - the root node to start the search
   * @param requisite - the requisite course to find
   * @param stack     - the stack to store the path to the root
   * @return the found requisite node, or null if not found
   */
  private List<Stack<CourseTrieNode<Course>>> findAndTraceBack(CourseTrieNode<Course> root,
      Course requisite) {
    // if we fell off the trie or the requisite course does not have requisites itself, we know we
    // won't find a direct path, return null
    if (root == null) {
      return null;
    }
    List<Stack<CourseTrieNode<Course>>> stackList = new ArrayList<>();
    Stack<CourseTrieNode<Course>> nodeStack = new Stack<>();
    
    // traverse children of our root
    for (CourseTrieNode<Course> child : root.getChildren()) {
      // when we find our desired requisite node, return it
      if (child.getCourse().equals(requisite)) {
        nodeStack.push(child);
        if (!root.equals(ROOT)) { nodeStack.push(root); }
        stackList.add(nodeStack);
        return stackList;
      }

      // recurse with every child
      List<Stack<CourseTrieNode<Course>>> found = findAndTraceBack(child, requisite);
      if (found != null) {
        if (!root.equals(ROOT)) { 
          for (Stack<CourseTrieNode<Course>> stack : found) {
            stack.push(root);
          }
        }
        stackList.addAll(found);
        // return nodeStack;
      }
    }
    return stackList; // if the root has no children and is not null, return null
  }

  /**
   * Private helper method to find a desired child of a given node within its children
   * 
   * @param node   - the parent of desired child node
   * @param course - the course to get to in a requisite path
   * @return
   */
  private CourseTrieNode<Course> findChild(CourseTrieNode<Course> node, Course course) {
    for (CourseTrieNode<Course> child : node.getChildren()) {
      if (child.getCourse().equals(course)) {
        return child;
      }
    }
    return null;
  }

  /**
   * Conducts a breadth first search of the Trie and only searches further down paths that are
   * rooted by a node containing a Course object in the priority queue
   * 
   * @param courses - a HashSet of Course objects to find common ground with (O(1) to search for
   *                contains)
   * @return - the number of credits in common with the priority queue of courses
   */
  public int bfsTraversal(Set<Course> courses) {
    Set<Course> visited = new HashSet<>();
    Queue<CourseTrieNode<Course>> queue = new PriorityQueue<>();
    queue.add(ROOT);
    int creditsInCommon = 0;

    // conduct BFS
    while (!queue.isEmpty()) {
      CourseTrieNode<Course> current = queue.poll(); // dequeue

      for (CourseTrieNode<Course> child : current.getChildren()) {
        // enqueue common course nodes
        if (courses.contains(child.getCourse())) {

          // only count courses that have not been visited
          if (!visited.contains(child.getCourse())) {
            
            // some credits are denoted as "1-n" so let's default to the higher number
            creditsInCommon += Integer.parseInt(child.getCourse().getCredits()
                .substring(child.getCourse().getCredits().length() - 1));
            visited.add(child.getCourse());
          }

          queue.add(child); // enqueue child nodes
        }
      }  
      visited.add(current.getCourse()); // mark the last node as visited
    }
    return creditsInCommon; // default return statement
  }

  /**
   * Prints the contents of the trie in a neat format
   */
  public void printContents() {
    printContentsHelper(ROOT, 0);
  }

  /**
   * Helper method to print the contents of the trie recursively
   *
   * @param node  - the current node being printed
   * @param level - the current level of depth in the trie
   */
  private void printContentsHelper(CourseTrieNode<Course> node, int level) {
    if (node == null) {
      return;
    }
    // Indentation based on the level
    for (int i = 0; i < level; i++) {
      System.out.print("      ");
    }

    // Print the course details
    System.out.println(node.getCourse().getCourseNumber());

    // Recursively print the children
    for (CourseTrieNode<Course> child : node.getChildren()) {
      printContentsHelper(child, level + 1);
    }
  }

  /**
   * Removes a given node from the Trie
   * 
   * @param node
   */
  public void remove(CourseTrieNode<Course> node) {
    // TODO
  }

  public CourseTrieNode<Course> search(Course course) {
    // TODO
    return null; // default return statement
  }



}
