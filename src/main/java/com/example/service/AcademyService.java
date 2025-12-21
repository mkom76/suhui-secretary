package com.example.service;

import com.example.dto.AcademyDto;
import com.example.entity.Academy;
import com.example.repository.AcademyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AcademyService {
    private final AcademyRepository academyRepository;

    public Page<AcademyDto> getAcademies(Pageable pageable) {
        return academyRepository.findAll(pageable).map(AcademyDto::from);
    }

    public AcademyDto getAcademy(Long id) {
        Academy academy = academyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Academy not found"));
        return AcademyDto.from(academy);
    }

    public AcademyDto createAcademy(AcademyDto dto) {
        Academy academy = dto.toEntity();
        academy = academyRepository.save(academy);
        return AcademyDto.from(academy);
    }

    public AcademyDto updateAcademy(Long id, AcademyDto dto) {
        Academy academy = academyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Academy not found"));

        academy.setName(dto.getName());
        academy = academyRepository.save(academy);
        return AcademyDto.from(academy);
    }

    public void deleteAcademy(Long id) {
        academyRepository.deleteById(id);
    }
}
