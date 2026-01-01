package com.example.service;

import com.example.dto.StudentSubmissionDto;
import com.example.dto.SubmissionDetailDto;
import com.example.entity.*;
import com.example.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SubmissionService {
    private final StudentSubmissionRepository submissionRepository;
    private final StudentSubmissionDetailRepository detailRepository;
    private final TestQuestionRepository questionRepository;
    private final StudentRepository studentRepository;
    private final TestRepository testRepository;
    
    public StudentSubmissionDto submitAnswers(Long studentId, Long testId, Map<Integer, String> answers) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));
        
        // 기존 제출 확인
        StudentSubmission submission = submissionRepository.findByStudentIdAndTestId(studentId, testId)
                .orElse(StudentSubmission.builder()
                        .student(student)
                        .test(test)
                        .build());
        
        // 문제 가져오기
        List<TestQuestion> questions = questionRepository.findByTestIdOrderByNumber(testId);
        
        // 답안 저장 및 채점
        List<StudentSubmissionDetail> details = new ArrayList<>();
        double earnedPoints = 0.0;
        double totalPoints = 0.0;

        for (TestQuestion question : questions) {
            String studentAnswer = answers.get(question.getNumber());
            boolean isCorrect = question.getAnswer() != null &&
                              question.getAnswer().equals(studentAnswer);

            StudentSubmissionDetail detail = StudentSubmissionDetail.builder()
                    .submission(submission)
                    .question(question)
                    .studentAnswer(studentAnswer)
                    .isCorrect(isCorrect)
                    .build();

            details.add(detail);

            // 배점 합산
            totalPoints += question.getPoints();
            if (isCorrect) {
                earnedPoints += question.getPoints();
            }
        }

        // 총점 계산 (배점 기반, 반올림)
        int totalScore = totalPoints == 0 ? 0 : (int) Math.round((earnedPoints / totalPoints) * 100);
        submission.setTotalScore(totalScore);
        submission.setSubmittedAt(LocalDateTime.now());

        // 기존 상세 답안 삭제 후 새로 저장
        submission.getDetails().clear();
        submission.getDetails().addAll(details);
        
        submission = submissionRepository.save(submission);
        
        StudentSubmissionDto dto = StudentSubmissionDto.from(submission);
        dto.setDetails(details.stream()
                .map(SubmissionDetailDto::from)
                .collect(Collectors.toList()));
        
        return dto;
    }
    
    public StudentSubmissionDto getSubmission(Long submissionId) {
        StudentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
        
        StudentSubmissionDto dto = StudentSubmissionDto.from(submission);
        List<SubmissionDetailDto> details = submission.getDetails().stream()
                .map(SubmissionDetailDto::from)
                .collect(Collectors.toList());
        dto.setDetails(details);
        
        return dto;
    }
    
    public StudentSubmissionDto getSubmissionByStudentAndTest(Long studentId, Long testId) {
        StudentSubmission submission = submissionRepository.findByStudentIdAndTestId(studentId, testId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
        
        StudentSubmissionDto dto = StudentSubmissionDto.from(submission);
        List<SubmissionDetailDto> details = submission.getDetails().stream()
                .map(SubmissionDetailDto::from)
                .collect(Collectors.toList());
        dto.setDetails(details);
        
        return dto;
    }
    
    public List<StudentSubmissionDto> getStudentSubmissions(Long studentId) {
        List<StudentSubmission> submissions = submissionRepository.findByStudentId(studentId);

        return submissions.stream()
                .map(submission -> {
                    StudentSubmissionDto dto = StudentSubmissionDto.from(submission);

                    // Calculate class average and rank for this test
                    List<StudentSubmission> allSubmissions = submissionRepository.findByTestId(submission.getTest().getId());

                    // Class average
                    double classAverage = allSubmissions.stream()
                            .mapToInt(StudentSubmission::getTotalScore)
                            .average()
                            .orElse(0.0);

                    // Calculate rank
                    List<Integer> scores = allSubmissions.stream()
                            .map(StudentSubmission::getTotalScore)
                            .sorted((a, b) -> b.compareTo(a)) // Descending order
                            .collect(Collectors.toList());

                    int rank = 1;
                    for (int i = 0; i < scores.size(); i++) {
                        if (scores.get(i).equals(submission.getTotalScore())) {
                            rank = i + 1;
                            break;
                        }
                    }

                    dto.setClassAverage(classAverage);
                    dto.setRank(rank);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<StudentSubmissionDto> getTestSubmissions(Long testId) {
        List<StudentSubmission> submissions = submissionRepository.findByTestId(testId);
        return submissions.stream()
                .map(StudentSubmissionDto::from)
                .collect(Collectors.toList());
    }
}