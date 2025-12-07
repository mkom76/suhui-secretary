package com.example.repository;

import com.example.entity.TeacherFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeacherFeedbackRepository extends JpaRepository<TeacherFeedback, Long> {
    List<TeacherFeedback> findBySubmissionId(Long submissionId);
    List<TeacherFeedback> findBySubmissionStudentId(Long studentId);
    List<TeacherFeedback> findBySubmissionTestId(Long testId);
}