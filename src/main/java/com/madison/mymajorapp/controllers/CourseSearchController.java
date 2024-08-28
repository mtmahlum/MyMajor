package com.madison.mymajorapp.controllers;

import com.madison.mymajorapp.models.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller to handle searches via the search bar
 */
@RestController
@RequestMapping("/api")
public class CourseSearchController {
    
    @GetMapping("/search")
    public List<String> searchCourses(@RequestParam String query) {
        return CourseCategory.getCourseMap().keySet().stream()
                .filter(key -> key.toLowerCase().contains(query.toLowerCase()))
                .limit(10)
                .collect(Collectors.toList());
    }
    
    @PostMapping("/recommend")
    public Map<String, List<?>> recommendMajorsAndCertificates(@RequestBody SearchRequest request) {
      try {
        Set<Course> completedCourses = request.convertCourses();
        List<School> schoolsOfInterest = request.convertSchools();

        List<Map<String, Object>> majors = new ArrayList<>();
        List<Map<String, Object>> certificates = new ArrayList<>();

        for (School school : schoolsOfInterest) {
            for (Major major : school.getMajors()) {
                int score = major.getTrie().bfsTraversal(completedCourses);
                Map<String, Object> majorInfo = new HashMap<>();
                majorInfo.put("name", major.getName());
                majorInfo.put("score", score);
                majorInfo.put("url", major.getUrl());
                majors.add(majorInfo);
            }

            for (Certificate certif : school.getCertifs()) {
                int score = certif.getTrie().bfsTraversal(completedCourses);
                Map<String, Object> certInfo = new HashMap<>();
                certInfo.put("name", certif.getName());
                certInfo.put("score", score);
                certInfo.put("url", certif.getURL());
                certificates.add(certInfo);
            }
        }

        // Sort the lists
        majors.sort((a, b) -> Integer.compare((Integer)b.get("score"), (Integer)a.get("score")));
        certificates.sort((a, b) -> Integer.compare((Integer)b.get("score"), (Integer)a.get("score")));

        Map<String, List<?>> result = new HashMap<>();
        result.put("majors", majors);
        result.put("certificates", certificates);
        return result;
    } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Error processing recommendation request: " + e.getMessage());
    }
    }
}