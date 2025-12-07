package com.example.dto;

import com.example.entity.StudentSubmissionDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionDetailDto {
    private Long id;
    private Integer questionNumber;
    private String studentAnswer;
    private String correctAnswer;
    private Boolean isCorrect;
    
    public static SubmissionDetailDto from(StudentSubmissionDetail detail) {
        return SubmissionDetailDto.builder()
                .id(detail.getId())
                .questionNumber(detail.getQuestion().getNumber())
                .studentAnswer(detail.getStudentAnswer())
                .correctAnswer(detail.getQuestion().getAnswer())
                .isCorrect(detail.getIsCorrect())
                .build();
    }
}