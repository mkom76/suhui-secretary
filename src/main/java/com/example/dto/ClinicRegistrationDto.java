package com.example.dto;

import com.example.entity.ClinicRegistration;
import com.example.entity.ClinicRegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicRegistrationDto {
    private Long id;
    private Long clinicId;
    private Long studentId;
    private String studentName;
    private ClinicRegistrationStatus status;
    private LocalDateTime createdAt;

    public static ClinicRegistrationDto from(ClinicRegistration registration) {
        return ClinicRegistrationDto.builder()
                .id(registration.getId())
                .clinicId(registration.getClinic().getId())
                .studentId(registration.getStudent().getId())
                .studentName(registration.getStudent().getName())
                .status(registration.getStatus())
                .createdAt(registration.getCreatedAt())
                .build();
    }
}
