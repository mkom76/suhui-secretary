package com.example.dto;

import com.example.entity.Homework;
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
public class HomeworkDto {
    private Long id;
    private String title;
    private Integer questionCount;
    private String memo;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static HomeworkDto from(Homework homework) {
        return HomeworkDto.builder()
                .id(homework.getId())
                .title(homework.getTitle())
                .questionCount(homework.getQuestionCount())
                .memo(homework.getMemo())
                .dueDate(homework.getDueDate())
                .createdAt(homework.getCreatedAt())
                .updatedAt(homework.getUpdatedAt())
                .build();
    }

    public Homework toEntity() {
        return Homework.builder()
                .id(this.id)
                .title(this.title)
                .questionCount(this.questionCount)
                .memo(this.memo)
                .dueDate(this.dueDate)
                .build();
    }
}
