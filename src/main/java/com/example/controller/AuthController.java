package com.example.controller;

import com.example.dto.AuthResponse;
import com.example.dto.LoginDto;
import com.example.entity.Student;
import com.example.entity.Teacher;
import com.example.repository.StudentRepository;
import com.example.repository.TeacherRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    @PostMapping("/student/login")
    public ResponseEntity<AuthResponse> studentLogin(@RequestBody LoginDto loginDto, HttpSession session) {
        Optional<Student> studentOpt = studentRepository.findByIdAndPin(loginDto.getStudentId(), loginDto.getPin());

        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder()
                            .message("학생 ID 또는 PIN이 올바르지 않습니다.")
                            .build());
        }

        Student student = studentOpt.get();
        session.setAttribute("userId", student.getId());
        session.setAttribute("userRole", "STUDENT");
        session.setAttribute("userName", student.getName());

        return ResponseEntity.ok(AuthResponse.builder()
                .userId(student.getId())
                .name(student.getName())
                .role("STUDENT")
                .message("로그인 성공")
                .build());
    }

    @PostMapping("/teacher/login")
    public ResponseEntity<AuthResponse> teacherLogin(@RequestBody LoginDto loginDto, HttpSession session) {
        Optional<Teacher> teacherOpt = teacherRepository.findByUsernameAndPin(loginDto.getUsername(), loginDto.getPin());

        if (teacherOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder()
                            .message("아이디 또는 PIN이 올바르지 않습니다.")
                            .build());
        }

        Teacher teacher = teacherOpt.get();
        session.setAttribute("userId", teacher.getId());
        session.setAttribute("userRole", "TEACHER");
        session.setAttribute("userName", teacher.getName());

        return ResponseEntity.ok(AuthResponse.builder()
                .userId(teacher.getId())
                .name(teacher.getName())
                .role("TEACHER")
                .message("로그인 성공")
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");
        String userName = (String) session.getAttribute("userName");

        if (userId == null || userRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(AuthResponse.builder()
                .userId(userId)
                .name(userName)
                .role(userRole)
                .build());
    }

    @PutMapping("/change-pin")
    public ResponseEntity<AuthResponse> changePin(
            @RequestBody Map<String, String> request,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");

        if (userId == null || userRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String currentPin = request.get("currentPin");
        String newPin = request.get("newPin");

        if (currentPin == null || newPin == null) {
            return ResponseEntity.badRequest()
                    .body(AuthResponse.builder()
                            .message("현재 PIN과 새 PIN을 입력해주세요")
                            .build());
        }

        try {
            if ("STUDENT".equals(userRole)) {
                Optional<Student> studentOpt = studentRepository.findById(userId);
                if (studentOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(AuthResponse.builder()
                                    .message("사용자를 찾을 수 없습니다")
                                    .build());
                }

                Student student = studentOpt.get();
                if (!currentPin.equals(student.getPin())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(AuthResponse.builder()
                                    .message("현재 PIN이 올바르지 않습니다")
                                    .build());
                }

                student.setPin(newPin);
                studentRepository.save(student);
            } else if ("TEACHER".equals(userRole)) {
                Optional<Teacher> teacherOpt = teacherRepository.findById(userId);
                if (teacherOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(AuthResponse.builder()
                                    .message("사용자를 찾을 수 없습니다")
                                    .build());
                }

                Teacher teacher = teacherOpt.get();
                if (!currentPin.equals(teacher.getPin())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(AuthResponse.builder()
                                    .message("현재 PIN이 올바르지 않습니다")
                                    .build());
                }

                teacher.setPin(newPin);
                teacherRepository.save(teacher);
            }

            return ResponseEntity.ok(AuthResponse.builder()
                    .message("PIN이 성공적으로 변경되었습니다")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.builder()
                            .message("PIN 변경에 실패했습니다")
                            .build());
        }
    }
}
