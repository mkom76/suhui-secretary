package com.example.dto;

import com.example.entity.StudentHomework;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentHomeworkAssignmentDto {
    private Long studentId;
    private String studentName;
    private Long assignedHomeworkId;
    private String assignedHomeworkTitle;
    private Integer incorrectCount;
    private Integer unsolvedCount;
    private Integer completion;

    public static StudentHomeworkAssignmentDto from(StudentHomework sh) {
        return StudentHomeworkAssignmentDto.builder()
                .studentId(sh.getStudent().getId())
                .studentName(sh.getStudent().getName())
                .assignedHomeworkId(sh.getHomework().getId())
                .assignedHomeworkTitle(sh.getHomework().getTitle())
                .incorrectCount(sh.getIncorrectCount())
                .unsolvedCount(sh.getUnsolvedCount())
                .completion(sh.getCompletion())
                .build();
    }
}
