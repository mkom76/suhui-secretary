package com.example.config;

import com.example.entity.*;
import com.example.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;
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
            AcademyRepository academyRepository,
            AcademyClassRepository academyClassRepository,
            StudentRepository studentRepository,
            TestRepository testRepository,
            TestQuestionRepository testQuestionRepository,
            StudentSubmissionRepository studentSubmissionRepository,
            StudentSubmissionDetailRepository studentSubmissionDetailRepository,
            TeacherFeedbackRepository teacherFeedbackRepository,
            HomeworkRepository homeworkRepository,
            StudentHomeworkRepository studentHomeworkRepository
    ) {
        return args -> {
            log.info("Initializing sample data...");

            // 1. 학원 데이터 생성
            Academy academy1 = new Academy();
            academy1.setName("강남학원");
            academy1 = academyRepository.save(academy1);

            Academy academy2 = new Academy();
            academy2.setName("대치학원");
            academy2 = academyRepository.save(academy2);

            log.info("Created {} academies", 2);

            // 2. 반 데이터 생성
            AcademyClass class1 = new AcademyClass();
            class1.setName("고1 수학반");
            class1.setAcademy(academy1);
            class1 = academyClassRepository.save(class1);

            AcademyClass class2 = new AcademyClass();
            class2.setName("고2 영어반");
            class2.setAcademy(academy1);
            class2 = academyClassRepository.save(class2);

            AcademyClass class3 = new AcademyClass();
            class3.setName("고1 과학반");
            class3.setAcademy(academy2);
            class3 = academyClassRepository.save(class3);

            AcademyClass class4 = new AcademyClass();
            class4.setName("고3 종합반");
            class4.setAcademy(academy2);
            class4 = academyClassRepository.save(class4);

            log.info("Created {} classes", 4);

            // 3. 학생 데이터 생성
            Student student1 = new Student();
            student1.setName("김철수");
            student1.setGrade("고1");
            student1.setSchool("서울고등학교");
            student1.setAcademy(academy1);
            student1.setAcademyClass(class1);
            student1 = studentRepository.save(student1);

            Student student2 = new Student();
            student2.setName("이영희");
            student2.setGrade("고2");
            student2.setSchool("서울고등학교");
            student2.setAcademy(academy1);
            student2.setAcademyClass(class2);
            student2 = studentRepository.save(student2);

            Student student3 = new Student();
            student3.setName("박민수");
            student3.setGrade("고1");
            student3.setSchool("강남고등학교");
            student3.setAcademy(academy2);
            student3.setAcademyClass(class3);
            student3 = studentRepository.save(student3);

            Student student4 = new Student();
            student4.setName("정수진");
            student4.setGrade("고3");
            student4.setSchool("강남고등학교");
            student4.setAcademy(academy2);
            student4.setAcademyClass(class4);
            student4 = studentRepository.save(student4);

            log.info("Created {} students", 4);

            // 4. 시험 데이터 생성
            Test test1 = new Test();
            test1.setTitle("2024년 1차 모의고사");
            test1.setAcademy(academy1);
            test1.setAcademyClass(class1);
            test1 = testRepository.save(test1);

            Test test2 = new Test();
            test2.setTitle("2024년 2차 모의고사");
            test2.setAcademy(academy1);
            test2.setAcademyClass(class2);
            test2 = testRepository.save(test2);

            Test test3 = new Test();
            test3.setTitle("2024년 중간고사 대비");
            test3.setAcademy(academy2);
            test3.setAcademyClass(class3);
            test3 = testRepository.save(test3);

            log.info("Created {} tests", 3);

            // 3. 시험 문제 생성 (test1)
            List<TestQuestion> test1Questions = new ArrayList<>();
            double test1BasePoints = Math.floor((100.0 / 10) * 10) / 10;
            for (int i = 1; i <= 10; i++) {
                TestQuestion question = new TestQuestion();
                question.setTest(test1);
                question.setNumber(i);
                question.setAnswer(String.valueOf((i % 5) + 1)); // 정답: 1~5 반복
                question.setPoints(test1BasePoints); // 각 10.0점
                test1Questions.add(testQuestionRepository.save(question));
            }

            // 시험 문제 생성 (test2)
            List<TestQuestion> test2Questions = new ArrayList<>();
            double test2BasePoints = Math.floor((100.0 / 15) * 10) / 10;
            double test2UsedPoints = 0;
            for (int i = 1; i <= 15; i++) {
                TestQuestion question = new TestQuestion();
                question.setTest(test2);
                question.setNumber(i);
                question.setAnswer(String.valueOf((i % 5) + 1)); // 정답: 1~5 반복

                // 마지막 문제에 남은 점수 할당
                if (i == 15) {
                    question.setPoints(Math.round((100 - test2UsedPoints) * 10) / 10.0);
                } else {
                    question.setPoints(test2BasePoints);
                    test2UsedPoints += test2BasePoints;
                }
                test2Questions.add(testQuestionRepository.save(question));
            }

            // 시험 문제 생성 (test3)
            List<TestQuestion> test3Questions = new ArrayList<>();
            double test3BasePoints = Math.floor((100.0 / 20) * 10) / 10;
            for (int i = 1; i <= 20; i++) {
                TestQuestion question = new TestQuestion();
                question.setTest(test3);
                question.setNumber(i);
                question.setAnswer(String.valueOf((i % 5) + 1)); // 정답: 1~5 반복
                question.setPoints(test3BasePoints); // 각 5.0점
                test3Questions.add(testQuestionRepository.save(question));
            }

            log.info("Created {} test questions", test1Questions.size() + test2Questions.size() + test3Questions.size());

            // 4. 학생 제출 및 답안 생성 (student1의 test1 제출)
            StudentSubmission submission1 = new StudentSubmission();
            submission1.setStudent(student1);
            submission1.setTest(test1);
            submission1.setTotalScore(80); // 80점
            submission1.setSubmittedAt(LocalDateTime.now().minusDays(30));
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
            submission2.setSubmittedAt(LocalDateTime.now().minusDays(29));
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
            submission3.setSubmittedAt(LocalDateTime.now().minusDays(20));
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

            // student1의 test2 제출 (85점)
            StudentSubmission submission4 = new StudentSubmission();
            submission4.setStudent(student1);
            submission4.setTest(test2);
            submission4.setTotalScore(85);
            submission4.setSubmittedAt(LocalDateTime.now().minusDays(18));
            submission4 = studentSubmissionRepository.save(submission4);

            for (int i = 0; i < test2Questions.size(); i++) {
                StudentSubmissionDetail detail = new StudentSubmissionDetail();
                detail.setSubmission(submission4);
                detail.setQuestion(test2Questions.get(i));
                String correctAnswer = test2Questions.get(i).getAnswer();
                // 13문제는 정답, 2문제는 오답
                String studentAnswer = (i < 13) ? correctAnswer : "2";
                detail.setStudentAnswer(studentAnswer);
                detail.setIsCorrect(correctAnswer.equals(studentAnswer));
                studentSubmissionDetailRepository.save(detail);
            }

            // student1의 test3 제출 (92점)
            StudentSubmission submission5 = new StudentSubmission();
            submission5.setStudent(student1);
            submission5.setTest(test3);
            submission5.setTotalScore(92);
            submission5.setSubmittedAt(LocalDateTime.now().minusDays(10));
            submission5 = studentSubmissionRepository.save(submission5);

            for (int i = 0; i < test3Questions.size(); i++) {
                StudentSubmissionDetail detail = new StudentSubmissionDetail();
                detail.setSubmission(submission5);
                detail.setQuestion(test3Questions.get(i));
                String correctAnswer = test3Questions.get(i).getAnswer();
                // 18문제는 정답, 2문제는 오답
                String studentAnswer = (i < 18) ? correctAnswer : "4";
                detail.setStudentAnswer(studentAnswer);
                detail.setIsCorrect(correctAnswer.equals(studentAnswer));
                studentSubmissionDetailRepository.save(detail);
            }

            // student2의 test2 제출 (78점)
            StudentSubmission submission6 = new StudentSubmission();
            submission6.setStudent(student2);
            submission6.setTest(test2);
            submission6.setTotalScore(78);
            submission6.setSubmittedAt(LocalDateTime.now().minusDays(17));
            submission6 = studentSubmissionRepository.save(submission6);

            for (int i = 0; i < test2Questions.size(); i++) {
                StudentSubmissionDetail detail = new StudentSubmissionDetail();
                detail.setSubmission(submission6);
                detail.setQuestion(test2Questions.get(i));
                String correctAnswer = test2Questions.get(i).getAnswer();
                // 12문제는 정답, 3문제는 오답
                String studentAnswer = (i < 12) ? correctAnswer : "1";
                detail.setStudentAnswer(studentAnswer);
                detail.setIsCorrect(correctAnswer.equals(studentAnswer));
                studentSubmissionDetailRepository.save(detail);
            }

            // student3의 test3 제출 (88점)
            StudentSubmission submission7 = new StudentSubmission();
            submission7.setStudent(student3);
            submission7.setTest(test3);
            submission7.setTotalScore(88);
            submission7.setSubmittedAt(LocalDateTime.now().minusDays(9));
            submission7 = studentSubmissionRepository.save(submission7);

            for (int i = 0; i < test3Questions.size(); i++) {
                StudentSubmissionDetail detail = new StudentSubmissionDetail();
                detail.setSubmission(submission7);
                detail.setQuestion(test3Questions.get(i));
                String correctAnswer = test3Questions.get(i).getAnswer();
                // 17문제는 정답, 3문제는 오답
                String studentAnswer = (i < 17) ? correctAnswer : "3";
                detail.setStudentAnswer(studentAnswer);
                detail.setIsCorrect(correctAnswer.equals(studentAnswer));
                studentSubmissionDetailRepository.save(detail);
            }

            log.info("Created {} student submissions with details", 7);

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

            TeacherFeedback feedback5 = new TeacherFeedback();
            feedback5.setSubmission(submission4);
            feedback5.setTeacherName("김선생님");
            feedback5.setContent("좋은 성적입니다! 지난 시험보다 5점이나 올랐네요. 계속 이 추세를 유지하세요.");
            teacherFeedbackRepository.save(feedback5);

            TeacherFeedback feedback6 = new TeacherFeedback();
            feedback6.setSubmission(submission5);
            feedback6.setTeacherName("이선생님");
            feedback6.setContent("매우 훌륭한 성적입니다! 꾸준히 실력이 향상되고 있어요. 이번에 틀린 문제 유형을 복습하면 다음엔 만점도 가능할 것 같습니다.");
            teacherFeedbackRepository.save(feedback6);

            log.info("Created {} teacher feedbacks", 6);

            // 8. 숙제 데이터 생성
            Homework homework1 = new Homework();
            homework1.setTitle("수학 기본 개념 문제집");
            homework1.setQuestionCount(30);
            homework1.setMemo("1단원부터 3단원까지의 기본 개념 문제입니다. 다음 주 월요일까지 풀어오세요.");
            homework1.setDueDate(LocalDate.now().plusDays(8));
            homework1.setAcademy(academy1);
            homework1.setAcademyClass(class1);
            homeworkRepository.save(homework1);

            Homework homework2 = new Homework();
            homework2.setTitle("영어 독해 연습 문제");
            homework2.setQuestionCount(20);
            homework2.setMemo("교과서 본문 5개 지문 독해 문제입니다. 해석과 함께 제출하세요.");
            homework2.setDueDate(LocalDate.now().plusDays(2));
            homework2.setAcademy(academy1);
            homework2.setAcademyClass(class2);
            homeworkRepository.save(homework2);

            Homework homework3 = new Homework();
            homework3.setTitle("과학 실험 보고서 작성");
            homework3.setQuestionCount(5);
            homework3.setMemo("지난주 실험 내용을 바탕으로 보고서를 작성하세요. 각 문항은 실험 결과 분석 질문입니다.");
            homework3.setDueDate(LocalDate.now().plusDays(5));
            homework3.setAcademy(academy2);
            homework3.setAcademyClass(class3);
            homeworkRepository.save(homework3);

            Homework homework4 = new Homework();
            homework4.setTitle("한국사 연표 정리");
            homework4.setQuestionCount(15);
            homework4.setMemo("조선시대 주요 사건 15가지를 연표로 정리하고 각 사건의 의미를 서술하세요.");
            homework4.setDueDate(LocalDate.now().minusDays(2));
            homework4.setAcademy(academy2);
            homework4.setAcademyClass(class4);
            homeworkRepository.save(homework4);

            Homework homework5 = new Homework();
            homework5.setTitle("수학 심화 문제 풀이");
            homework5.setQuestionCount(50);
            homework5.setMemo("고난도 문제 50문제입니다. 오답 노트와 함께 제출하세요. 이번 주말까지 완료.");
            homework5.setDueDate(LocalDate.now().plusDays(1));
            homework5.setAcademy(academy1);
            homework5.setAcademyClass(class1);
            homeworkRepository.save(homework5);

            Homework homework6 = new Homework();
            homework6.setTitle("영어 단어 암기 테스트");
            homework6.setQuestionCount(100);
            homework6.setMemo("단어장 Chapter 5-7까지 총 100개 단어입니다. 금요일 쪽지 시험 예정.");
            homework6.setDueDate(LocalDate.now().plusDays(4));
            homework6.setAcademy(academy1);
            homework6.setAcademyClass(class2);
            homeworkRepository.save(homework6);

            log.info("Created {} homeworks", 6);

            // 9. 학생-숙제 완성도 데이터 생성
            StudentHomework sh1 = new StudentHomework();
            sh1.setStudent(student1);
            sh1.setHomework(homework1);
            sh1.setCompletion(85);
            studentHomeworkRepository.save(sh1);

            StudentHomework sh2 = new StudentHomework();
            sh2.setStudent(student1);
            sh2.setHomework(homework5);
            sh2.setCompletion(70);
            studentHomeworkRepository.save(sh2);

            StudentHomework sh3 = new StudentHomework();
            sh3.setStudent(student2);
            sh3.setHomework(homework2);
            sh3.setCompletion(95);
            studentHomeworkRepository.save(sh3);

            StudentHomework sh4 = new StudentHomework();
            sh4.setStudent(student2);
            sh4.setHomework(homework6);
            sh4.setCompletion(80);
            studentHomeworkRepository.save(sh4);

            StudentHomework sh5 = new StudentHomework();
            sh5.setStudent(student3);
            sh5.setHomework(homework3);
            sh5.setCompletion(90);
            studentHomeworkRepository.save(sh5);

            StudentHomework sh6 = new StudentHomework();
            sh6.setStudent(student4);
            sh6.setHomework(homework4);
            sh6.setCompletion(65);
            studentHomeworkRepository.save(sh6);

            log.info("Created {} student homework records", 6);

            log.info("Sample data initialization completed successfully!");
            log.info("===================================================");
            log.info("Summary:");
            log.info("- Academies: 2");
            log.info("- Classes: 4");
            log.info("- Students: 4");
            log.info("- Tests: 3");
            log.info("- Test Questions: 45");
            log.info("- Student Submissions: 7");
            log.info("- Teacher Feedbacks: 6");
            log.info("- Homeworks: 6");
            log.info("- Student Homework Records: 6");
            log.info("===================================================");
        };
    }
}