package com.madison.mymajorapp.controllers;

import com.madison.mymajorapp.models.*;
import com.madison.mymajorapp.spiders.SchoolSpider;
import java.util.*;

public class SearchRequest {
    private List<String> schoolsOfInterest;
    private Set<String> completedCourses;
    
    // Comparators
    public Comparator<MajorWithScore> majorComparator = (m1, m2) -> Integer.compare(m2.getScore(), m1.getScore());
    public Comparator<CertificateWithScore> certificateComparator = (c1, c2) -> Integer.compare(c2.getScore(), c1.getScore());
    
    // Getters and setters
    public Set<String> getCompletedCourses() {
        return completedCourses;
    }

    public void setCompletedCourses(Set<String> completedCourses) {
        this.completedCourses = completedCourses;
    }

    public List<String> getSchoolsOfInterest() {
        return schoolsOfInterest;
    }

    public void setSchoolsOfInterest(List<String> schoolsOfInterest) {
        this.schoolsOfInterest = schoolsOfInterest;
    }
    
    // Convert methods
    public List<School> convertSchools() {
        List<School> schools = new ArrayList<>();
        for (String schoolName : schoolsOfInterest) {
            School school = SchoolSpider.getSchoolByName(schoolName);
            if (school != null) {
                schools.add(school);
            }
        }
        return schools;
    }

    public Set<Course> convertCourses() {
        Set<Course> courses = new HashSet<>();
        for (String courseName : completedCourses) {
            Course course = CourseCategory.getCourseMap().get(courseName);
            if (course != null) {
                courses.add(course);
            }
        }
        return courses;
    }
}