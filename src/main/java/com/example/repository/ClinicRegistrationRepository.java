package com.example.repository;

import com.example.entity.ClinicRegistration;
import com.example.entity.ClinicRegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicRegistrationRepository extends JpaRepository<ClinicRegistration, Long> {

    @Query("SELECT cr FROM ClinicRegistration cr WHERE cr.clinic.id = :clinicId")
    List<ClinicRegistration> findByClinicId(@Param("clinicId") Long clinicId);

    @Query("SELECT cr FROM ClinicRegistration cr WHERE cr.student.id = :studentId " +
           "AND cr.clinic.clinicDate >= :today ORDER BY cr.clinic.clinicDate ASC")
    List<ClinicRegistration> findUpcomingByStudentId(@Param("studentId") Long studentId,
                                                      @Param("today") java.time.LocalDate today);

    @Query("SELECT cr FROM ClinicRegistration cr WHERE cr.clinic.id = :clinicId " +
           "AND cr.student.id = :studentId")
    Optional<ClinicRegistration> findByClinicIdAndStudentId(@Param("clinicId") Long clinicId,
                                                              @Param("studentId") Long studentId);

    @Query("SELECT cr FROM ClinicRegistration cr WHERE cr.clinic.id = :clinicId " +
           "AND cr.status = :status")
    List<ClinicRegistration> findByClinicIdAndStatus(@Param("clinicId") Long clinicId,
                                                       @Param("status") ClinicRegistrationStatus status);
}
