package com.example.repository;

import com.example.entity.AcademyClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcademyClassRepository extends JpaRepository<AcademyClass, Long> {
    List<AcademyClass> findByAcademyId(Long academyId);
}
