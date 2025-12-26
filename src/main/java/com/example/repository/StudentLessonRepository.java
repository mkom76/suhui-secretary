package com.example.repository;

import com.example.entity.StudentLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentLessonRepository extends JpaRepository<StudentLesson, Long> {
    Optional<StudentLesson> findByStudentIdAndLessonId(Long studentId, Long lessonId);

    List<StudentLesson> findByStudentIdOrderByLesson_LessonDateDesc(Long studentId);

    List<StudentLesson> findByLessonId(Long lessonId);

    @Query("SELECT sl FROM StudentLesson sl WHERE sl.student.id = :studentId " +
           "AND sl.lesson.lessonDate = :lessonDate")
    Optional<StudentLesson> findByStudentIdAndDate(@Param("studentId") Long studentId,
                                                     @Param("lessonDate") LocalDate lessonDate);
}
