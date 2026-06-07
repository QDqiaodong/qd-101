package com.example.cityactivity.service;

import com.example.cityactivity.dto.request.ActivityCreateRequest;
import com.example.cityactivity.dto.response.ContentReviewResult;

public interface ContentReviewService {

    ContentReviewResult reviewActivityContent(ActivityCreateRequest request);

    ContentReviewResult reviewText(String text, String fieldName);
}
