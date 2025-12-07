package com.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;
    
    @Column(nullable = false)
    private Integer number;

    private String answer;

    @Column(nullable = false)
    private Double points;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    @Builder.Default
    private List<StudentSubmissionDetail> submissionDetails = new ArrayList<>();
}