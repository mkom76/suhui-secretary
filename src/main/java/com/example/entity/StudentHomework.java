package com.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_homeworks")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentHomework {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homework_id", nullable = false)
    private Homework homework;

    @Column(name = "incorrect_count")
    private Integer incorrectCount; // 오답 개수 (null = 미제출)

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 완성도 계산 (0-100), null이면 미제출 상태
    public Integer getCompletion() {
        // 미제출 상태
        if (incorrectCount == null) {
            return null;
        }

        if (homework == null || homework.getQuestionCount() == null || homework.getQuestionCount() == 0) {
            return 0;
        }

        int total = homework.getQuestionCount();
        int incorrect = incorrectCount;

        return (int) Math.round(((double)(total - incorrect) / total) * 100);
    }
}
