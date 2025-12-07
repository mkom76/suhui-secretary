package com.example.dto;

import com.example.entity.Test;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestDto {
    private Long id;
    private String title;
    private LocalDateTime createdAt;
    private Integer questionCount;

    public static TestDto from(Test test) {
        return TestDto.builder()
                .id(test.getId())
                .title(test.getTitle())
                .createdAt(test.getCreatedAt())
                .questionCount(test.getQuestions() != null ? test.getQuestions().size() : 0)
                .build();
    }

    public Test toEntity() {
        return Test.builder()
                .id(this.id)
                .title(this.title)
                .createdAt(this.createdAt)
                .build();
    }
}