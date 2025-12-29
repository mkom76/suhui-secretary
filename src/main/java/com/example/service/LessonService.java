package com.example.service;

import com.example.dto.LessonDto;
import com.example.dto.LessonStudentStatsDto;
import com.example.entity.*;
import com.example.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonService {
    private final LessonRepository lessonRepository;
    private final AcademyRepository academyRepository;
    private final AcademyClassRepository academyClassRepository;
    private final TestRepository testRepository;
    private final HomeworkRepository homeworkRepository;
    private final StudentRepository studentRepository;
    private final StudentSubmissionRepository studentSubmissionRepository;
    private final StudentHomeworkRepository studentHomeworkRepository;

    /**
     * Get or create lesson for a specific date/class
     * Called automatically when creating test/homework
     */
    public Lesson getOrCreateLesson(Long academyId, Long classId, LocalDate lessonDate) {
        return lessonRepository.findByAcademyIdAndClassIdAndLessonDate(academyId, classId, lessonDate)
                .orElseGet(() -> {
                    Academy academy = academyRepository.findById(academyId)
                            .orElseThrow(() -> new RuntimeException("Academy not found"));
                    AcademyClass academyClass = academyClassRepository.findById(classId)
                            .orElseThrow(() -> new RuntimeException("Class not found"));

                    Lesson lesson = Lesson.builder()
                            .lessonDate(lessonDate)
                            .academy(academy)
                            .academyClass(academyClass)
                            .build();
                    return lessonRepository.save(lesson);
                });
    }

    @Transactional(readOnly = true)
    public Page<LessonDto> getLessons(Pageable pageable) {
        return lessonRepository.findAll(pageable).map(LessonDto::from);
    }

    @Transactional(readOnly = true)
    public LessonDto getLesson(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        return LessonDto.from(lesson);
    }

    @Transactional(readOnly = true)
    public List<LessonDto> getLessonsByClass(Long classId) {
        return lessonRepository.findByAcademyClassIdOrderByLessonDateDesc(classId)
                .stream()
                .map(LessonDto::from)
                .collect(Collectors.toList());
    }

    public void deleteLesson(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        // Validate: cannot delete if test or homework attached
        if (lesson.getTest() != null) {
            throw new RuntimeException("Cannot delete lesson with attached test");
        }
        if (lesson.getHomework() != null) {
            throw new RuntimeException("Cannot delete lesson with attached homework");
        }

        lessonRepository.deleteById(id);
    }

    /**
     * Create a new lesson (manual creation for the new workflow)
     */
    public LessonDto createLesson(Long academyId, Long classId, LocalDate lessonDate) {
        Academy academy = academyRepository.findById(academyId)
                .orElseThrow(() -> new RuntimeException("Academy not found"));
        AcademyClass academyClass = academyClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        // Check if lesson already exists for this date/class
        if (lessonRepository.findByAcademyIdAndClassIdAndLessonDate(academyId, classId, lessonDate).isPresent()) {
            throw new RuntimeException("Lesson already exists for this date and class");
        }

        Lesson lesson = Lesson.builder()
                .lessonDate(lessonDate)
                .academy(academy)
                .academyClass(academyClass)
                .build();

        lesson = lessonRepository.save(lesson);
        return LessonDto.from(lesson);
    }

    /**
     * Attach a test to a lesson
     */
    public LessonDto attachTest(Long lessonId, Long testId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        // Validate: test must not already be attached to another lesson
        if (test.getLesson() != null && !test.getLesson().getId().equals(lessonId)) {
            throw new RuntimeException("Test is already attached to another lesson");
        }

        // Validate: test must be from same academy and class
        if (!test.getAcademy().getId().equals(lesson.getAcademy().getId()) ||
            !test.getAcademyClass().getId().equals(lesson.getAcademyClass().getId())) {
            throw new RuntimeException("Test must be from the same academy and class as the lesson");
        }

        test.setLesson(lesson);
        testRepository.save(test);

        return LessonDto.from(lessonRepository.findById(lessonId).orElseThrow());
    }

    /**
     * Attach homework to a lesson
     */
    public LessonDto attachHomework(Long lessonId, Long homeworkId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new RuntimeException("Homework not found"));

        // Validate: homework must not already be attached to another lesson
        if (homework.getLesson() != null && !homework.getLesson().getId().equals(lessonId)) {
            throw new RuntimeException("Homework is already attached to another lesson");
        }

        // Validate: homework must be from same academy and class
        if (!homework.getAcademy().getId().equals(lesson.getAcademy().getId()) ||
            !homework.getAcademyClass().getId().equals(lesson.getAcademyClass().getId())) {
            throw new RuntimeException("Homework must be from the same academy and class as the lesson");
        }

        homework.setLesson(lesson);
        homeworkRepository.save(homework);

        return LessonDto.from(lessonRepository.findById(lessonId).orElseThrow());
    }

    /**
     * Detach test from a lesson
     */
    public LessonDto detachTest(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        if (lesson.getTest() != null) {
            Test test = lesson.getTest();
            test.setLesson(null);
            testRepository.save(test);
        }

        return LessonDto.from(lessonRepository.findById(lessonId).orElseThrow());
    }

    /**
     * Detach homework from a lesson
     */
    public LessonDto detachHomework(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        if (lesson.getHomework() != null) {
            Homework homework = lesson.getHomework();
            homework.setLesson(null);
            homeworkRepository.save(homework);
        }

        return LessonDto.from(lessonRepository.findById(lessonId).orElseThrow());
    }

    /**
     * Get lessons for a specific student (based on their class)
     */
    @Transactional(readOnly = true)
    public List<LessonDto> getLessonsByStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        return lessonRepository.findByAcademyClassIdOrderByLessonDateDesc(student.getAcademyClass().getId())
                .stream()
                .map(LessonDto::from)
                .collect(Collectors.toList());
    }

    /**
     * Update lesson feedback and announcement
     */
    public LessonDto updateLessonContent(Long lessonId, String commonFeedback, String announcement) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        lesson.setCommonFeedback(commonFeedback);
        lesson.setAnnouncement(announcement);

        lesson = lessonRepository.save(lesson);
        return LessonDto.from(lesson);
    }

    /**
     * Get student statistics for a lesson (test scores and homework completion)
     */
    @Transactional(readOnly = true)
    public LessonStudentStatsDto getLessonStudentStats(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        // Get all students in this class
        List<Student> students = studentRepository.findAll().stream()
                .filter(s -> s.getAcademyClass() != null && s.getAcademyClass().getId().equals(lesson.getAcademyClass().getId()))
                .collect(Collectors.toList());

        LessonStudentStatsDto stats = new LessonStudentStatsDto();

        // Test statistics
        if (lesson.getTest() != null) {
            List<LessonStudentStatsDto.StudentTestScore> testScores = new ArrayList<>();
            List<StudentSubmission> submissions = studentSubmissionRepository.findByTestId(lesson.getTest().getId());
            Map<Long, StudentSubmission> submissionMap = submissions.stream()
                    .collect(Collectors.toMap(s -> s.getStudent().getId(), s -> s));

            for (Student student : students) {
                StudentSubmission submission = submissionMap.get(student.getId());
                testScores.add(LessonStudentStatsDto.StudentTestScore.builder()
                        .studentId(student.getId())
                        .studentName(student.getName())
                        .score(submission != null ? submission.getTotalScore() : null)
                        .submitted(submission != null)
                        .build());
            }

            // Sort by score (descending), null scores at the end
            testScores.sort((a, b) -> {
                if (a.getScore() == null) return 1;
                if (b.getScore() == null) return -1;
                return b.getScore().compareTo(a.getScore());
            });

            // Assign ranks
            int rank = 1;
            Integer prevScore = null;
            int sameRankCount = 0;
            for (LessonStudentStatsDto.StudentTestScore score : testScores) {
                if (score.getScore() != null) {
                    if (prevScore != null && prevScore.equals(score.getScore())) {
                        sameRankCount++;
                    } else {
                        rank += sameRankCount;
                        sameRankCount = 1;
                    }
                    score.setRank(rank);
                    prevScore = score.getScore();
                }
            }

            // Calculate average
            double average = submissions.stream()
                    .mapToInt(StudentSubmission::getTotalScore)
                    .average()
                    .orElse(0.0);

            stats.setTestScores(testScores);
            stats.setTestAverage(average);
        }

        // Homework statistics
        if (lesson.getHomework() != null) {
            List<LessonStudentStatsDto.StudentHomeworkCompletion> homeworkCompletions = new ArrayList<>();
            List<StudentHomework> studentHomeworks = studentHomeworkRepository
                    .findByHomeworkId(lesson.getHomework().getId());
            Map<Long, StudentHomework> homeworkMap = studentHomeworks.stream()
                    .collect(Collectors.toMap(sh -> sh.getStudent().getId(), sh -> sh));

            for (Student student : students) {
                StudentHomework studentHomework = homeworkMap.get(student.getId());
                homeworkCompletions.add(LessonStudentStatsDto.StudentHomeworkCompletion.builder()
                        .studentId(student.getId())
                        .studentName(student.getName())
                        .incorrectCount(studentHomework != null ? studentHomework.getIncorrectCount() : null)
                        .completion(studentHomework != null ? studentHomework.getCompletion() : null)
                        .completed(studentHomework != null)
                        .totalQuestions(lesson.getHomework().getQuestionCount())
                        .build());
            }

            // Sort by completion (descending), null completion at the end
            homeworkCompletions.sort((a, b) -> {
                if (a.getCompletion() == null) return 1;
                if (b.getCompletion() == null) return -1;
                return b.getCompletion().compareTo(a.getCompletion());
            });

            // Calculate average completion
            double average = studentHomeworks.stream()
                    .mapToInt(StudentHomework::getCompletion)
                    .average()
                    .orElse(0.0);

            stats.setHomeworkCompletions(homeworkCompletions);
            stats.setHomeworkAverage(average);
        }

        return stats;
    }
}
