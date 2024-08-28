package com.madison.mymajorapp.controllers;

import java.util.PriorityQueue;

public class SearchResult {
    private PriorityQueue<MajorWithScore> majors;
    private PriorityQueue<CertificateWithScore> certificates;

    public SearchResult(PriorityQueue<MajorWithScore> majors, PriorityQueue<CertificateWithScore> certificates) {
        this.majors = majors;
        this.certificates = certificates;
    }

    // Getters

    public PriorityQueue<MajorWithScore> getMajors() {
        return majors;
    }

    public PriorityQueue<CertificateWithScore> getCertificates() {
        return certificates;
    }
}