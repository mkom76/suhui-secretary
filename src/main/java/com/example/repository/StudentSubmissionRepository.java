package com.example.repository;

import com.example.entity.StudentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentSubmissionRepository extends JpaRepository<StudentSubmission, Long> {
    List<StudentSubmission> findByTestId(Long testId);
    List<StudentSubmission> findByStudentId(Long studentId);
    Optional<StudentSubmission> findByStudentIdAndTestId(Long studentId, Long testId);
    
    @Query("SELECT AVG(s.totalScore) FROM StudentSubmission s WHERE s.test.id = :testId")
    Double getAverageScoreByTestId(Long testId);
}