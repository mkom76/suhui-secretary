package com.example.dto;

import com.example.entity.Student;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDto {
    private Long id;
    private String name;
    private String grade;
    private String school;
    private String academy;

    public static StudentDto from(Student student) {
        return StudentDto.builder()
                .id(student.getId())
                .name(student.getName())
                .grade(student.getGrade())
                .school(student.getSchool())
                .academy(student.getAcademy())
                .build();
    }

    public Student toEntity() {
        return Student.builder()
                .id(this.id)
                .name(this.name)
                .grade(this.grade)
                .school(this.school)
                .academy(this.academy)
                .build();
    }
}