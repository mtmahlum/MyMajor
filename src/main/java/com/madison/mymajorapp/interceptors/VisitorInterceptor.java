package com.madison.mymajorapp.interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.madison.mymajorapp.services.VisitorCounter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * SpringBoot component to intercept visitors to the webpage
 */
@Component
public class VisitorInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(VisitorInterceptor.class);
    
    @Autowired
    private VisitorCounter visitorCounter;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();
        if (requestURI.equals("/") || requestURI.equals("/index.html")) {
            HttpSession session = request.getSession(true);
            String visitorId = session.getId();
            long count = visitorCounter.incrementAndGet(visitorId);
            logger.info("Visitor count: " + count + ", Visitor ID: " + visitorId);
        }
        return true;
    }
}
