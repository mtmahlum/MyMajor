package com.madison.mymajorapp.controllers;

import com.madison.mymajorapp.models.Certificate;

public class CertificateWithScore {
  private String name;
  private int score;

  public CertificateWithScore(Certificate certificate, int score) {
      this.name = certificate.getName();
      this.score = score;
  }

  // Getters
  public String getName() { return name; }
  public int getScore() { return score; }
}
