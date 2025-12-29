package com.example.dto;

import com.example.entity.Lesson;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonDto {
    private Long id;
    private LocalDate lessonDate;
    private Long academyId;
    private String academyName;
    private Long classId;
    private String className;
    private Long testId;
    private String testTitle;
    private Long homeworkId;
    private String homeworkTitle;
    private String commonFeedback;
    private String announcement;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LessonDto from(Lesson lesson) {
        return LessonDto.builder()
                .id(lesson.getId())
                .lessonDate(lesson.getLessonDate())
                .academyId(lesson.getAcademy() != null ? lesson.getAcademy().getId() : null)
                .academyName(lesson.getAcademy() != null ? lesson.getAcademy().getName() : null)
                .classId(lesson.getAcademyClass() != null ? lesson.getAcademyClass().getId() : null)
                .className(lesson.getAcademyClass() != null ? lesson.getAcademyClass().getName() : null)
                .testId(lesson.getTest() != null ? lesson.getTest().getId() : null)
                .testTitle(lesson.getTest() != null ? lesson.getTest().getTitle() : null)
                .homeworkId(lesson.getHomework() != null ? lesson.getHomework().getId() : null)
                .homeworkTitle(lesson.getHomework() != null ? lesson.getHomework().getTitle() : null)
                .commonFeedback(lesson.getCommonFeedback())
                .announcement(lesson.getAnnouncement())
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .build();
    }
}
