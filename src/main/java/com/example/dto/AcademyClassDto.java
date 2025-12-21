package com.example.dto;

import com.example.entity.AcademyClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademyClassDto {
    private Long id;
    private String name;
    private Long academyId;
    private String academyName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AcademyClassDto from(AcademyClass academyClass) {
        return AcademyClassDto.builder()
                .id(academyClass.getId())
                .name(academyClass.getName())
                .academyId(academyClass.getAcademy() != null ? academyClass.getAcademy().getId() : null)
                .academyName(academyClass.getAcademy() != null ? academyClass.getAcademy().getName() : null)
                .createdAt(academyClass.getCreatedAt())
                .updatedAt(academyClass.getUpdatedAt())
                .build();
    }
}
