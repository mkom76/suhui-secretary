package com.example.service;

import com.example.dto.StudentDto;
import com.example.entity.Academy;
import com.example.entity.AcademyClass;
import com.example.entity.Student;
import com.example.repository.AcademyRepository;
import com.example.repository.AcademyClassRepository;
import com.example.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentService {
    private final StudentRepository studentRepository;
    private final AcademyRepository academyRepository;
    private final AcademyClassRepository academyClassRepository;
    
    public Page<StudentDto> getStudents(String name, Pageable pageable) {
        Page<Student> students;
        if (name != null && !name.isEmpty()) {
            students = studentRepository.findByNameContaining(name, pageable);
        } else {
            students = studentRepository.findAll(pageable);
        }
        return students.map(StudentDto::from);
    }
    
    public StudentDto getStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return StudentDto.from(student);
    }
    
    public StudentDto createStudent(StudentDto dto) {
        Academy academy = academyRepository.findById(dto.getAcademyId())
                .orElseThrow(() -> new RuntimeException("Academy not found"));
        AcademyClass academyClass = academyClassRepository.findById(dto.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found"));

        Student student = Student.builder()
                .name(dto.getName())
                .grade(dto.getGrade())
                .school(dto.getSchool())
                .academy(academy)
                .academyClass(academyClass)
                .build();

        student = studentRepository.save(student);
        return StudentDto.from(student);
    }

    public StudentDto updateStudent(Long id, StudentDto dto) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        student.setName(dto.getName());
        student.setGrade(dto.getGrade());
        student.setSchool(dto.getSchool());

        if (dto.getAcademyId() != null && !dto.getAcademyId().equals(student.getAcademy().getId())) {
            Academy academy = academyRepository.findById(dto.getAcademyId())
                    .orElseThrow(() -> new RuntimeException("Academy not found"));
            student.setAcademy(academy);
        }

        if (dto.getClassId() != null && !dto.getClassId().equals(student.getAcademyClass().getId())) {
            AcademyClass academyClass = academyClassRepository.findById(dto.getClassId())
                    .orElseThrow(() -> new RuntimeException("Class not found"));
            student.setAcademyClass(academyClass);
        }

        student = studentRepository.save(student);
        return StudentDto.from(student);
    }
    
    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }
}