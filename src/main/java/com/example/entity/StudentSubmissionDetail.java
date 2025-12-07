package com.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "student_submission_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSubmissionDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "submission_id", nullable = false)
    private StudentSubmission submission;
    
    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private TestQuestion question;
    
    @Column(name = "student_answer")
    private String studentAnswer;
    
    @Column(name = "is_correct")
    private Boolean isCorrect;
}