package com.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "student_submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @ManyToOne
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;
    
    @Column(name = "total_score")
    private Integer totalScore;
    
    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StudentSubmissionDetail> details = new ArrayList<>();
    
    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL)
    @Builder.Default
    private List<TeacherFeedback> feedbacks = new ArrayList<>();
}