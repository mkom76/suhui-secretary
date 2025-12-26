package com.example.service;

import com.example.dto.LessonDto;
import com.example.entity.Academy;
import com.example.entity.AcademyClass;
import com.example.entity.Lesson;
import com.example.repository.AcademyClassRepository;
import com.example.repository.AcademyRepository;
import com.example.repository.LessonRepository;
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
        lessonRepository.deleteById(id);
    }
}
