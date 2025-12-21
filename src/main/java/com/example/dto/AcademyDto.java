package com.example.dto;

import com.example.entity.Academy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademyDto {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AcademyDto from(Academy academy) {
        return AcademyDto.builder()
                .id(academy.getId())
                .name(academy.getName())
                .createdAt(academy.getCreatedAt())
                .updatedAt(academy.getUpdatedAt())
                .build();
    }

    public Academy toEntity() {
        return Academy.builder()
                .id(this.id)
                .name(this.name)
                .build();
    }
}
