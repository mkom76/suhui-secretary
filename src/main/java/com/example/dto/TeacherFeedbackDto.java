package com.example.dto;

import com.example.entity.TeacherFeedback;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherFeedbackDto {
    private Long id;
    private Long submissionId;
    private StudentSubmissionDto submission;
    private String teacherName;
    private String content;
    private LocalDateTime createdAt;

    public static TeacherFeedbackDto from(TeacherFeedback feedback) {
        return TeacherFeedbackDto.builder()
                .id(feedback.getId())
                .submissionId(feedback.getSubmission().getId())
                .submission(StudentSubmissionDto.from(feedback.getSubmission()))
                .teacherName(feedback.getTeacherName())
                .content(feedback.getContent())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}