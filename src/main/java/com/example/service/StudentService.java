package com.example.service;

import com.example.dto.StudentDto;
import com.example.entity.Student;
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
        Student student = dto.toEntity();
        student = studentRepository.save(student);
        return StudentDto.from(student);
    }
    
    public StudentDto updateStudent(Long id, StudentDto dto) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        student.setName(dto.getName());
        student.setGrade(dto.getGrade());
        student.setSchool(dto.getSchool());
        
        student = studentRepository.save(student);
        return StudentDto.from(student);
    }
    
    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }
}