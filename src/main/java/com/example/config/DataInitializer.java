package com.example.config;

import com.example.entity.*;
import com.example.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    @Profile("!prod") // 운영 환경이 아닌 경우에만 실행
    CommandLineRunner initDatabase(
            StudentRepository studentRepository,
            TestRepository testRepository,
            TestQuestionRepository testQuestionRepository,
            StudentSubmissionRepository studentSubmissionRepository,
            StudentSubmissionDetailRepository studentSubmissionDetailRepository,
            TeacherFeedbackRepository teacherFeedbackRepository
    ) {
        return args -> {
            log.info("Initializing sample data...");

            // 1. 학생 데이터 생성
            Student student1 = new Student();
            student1.setName("김철수");
            student1.setGrade("고1");
            student1.setSchool("서울고등학교");
            student1 = studentRepository.save(student1);

            Student student2 = new Student();
            student2.setName("이영희");
            student2.setGrade("고2");
            student2.setSchool("서울고등학교");
            student2 = studentRepository.save(student2);

            Student student3 = new Student();
            student3.setName("박민수");
            student3.setGrade("고1");
            student3.setSchool("강남고등학교");
            student3 = studentRepository.save(student3);

            Student student4 = new Student();
            student4.setName("정수진");
            student4.setGrade("고3");
            student4.setSchool("강남고등학교");
            student4 = studentRepository.save(student4);

            log.info("Created {} students", 4);

            // 2. 시험 데이터 생성
            Test test1 = new Test();
            test1.setTitle("2024년 1차 모의고사");
            test1 = testRepository.save(test1);

            Test test2 = new Test();
            test2.setTitle("2024년 2차 모의고사");
            test2 = testRepository.save(test2);

            Test test3 = new Test();
            test3.setTitle("2024년 중간고사 대비");
            test3 = testRepository.save(test3);

            log.info("Created {} tests", 3);

            // 3. 시험 문제 생성 (test1)
            List<TestQuestion> test1Questions = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                TestQuestion question = new TestQuestion();
                question.setTest(test1);
                question.setNumber(i);
                question.setAnswer(String.valueOf((i % 5) + 1)); // 정답: 1~5 반복
                test1Questions.add(testQuestionRepository.save(question));
            }

            // 시험 문제 생성 (test2)
            List<TestQuestion> test2Questions = new ArrayList<>();
            for (int i = 1; i <= 15; i++) {
                TestQuestion question = new TestQuestion();
                question.setTest(test2);
                question.setNumber(i);
                question.setAnswer(String.valueOf((i % 5) + 1)); // 정답: 1~5 반복
                test2Questions.add(testQuestionRepository.save(question));
            }

            // 시험 문제 생성 (test3)
            List<TestQuestion> test3Questions = new ArrayList<>();
            for (int i = 1; i <= 20; i++) {
                TestQuestion question = new TestQuestion();
                question.setTest(test3);
                question.setNumber(i);
                question.setAnswer(String.valueOf((i % 5) + 1)); // 정답: 1~5 반복
                test3Questions.add(testQuestionRepository.save(question));
            }

            log.info("Created {} test questions", test1Questions.size() + test2Questions.size() + test3Questions.size());

            // 4. 학생 제출 및 답안 생성 (student1의 test1 제출)
            StudentSubmission submission1 = new StudentSubmission();
            submission1.setStudent(student1);
            submission1.setTest(test1);
            submission1.setTotalScore(80); // 80점
            submission1 = studentSubmissionRepository.save(submission1);

            // student1의 답안 상세
            for (int i = 0; i < test1Questions.size(); i++) {
                StudentSubmissionDetail detail = new StudentSubmissionDetail();
                detail.setSubmission(submission1);
                detail.setQuestion(test1Questions.get(i));
                String correctAnswer = test1Questions.get(i).getAnswer();
                // 8문제는 정답, 2문제는 오답
                String studentAnswer = (i < 8) ? correctAnswer : String.valueOf((Integer.parseInt(correctAnswer) % 5) + 1);
                detail.setStudentAnswer(studentAnswer);
                detail.setIsCorrect(correctAnswer.equals(studentAnswer));
                studentSubmissionDetailRepository.save(detail);
            }

            // 5. 학생 제출 및 답안 생성 (student2의 test1 제출)
            StudentSubmission submission2 = new StudentSubmission();
            submission2.setStudent(student2);
            submission2.setTest(test1);
            submission2.setTotalScore(90); // 90점
            submission2 = studentSubmissionRepository.save(submission2);

            // student2의 답안 상세
            for (int i = 0; i < test1Questions.size(); i++) {
                StudentSubmissionDetail detail = new StudentSubmissionDetail();
                detail.setSubmission(submission2);
                detail.setQuestion(test1Questions.get(i));
                String correctAnswer = test1Questions.get(i).getAnswer();
                // 9문제는 정답, 1문제는 오답
                String studentAnswer = (i < 9) ? correctAnswer : "5";
                detail.setStudentAnswer(studentAnswer);
                detail.setIsCorrect(correctAnswer.equals(studentAnswer));
                studentSubmissionDetailRepository.save(detail);
            }

            // 6. 학생 제출 및 답안 생성 (student3의 test2 제출)
            StudentSubmission submission3 = new StudentSubmission();
            submission3.setStudent(student3);
            submission3.setTest(test2);
            submission3.setTotalScore(73); // 73점
            submission3 = studentSubmissionRepository.save(submission3);

            // student3의 답안 상세
            for (int i = 0; i < test2Questions.size(); i++) {
                StudentSubmissionDetail detail = new StudentSubmissionDetail();
                detail.setSubmission(submission3);
                detail.setQuestion(test2Questions.get(i));
                String correctAnswer = test2Questions.get(i).getAnswer();
                // 11문제는 정답, 4문제는 오답
                String studentAnswer = (i < 11) ? correctAnswer : "3";
                detail.setStudentAnswer(studentAnswer);
                detail.setIsCorrect(correctAnswer.equals(studentAnswer));
                studentSubmissionDetailRepository.save(detail);
            }

            log.info("Created {} student submissions with details", 3);

            // 7. 선생님 피드백 생성
            TeacherFeedback feedback1 = new TeacherFeedback();
            feedback1.setSubmission(submission1);
            feedback1.setTeacherName("김선생님");
            feedback1.setContent("전반적으로 잘 했습니다. 마지막 두 문제는 시간 부족으로 실수한 것 같네요. 다음에는 시간 배분에 더 신경쓰세요.");
            teacherFeedbackRepository.save(feedback1);

            TeacherFeedback feedback2 = new TeacherFeedback();
            feedback2.setSubmission(submission2);
            feedback2.setTeacherName("이선생님");
            feedback2.setContent("아주 우수한 성적입니다! 한 문제만 틀렸는데, 해당 개념을 다시 한 번 복습하면 좋겠습니다.");
            teacherFeedbackRepository.save(feedback2);

            TeacherFeedback feedback3 = new TeacherFeedback();
            feedback3.setSubmission(submission3);
            feedback3.setTeacherName("박선생님");
            feedback3.setContent("기본 개념은 잘 이해하고 있습니다. 응용 문제에서 실수가 있었네요. 문제를 더 꼼꼼히 읽는 연습이 필요합니다.");
            teacherFeedbackRepository.save(feedback3);

            TeacherFeedback feedback4 = new TeacherFeedback();
            feedback4.setSubmission(submission1);
            feedback4.setTeacherName("최선생님");
            feedback4.setContent("추가 피드백: 문제 8번과 9번의 풀이 과정을 다시 검토해보세요. 계산 실수가 있었습니다.");
            teacherFeedbackRepository.save(feedback4);

            log.info("Created {} teacher feedbacks", 4);

            log.info("Sample data initialization completed successfully!");
            log.info("===================================================");
            log.info("Summary:");
            log.info("- Students: 4");
            log.info("- Tests: 3");
            log.info("- Test Questions: 45");
            log.info("- Student Submissions: 3");
            log.info("- Teacher Feedbacks: 4");
            log.info("===================================================");
        };
    }
}