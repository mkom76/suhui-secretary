package com.example.controller;

import com.example.dto.TeacherFeedbackDto;
import com.example.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {
    private final FeedbackService feedbackService;

    @GetMapping("/test/{testId}")
    public ResponseEntity<List<TeacherFeedbackDto>> getTestFeedbacks(@PathVariable Long testId) {
        return ResponseEntity.ok(feedbackService.getTestFeedbacks(testId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<TeacherFeedbackDto>> getStudentFeedbacks(@PathVariable Long studentId) {
        return ResponseEntity.ok(feedbackService.getStudentFeedbacks(studentId));
    }

    @PostMapping
    public ResponseEntity<TeacherFeedbackDto> createFeedback(@RequestBody TeacherFeedbackDto dto) {
        return ResponseEntity.ok(feedbackService.createFeedback(dto.getSubmissionId(), dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeacherFeedbackDto> updateFeedback(
            @PathVariable Long id,
            @RequestBody TeacherFeedbackDto dto) {
        return ResponseEntity.ok(feedbackService.updateFeedback(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.noContent().build();
    }
}