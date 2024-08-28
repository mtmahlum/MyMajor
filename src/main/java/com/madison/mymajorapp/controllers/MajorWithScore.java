package com.madison.mymajorapp.controllers;

import com.madison.mymajorapp.models.Major;

public class MajorWithScore {
  private String name;
  private int score;
  
  public MajorWithScore(Major major, int score) {
      this.name = major.getName();
      this.score = score;
  }

  // Getters
  public String getName() { return name; }
  public int getScore() { return score; }
}
