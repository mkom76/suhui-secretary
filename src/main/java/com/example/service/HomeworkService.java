package com.example.service;

import com.example.dto.HomeworkDto;
import com.example.entity.Homework;
import com.example.repository.HomeworkRepository;
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

    public Page<HomeworkDto> getHomeworks(Pageable pageable) {
        return homeworkRepository.findAll(pageable).map(HomeworkDto::from);
    }

    public HomeworkDto getHomework(Long id) {
        Homework homework = homeworkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Homework not found"));
        return HomeworkDto.from(homework);
    }

    public HomeworkDto createHomework(HomeworkDto dto) {
        Homework homework = dto.toEntity();
        homework = homeworkRepository.save(homework);
        return HomeworkDto.from(homework);
    }

    public HomeworkDto updateHomework(Long id, HomeworkDto dto) {
        Homework homework = homeworkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Homework not found"));

        homework.setTitle(dto.getTitle());
        homework.setQuestionCount(dto.getQuestionCount());
        homework.setMemo(dto.getMemo());
        homework = homeworkRepository.save(homework);
        return HomeworkDto.from(homework);
    }

    public void deleteHomework(Long id) {
        homeworkRepository.deleteById(id);
    }
}
