package com.madison.mymajorapp;

import com.madison.mymajorapp.spiders.CourseCategorySpider;
import com.madison.mymajorapp.spiders.SchoolSpider;
import com.madison.mymajorapp.interceptors.VisitorInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
/**
 * SpringBoot main application class
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class MyMajorApp implements WebMvcConfigurer {
  
  private final VisitorInterceptor visitorInterceptor;
  
  public MyMajorApp(VisitorInterceptor visitorInterceptor) {
    this.visitorInterceptor = visitorInterceptor;
  }
  
  public static void main(String[] args) {
    SpringApplication.run(MyMajorApp.class, args);
  }
  
  @PostConstruct
  public void init() {
    loadStaticData();
  }
  
  private void loadStaticData() {
    CourseCategorySpider courseSpider = new CourseCategorySpider();
    courseSpider.loadCategoryData();

    SchoolSpider schoolSpider = new SchoolSpider();
    schoolSpider.loadSchoolData();
  }
  
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(visitorInterceptor);
  }

    @Bean
    WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://mymajorapp.com", "https://www.mymajorapp.com", "http://mymajorapp.com", "http://www.mymajorapp.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
      }
    };
  }
}
