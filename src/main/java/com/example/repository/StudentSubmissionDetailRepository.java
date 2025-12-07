package com.example.repository;

import com.example.entity.StudentSubmissionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentSubmissionDetailRepository extends JpaRepository<StudentSubmissionDetail, Long> {
    List<StudentSubmissionDetail> findBySubmissionId(Long submissionId);
    
    @Query("SELECT d.question.number, COUNT(CASE WHEN d.isCorrect = true THEN 1 END) * 100.0 / COUNT(*) " +
           "FROM StudentSubmissionDetail d " +
           "WHERE d.submission.test.id = :testId " +
           "GROUP BY d.question.number " +
           "ORDER BY d.question.number")
    List<Object[]> getQuestionCorrectRatesByTestId(Long testId);
}