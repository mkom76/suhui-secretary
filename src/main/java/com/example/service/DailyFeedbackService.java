package com.example.service;

import com.example.dto.DailyFeedbackDto;
import com.example.dto.StudentLessonDto;
import com.example.entity.*;
import com.example.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyFeedbackService {
    private final StudentLessonRepository studentLessonRepository;
    private final LessonRepository lessonRepository;
    private final StudentHomeworkRepository studentHomeworkRepository;
    private final StudentSubmissionRepository studentSubmissionRepository;
    private final StudentSubmissionDetailRepository studentSubmissionDetailRepository;
    private final StudentRepository studentRepository;

    public DailyFeedbackDto getTodayFeedback(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        LocalDate today = LocalDate.now();

        Optional<Lesson> todayLesson = lessonRepository.findByAcademyIdAndClassIdAndLessonDate(
            student.getAcademy().getId(), student.getAcademyClass().getId(), today);

        if (todayLesson.isEmpty()) {
            throw new RuntimeException("No lesson scheduled for today");
        }

        return getDailyFeedback(studentId, todayLesson.get().getId());
    }

    public DailyFeedbackDto getDailyFeedback(Long studentId, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        StudentLesson studentLesson = studentLessonRepository
                .findByStudentIdAndLessonId(studentId, lessonId)
                .orElse(null);

        DailyFeedbackDto feedback = new DailyFeedbackDto();
        feedback.setLessonId(lessonId);
        feedback.setLessonDate(lesson.getLessonDate());

        // A. Today's homework
        if (lesson.getHomework() != null) {
            feedback.setTodayHomework(getHomeworkSummary(studentId, lesson.getHomework()));
        }

        // Next homework
        List<Lesson> nextLessons = lessonRepository.findNextLessonsAfter(
            lesson.getAcademyClass().getId(), lesson.getLessonDate());
        if (!nextLessons.isEmpty() && nextLessons.get(0).getHomework() != null) {
            feedback.setNextHomework(getHomeworkSummary(studentId, nextLessons.get(0).getHomework()));
        }

        // B. Today's test with incorrect questions and academy accuracy
        if (lesson.getTest() != null) {
            feedback.setTodayTest(getTestFeedback(studentId, lesson.getTest()));
        }

        // C. Instructor feedback
        if (studentLesson != null) {
            feedback.setInstructorFeedback(studentLesson.getInstructorFeedback());
            feedback.setFeedbackAuthor(studentLesson.getFeedbackAuthor());
        }

        return feedback;
    }

    @Transactional
    public StudentLessonDto updateInstructorFeedback(Long studentId, Long lessonId,
                                                      String feedback, String authorName) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        StudentLesson studentLesson = studentLessonRepository
                .findByStudentIdAndLessonId(studentId, lessonId)
                .orElseGet(() -> StudentLesson.builder()
                        .student(student)
                        .lesson(lesson)
                        .build());

        studentLesson.setInstructorFeedback(feedback);
        studentLesson.setFeedbackAuthor(authorName);
        return StudentLessonDto.from(studentLessonRepository.save(studentLesson));
    }

    private DailyFeedbackDto.HomeworkSummary getHomeworkSummary(Long studentId, Homework homework) {
        Optional<StudentHomework> sh = studentHomeworkRepository
                .findByStudentIdAndHomeworkId(studentId, homework.getId());

        return DailyFeedbackDto.HomeworkSummary.builder()
                .homeworkId(homework.getId())
                .homeworkTitle(homework.getTitle())
                .questionCount(homework.getQuestionCount())
                .incorrectCount(sh.map(StudentHomework::getIncorrectCount).orElse(null))
                .completion(sh.map(StudentHomework::getCompletion).orElse(null))
                .dueDate(homework.getDueDate())
                .build();
    }

    private DailyFeedbackDto.TestFeedback getTestFeedback(Long studentId, Test test) {
        Optional<StudentSubmission> submission = studentSubmissionRepository
                .findByStudentIdAndTestId(studentId, test.getId());

        if (submission.isEmpty()) return null;

        StudentSubmission sub = submission.get();

        // Get incorrect question numbers
        List<Integer> incorrectQuestions = studentSubmissionDetailRepository
                .findBySubmissionId(sub.getId())
                .stream()
                .filter(d -> Boolean.FALSE.equals(d.getIsCorrect()))
                .map(d -> d.getQuestion().getNumber())
                .sorted()
                .collect(Collectors.toList());

        // Get academy accuracy rates
        List<Object[]> accuracyData = studentSubmissionDetailRepository
                .getQuestionCorrectRatesByTestId(test.getId());

        List<DailyFeedbackDto.QuestionAccuracy> rates = accuracyData.stream()
                .map(arr -> DailyFeedbackDto.QuestionAccuracy.builder()
                        .questionNumber((Integer) arr[0])
                        .correctRate((Double) arr[1])
                        .build())
                .collect(Collectors.toList());

        return DailyFeedbackDto.TestFeedback.builder()
                .testId(test.getId())
                .testTitle(test.getTitle())
                .studentScore(sub.getTotalScore())
                .incorrectQuestions(incorrectQuestions)
                .questionAccuracyRates(rates)
                .build();
    }
}
