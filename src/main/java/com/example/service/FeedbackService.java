package com.example.service;

import com.example.dto.TeacherFeedbackDto;
import com.example.entity.StudentSubmission;
import com.example.entity.TeacherFeedback;
import com.example.repository.StudentSubmissionRepository;
import com.example.repository.TeacherFeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackService {
    private final TeacherFeedbackRepository feedbackRepository;
    private final StudentSubmissionRepository submissionRepository;
    
    public List<TeacherFeedbackDto> getSubmissionFeedbacks(Long submissionId) {
        List<TeacherFeedback> feedbacks = feedbackRepository.findBySubmissionId(submissionId);
        return feedbacks.stream()
                .map(TeacherFeedbackDto::from)
                .collect(Collectors.toList());
    }
    
    public List<TeacherFeedbackDto> getStudentFeedbacks(Long studentId) {
        List<TeacherFeedback> feedbacks = feedbackRepository.findBySubmissionStudentId(studentId);
        return feedbacks.stream()
                .map(TeacherFeedbackDto::from)
                .collect(Collectors.toList());
    }

    public List<TeacherFeedbackDto> getTestFeedbacks(Long testId) {
        List<TeacherFeedback> feedbacks = feedbackRepository.findBySubmissionTestId(testId);
        return feedbacks.stream()
                .map(TeacherFeedbackDto::from)
                .collect(Collectors.toList());
    }
    
    public TeacherFeedbackDto createFeedback(Long submissionId, TeacherFeedbackDto dto) {
        StudentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
        
        TeacherFeedback feedback = TeacherFeedback.builder()
                .submission(submission)
                .teacherName(dto.getTeacherName())
                .content(dto.getContent())
                .build();
        
        feedback = feedbackRepository.save(feedback);
        return TeacherFeedbackDto.from(feedback);
    }
    
    public TeacherFeedbackDto updateFeedback(Long feedbackId, TeacherFeedbackDto dto) {
        TeacherFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
        
        feedback.setTeacherName(dto.getTeacherName());
        feedback.setContent(dto.getContent());
        
        feedback = feedbackRepository.save(feedback);
        return TeacherFeedbackDto.from(feedback);
    }
    
    public void deleteFeedback(Long feedbackId) {
        feedbackRepository.deleteById(feedbackId);
    }
}