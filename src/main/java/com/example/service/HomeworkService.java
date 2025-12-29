package com.example.service;

import com.example.dto.HomeworkDto;
import com.example.entity.Academy;
import com.example.entity.Homework;
import com.example.entity.AcademyClass;
import com.example.entity.Lesson;
import com.example.repository.AcademyRepository;
import com.example.repository.HomeworkRepository;
import com.example.repository.AcademyClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class HomeworkService {
    private final HomeworkRepository homeworkRepository;
    private final AcademyRepository academyRepository;
    private final AcademyClassRepository academyClassRepository;
    private final LessonService lessonService;

    public Page<HomeworkDto> getHomeworks(Pageable pageable) {
        return homeworkRepository.findAll(pageable).map(HomeworkDto::from);
    }

    public HomeworkDto getHomework(Long id) {
        Homework homework = homeworkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Homework not found"));
        return HomeworkDto.from(homework);
    }

    public HomeworkDto createHomework(HomeworkDto dto) {
        Academy academy = academyRepository.findById(dto.getAcademyId())
                .orElseThrow(() -> new RuntimeException("Academy not found"));
        AcademyClass academyClass = academyClassRepository.findById(dto.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found"));

        Homework homework = Homework.builder()
                .title(dto.getTitle())
                .questionCount(dto.getQuestionCount())
                .memo(dto.getMemo())
                .dueDate(dto.getDueDate())
                .academy(academy)
                .academyClass(academyClass)
                .lesson(null)  // Lesson will be attached later via LessonService
                .build();

        homework = homeworkRepository.save(homework);
        return HomeworkDto.from(homework);
    }

    public HomeworkDto updateHomework(Long id, HomeworkDto dto) {
        Homework homework = homeworkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Homework not found"));

        homework.setTitle(dto.getTitle());
        homework.setQuestionCount(dto.getQuestionCount());
        homework.setMemo(dto.getMemo());
        homework.setDueDate(dto.getDueDate());

        if (dto.getAcademyId() != null && !dto.getAcademyId().equals(homework.getAcademy().getId())) {
            Academy academy = academyRepository.findById(dto.getAcademyId())
                    .orElseThrow(() -> new RuntimeException("Academy not found"));
            homework.setAcademy(academy);
        }

        if (dto.getClassId() != null && !dto.getClassId().equals(homework.getAcademyClass().getId())) {
            AcademyClass academyClass = academyClassRepository.findById(dto.getClassId())
                    .orElseThrow(() -> new RuntimeException("Class not found"));
            homework.setAcademyClass(academyClass);
        }

        homework = homeworkRepository.save(homework);
        return HomeworkDto.from(homework);
    }

    public void deleteHomework(Long id) {
        homeworkRepository.deleteById(id);
    }

    public java.util.List<HomeworkDto> getUnattachedHomeworks(Long academyId, Long classId) {
        return homeworkRepository.findUnattachedByAcademyIdAndClassId(academyId, classId)
                .stream()
                .map(HomeworkDto::from)
                .collect(java.util.stream.Collectors.toList());
    }
}
