package com.example.service;

import com.example.dto.LessonDto;
import com.example.entity.*;
import com.example.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
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
}
