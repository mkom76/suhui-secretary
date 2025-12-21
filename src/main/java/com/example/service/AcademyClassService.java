package com.example.service;

import com.example.dto.AcademyClassDto;
import com.example.entity.Academy;
import com.example.entity.AcademyClass;
import com.example.repository.AcademyRepository;
import com.example.repository.AcademyClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AcademyClassService {
    private final AcademyClassRepository academyClassRepository;
    private final AcademyRepository academyRepository;

    public Page<AcademyClassDto> getClasses(Pageable pageable) {
        return academyClassRepository.findAll(pageable).map(AcademyClassDto::from);
    }

    public List<AcademyClassDto> getClassesByAcademy(Long academyId) {
        return academyClassRepository.findByAcademyId(academyId).stream()
                .map(AcademyClassDto::from)
                .collect(Collectors.toList());
    }

    public AcademyClassDto getClass(Long id) {
        AcademyClass academyClass = academyClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        return AcademyClassDto.from(academyClass);
    }

    public AcademyClassDto createClass(AcademyClassDto dto) {
        Academy academy = academyRepository.findById(dto.getAcademyId())
                .orElseThrow(() -> new RuntimeException("Academy not found"));

        AcademyClass academyClass = AcademyClass.builder()
                .name(dto.getName())
                .academy(academy)
                .build();

        academyClass = academyClassRepository.save(academyClass);
        return AcademyClassDto.from(academyClass);
    }

    public AcademyClassDto updateClass(Long id, AcademyClassDto dto) {
        AcademyClass academyClass = academyClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        academyClass.setName(dto.getName());

        if (dto.getAcademyId() != null && !dto.getAcademyId().equals(academyClass.getAcademy().getId())) {
            Academy academy = academyRepository.findById(dto.getAcademyId())
                    .orElseThrow(() -> new RuntimeException("Academy not found"));
            academyClass.setAcademy(academy);
        }

        academyClass = academyClassRepository.save(academyClass);
        return AcademyClassDto.from(academyClass);
    }

    public void deleteClass(Long id) {
        academyClassRepository.deleteById(id);
    }
}
