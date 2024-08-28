package com.madison.mymajorapp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.madison.mymajorapp.services.VisitorCounter;

/**
 * Simple controller to track visitors to mymajorapp.com
 */
@RestController
public class VisitorController {

    @Autowired
    private VisitorCounter visitorCounter;

    @GetMapping("/visitorCount")
    public String getVisitorCount() {
        return "" + (visitorCounter.getCount() + 162); // modified after rebooting website
    }
}
