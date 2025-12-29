package com.example.controller;

import com.example.dto.LessonDto;
import com.example.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {
    private final LessonService lessonService;

    @GetMapping
    public ResponseEntity<Page<LessonDto>> getLessons(Pageable pageable) {
        return ResponseEntity.ok(lessonService.getLessons(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonDto> getLesson(@PathVariable Long id) {
        return ResponseEntity.ok(lessonService.getLesson(id));
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<List<LessonDto>> getLessonsByClass(@PathVariable Long classId) {
        return ResponseEntity.ok(lessonService.getLessonsByClass(classId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        lessonService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<LessonDto> createLesson(@RequestBody LessonDto dto) {
        return ResponseEntity.ok(lessonService.createLesson(dto.getAcademyId(), dto.getClassId(), dto.getLessonDate()));
    }

    @PutMapping("/{lessonId}/test/{testId}")
    public ResponseEntity<LessonDto> attachTest(@PathVariable Long lessonId, @PathVariable Long testId) {
        return ResponseEntity.ok(lessonService.attachTest(lessonId, testId));
    }

    @PutMapping("/{lessonId}/homework/{homeworkId}")
    public ResponseEntity<LessonDto> attachHomework(@PathVariable Long lessonId, @PathVariable Long homeworkId) {
        return ResponseEntity.ok(lessonService.attachHomework(lessonId, homeworkId));
    }

    @DeleteMapping("/{lessonId}/test")
    public ResponseEntity<LessonDto> detachTest(@PathVariable Long lessonId) {
        return ResponseEntity.ok(lessonService.detachTest(lessonId));
    }

    @DeleteMapping("/{lessonId}/homework")
    public ResponseEntity<LessonDto> detachHomework(@PathVariable Long lessonId) {
        return ResponseEntity.ok(lessonService.detachHomework(lessonId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<LessonDto>> getLessonsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(lessonService.getLessonsByStudent(studentId));
    }
}
