package com.example.repository;

import com.example.entity.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicRepository extends JpaRepository<Clinic, Long> {

    @Query("SELECT c FROM Clinic c WHERE c.academyClass.id = :classId " +
           "AND c.clinicDate >= :today ORDER BY c.clinicDate ASC, c.clinicTime ASC")
    List<Clinic> findUpcomingClinicsByClass(@Param("classId") Long classId,
                                             @Param("today") LocalDate today);

    @Query("SELECT c FROM Clinic c WHERE c.academyClass.id = :classId " +
           "ORDER BY c.clinicDate DESC, c.clinicTime DESC")
    List<Clinic> findByAcademyClassIdOrderByClinicDateDesc(@Param("classId") Long classId);

    @Query("SELECT c FROM Clinic c WHERE c.academyClass.id = :classId " +
           "AND c.clinicDate = :clinicDate")
    Optional<Clinic> findByClassIdAndDate(@Param("classId") Long classId,
                                           @Param("clinicDate") LocalDate clinicDate);
}
