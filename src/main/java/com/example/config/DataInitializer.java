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
    // @Profile("!prod") // 일시적으로 prod에서도 더미 데이터 생성 (런칭 전 다시 활성화 필요)
    CommandLineRunner initDatabase(
            AcademyRepository academyRepository,
            AcademyClassRepository academyClassRepository,
            StudentRepository studentRepository,
            TestRepository testRepository,
            TestQuestionRepository testQuestionRepository,
            StudentSubmissionRepository studentSubmissionRepository,
            StudentSubmissionDetailRepository studentSubmissionDetailRepository,
            HomeworkRepository homeworkRepository,
            StudentHomeworkRepository studentHomeworkRepository,
            TeacherRepository teacherRepository,
            LessonRepository lessonRepository
    ) {
        return args -> {
            log.info("Initializing sample data...");

            // 0. 선생님 데이터 생성
            Teacher teacher1 = new Teacher();
            teacher1.setName("수희");
            teacher1.setUsername("suhui");
            teacher1.setPin("123456");
            teacher1 = teacherRepository.save(teacher1);

            log.info("Created {} teachers", 1);

            // 1. 학원 데이터 생성
            Academy academy1 = new Academy();
            academy1.setName("강남수학학원");
            academy1 = academyRepository.save(academy1);

            Academy academy2 = new Academy();
            academy2.setName("대치수학학원");
            academy2 = academyRepository.save(academy2);

            log.info("Created {} academies", 2);

            // 2. 반 데이터 생성 (모두 수학반)
            AcademyClass class1 = new AcademyClass();
            class1.setName("고1 수학 기본반");
            class1.setAcademy(academy1);
            class1 = academyClassRepository.save(class1);

            AcademyClass class2 = new AcademyClass();
            class2.setName("고1 수학 심화반");
            class2.setAcademy(academy1);
            class2 = academyClassRepository.save(class2);

            AcademyClass class3 = new AcademyClass();
            class3.setName("고2 수학 기본반");
            class3.setAcademy(academy1);
            class3 = academyClassRepository.save(class3);

            AcademyClass class4 = new AcademyClass();
            class4.setName("고2 수학 심화반");
            class4.setAcademy(academy2);
            class4 = academyClassRepository.save(class4);

            AcademyClass class5 = new AcademyClass();
            class5.setName("고3 수학 정규반");
            class5.setAcademy(academy2);
            class5 = academyClassRepository.save(class5);

            AcademyClass class6 = new AcademyClass();
            class6.setName("고3 수학 특강반");
            class6.setAcademy(academy2);
            class6 = academyClassRepository.save(class6);

            log.info("Created {} classes", 6);

            // 3. 학생 데이터 생성
            Student student1 = new Student();
            student1.setName("김민준");
            student1.setGrade("고1");
            student1.setSchool("서울고등학교");
            student1.setAcademy(academy1);
            student1.setAcademyClass(class1);
            student1.setPin("1111");
            student1 = studentRepository.save(student1);

            Student student2 = new Student();
            student2.setName("이서연");
            student2.setGrade("고1");
            student2.setSchool("서울고등학교");
            student2.setAcademy(academy1);
            student2.setAcademyClass(class1);
            student2.setPin("2222");
            student2 = studentRepository.save(student2);

            Student student3 = new Student();
            student3.setName("박지훈");
            student3.setGrade("고1");
            student3.setSchool("강남고등학교");
            student3.setAcademy(academy1);
            student3.setAcademyClass(class2);
            student3.setPin("3333");
            student3 = studentRepository.save(student3);

            Student student4 = new Student();
            student4.setName("최유진");
            student4.setGrade("고2");
            student4.setSchool("강남고등학교");
            student4.setAcademy(academy1);
            student4.setAcademyClass(class3);
            student4.setPin("4444");
            student4 = studentRepository.save(student4);

            Student student5 = new Student();
            student5.setName("정도윤");
            student5.setGrade("고2");
            student5.setSchool("대치고등학교");
            student5.setAcademy(academy1);
            student5.setAcademyClass(class3);
            student5.setPin("5555");
            student5 = studentRepository.save(student5);

            Student student6 = new Student();
            student6.setName("강서아");
            student6.setGrade("고2");
            student6.setSchool("대치고등학교");
            student6.setAcademy(academy2);
            student6.setAcademyClass(class4);
            student6.setPin("6666");
            student6 = studentRepository.save(student6);

            Student student7 = new Student();
            student7.setName("윤하은");
            student7.setGrade("고3");
            student7.setSchool("서울고등학교");
            student7.setAcademy(academy2);
            student7.setAcademyClass(class5);
            student7.setPin("7777");
            student7 = studentRepository.save(student7);

            Student student8 = new Student();
            student8.setName("임준서");
            student8.setGrade("고3");
            student8.setSchool("강남고등학교");
            student8.setAcademy(academy2);
            student8.setAcademyClass(class5);
            student8.setPin("8888");
            student8 = studentRepository.save(student8);

            Student student9 = new Student();
            student9.setName("한지우");
            student9.setGrade("고3");
            student9.setSchool("대치고등학교");
            student9.setAcademy(academy2);
            student9.setAcademyClass(class6);
            student9.setPin("9999");
            student9 = studentRepository.save(student9);

            log.info("Created {} students", 9);

            // 4. 수업(Lesson) 데이터 생성
            Lesson lesson1 = new Lesson();
            lesson1.setLessonDate(LocalDate.now().minusDays(30));
            lesson1.setAcademy(academy1);
            lesson1.setAcademyClass(class1);
            lesson1.setCommonFeedback("오늘 수업에서는 1차 방정식과 2차 방정식의 기본 개념을 다루었습니다. 대부분의 학생들이 기본 개념은 잘 이해하고 있으나, 복잡한 문제에서 실수가 많았습니다. 다음 수업까지 복습 문제를 꼭 풀어오시기 바랍니다.");
            lesson1.setAnnouncement("다음 주 월요일은 임시 공휴일로 수업이 없습니다. 목요일에 만나요!");
            lesson1 = lessonRepository.save(lesson1);

            Lesson lesson2 = new Lesson();
            lesson2.setLessonDate(LocalDate.now().minusDays(28));
            lesson2.setAcademy(academy1);
            lesson2.setAcademyClass(class2);
            lesson2.setCommonFeedback("함수의 극한과 연속성에 대해 학습했습니다. 극한값 계산 시 L'Hospital 정리를 적절히 활용하는 연습이 필요합니다. 전반적으로 수업 참여도가 높았고, 질문도 활발했습니다.");
            lesson2.setAnnouncement("이번 주 토요일 오후 2시에 특강이 있습니다. 미적분 심화 내용을 다룰 예정이니 참석 부탁드립니다.");
            lesson2 = lessonRepository.save(lesson2);

            Lesson lesson3 = new Lesson();
            lesson3.setLessonDate(LocalDate.now().minusDays(25));
            lesson3.setAcademy(academy1);
            lesson3.setAcademyClass(class3);
            lesson3.setCommonFeedback("미분의 기본 개념과 공식을 배웠습니다. 미분 계수의 의미를 정확히 이해하는 것이 중요합니다. 일부 학생들이 곱의 미분법과 몫의 미분법을 헷갈려 했으니, 다음 시간에 추가 설명하겠습니다.");
            lesson3.setAnnouncement("중간고사가 2주 후로 다가왔습니다. 시험 범위는 1단원부터 3단원까지입니다.");
            lesson3 = lessonRepository.save(lesson3);

            Lesson lesson4 = new Lesson();
            lesson4.setLessonDate(LocalDate.now().minusDays(20));
            lesson4.setAcademy(academy2);
            lesson4.setAcademyClass(class4);
            lesson4.setCommonFeedback("정적분의 활용 문제를 풀었습니다. 넓이와 부피 계산 문제에서 적분 구간 설정이 중요합니다. 그래프를 그려서 문제를 시각화하는 습관을 들이세요. 대부분 학생들이 개념은 이해했으나 계산 실수가 있었습니다.");
            lesson4.setAnnouncement("다음 주부터 모의고사 대비 문제 풀이 수업을 진행합니다. 추가 프린트물을 나눠드릴 예정입니다.");
            lesson4 = lessonRepository.save(lesson4);

            Lesson lesson5 = new Lesson();
            lesson5.setLessonDate(LocalDate.now().minusDays(15));
            lesson5.setAcademy(academy2);
            lesson5.setAcademyClass(class5);
            lesson5.setCommonFeedback("확률분포와 통계적 추정 문제를 다루었습니다. 정규분포와 이항분포의 차이를 명확히 이해해야 합니다. 수능에서 자주 출제되는 유형이니 반복 학습이 필요합니다. 전체적으로 수업 집중도가 좋았습니다.");
            lesson5.setAnnouncement("수능이 한 달 앞으로 다가왔습니다. 마지막까지 최선을 다해주세요. 개별 상담이 필요한 학생은 언제든 연락주세요.");
            lesson5 = lessonRepository.save(lesson5);

            Lesson lesson6 = new Lesson();
            lesson6.setLessonDate(LocalDate.now().minusDays(10));
            lesson6.setAcademy(academy1);
            lesson6.setAcademyClass(class1);
            lesson6.setCommonFeedback("공간도형과 공간좌표에 대해 학습했습니다. 3차원 공간을 2차원으로 표현하는 것이 어려울 수 있으니, 다양한 각도에서 도형을 바라보는 연습이 필요합니다. 입체도형 모형을 활용한 학습을 권장합니다.");
            lesson6.setAnnouncement("학부모 상담 주간이 다가왔습니다. 상담 희망 시간을 문자로 보내주시기 바랍니다.");
            lesson6 = lessonRepository.save(lesson6);

            Lesson lesson7 = new Lesson();
            lesson7.setLessonDate(LocalDate.now().minusDays(7));
            lesson7.setAcademy(academy2);
            lesson7.setAcademyClass(class6);
            lesson7.setCommonFeedback("수능 기출 문제를 풀며 실전 감각을 익혔습니다. 시간 배분이 중요하니 모의고사 연습을 많이 해보세요. 오답 노트 작성은 필수입니다. 여러분의 실력이 많이 향상되었습니다. 자신감을 가지세요!");
            lesson7.setAnnouncement("파이널 모의고사가 이번 주 토요일 오전 9시에 있습니다. 실전처럼 준비해 오세요.");
            lesson7 = lessonRepository.save(lesson7);

            Lesson lesson8 = new Lesson();
            lesson8.setLessonDate(LocalDate.now().minusDays(3));
            lesson8.setAcademy(academy1);
            lesson8.setAcademyClass(class2);
            lesson8.setCommonFeedback("삼각함수의 덧셈정리와 배각공식을 다루었습니다. 공식을 단순히 암기하기보다는 유도 과정을 이해하는 것이 중요합니다. 다음 수업에서는 삼각함수의 그래프 변환을 배울 예정입니다.");
            lesson8.setAnnouncement("교재 추가 구매가 필요한 학생은 다음 수업 전까지 말씀해 주세요.");
            lesson8 = lessonRepository.save(lesson8);

            log.info("Created {} lessons", 8);

            // 5. 시험 데이터 생성 (Lesson과 연결) - 모두 수학 시험
            Test test1 = new Test();
            test1.setTitle("고1 수학 1학기 중간고사 대비");
            test1.setAcademy(academy1);
            test1.setAcademyClass(class1);
            test1.setLesson(lesson1);
            test1 = testRepository.save(test1);

            Test test2 = new Test();
            test2.setTitle("고1 수학 심화 모의고사 1회");
            test2.setAcademy(academy1);
            test2.setAcademyClass(class2);
            test2.setLesson(lesson2);
            test2 = testRepository.save(test2);

            Test test3 = new Test();
            test3.setTitle("고2 수학 평가원 모의고사");
            test3.setAcademy(academy1);
            test3.setAcademyClass(class3);
            test3.setLesson(lesson3);
            test3 = testRepository.save(test3);

            Test test4 = new Test();
            test4.setTitle("고2 수학 교육청 모의고사");
            test4.setAcademy(academy2);
            test4.setAcademyClass(class4);
            test4.setLesson(lesson4);
            test4 = testRepository.save(test4);

            Test test5 = new Test();
            test5.setTitle("고3 수학 수능 대비 파이널");
            test5.setAcademy(academy2);
            test5.setAcademyClass(class5);
            test5.setLesson(lesson5);
            test5 = testRepository.save(test5);

            log.info("Created {} tests", 5);

            // 시험 문제 생성 (test1 - 10문제)
            List<TestQuestion> test1Questions = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                TestQuestion question = new TestQuestion();
                question.setTest(test1);
                question.setNumber(i);
                question.setAnswer(String.valueOf((i % 5) + 1));
                question.setPoints(10.0);
                test1Questions.add(testQuestionRepository.save(question));
            }

            // 시험 문제 생성 (test2 - 15문제)
            List<TestQuestion> test2Questions = new ArrayList<>();
            for (int i = 1; i <= 15; i++) {
                TestQuestion question = new TestQuestion();
                question.setTest(test2);
                question.setNumber(i);
                question.setAnswer(String.valueOf((i % 5) + 1));
                question.setPoints(Math.round((100.0 / 15) * 10) / 10.0);
                test2Questions.add(testQuestionRepository.save(question));
            }

            // 시험 문제 생성 (test3 - 20문제)
            List<TestQuestion> test3Questions = new ArrayList<>();
            for (int i = 1; i <= 20; i++) {
                TestQuestion question = new TestQuestion();
                question.setTest(test3);
                question.setNumber(i);
                question.setAnswer(String.valueOf((i % 5) + 1));
                question.setPoints(5.0);
                test3Questions.add(testQuestionRepository.save(question));
            }

            // 시험 문제 생성 (test4 - 12문제)
            List<TestQuestion> test4Questions = new ArrayList<>();
            for (int i = 1; i <= 12; i++) {
                TestQuestion question = new TestQuestion();
                question.setTest(test4);
                question.setNumber(i);
                question.setAnswer(String.valueOf((i % 5) + 1));
                question.setPoints(Math.round((100.0 / 12) * 10) / 10.0);
                test4Questions.add(testQuestionRepository.save(question));
            }

            // 시험 문제 생성 (test5 - 25문제)
            List<TestQuestion> test5Questions = new ArrayList<>();
            for (int i = 1; i <= 25; i++) {
                TestQuestion question = new TestQuestion();
                question.setTest(test5);
                question.setNumber(i);
                question.setAnswer(String.valueOf((i % 5) + 1));
                question.setPoints(4.0);
                test5Questions.add(testQuestionRepository.save(question));
            }

            log.info("Created {} test questions", test1Questions.size() + test2Questions.size() +
                    test3Questions.size() + test4Questions.size() + test5Questions.size());

            // 학생 답안 제출 생성
            // student1의 test1 제출 (80점)
            StudentSubmission submission1 = new StudentSubmission();
            submission1.setStudent(student1);
            submission1.setTest(test1);
            submission1.setTotalScore(80);
            submission1.setSubmittedAt(LocalDateTime.now().minusDays(30));
            submission1 = studentSubmissionRepository.save(submission1);

            for (int i = 0; i < test1Questions.size(); i++) {
                StudentSubmissionDetail detail = new StudentSubmissionDetail();
                detail.setSubmission(submission1);
                detail.setQuestion(test1Questions.get(i));
                String correctAnswer = test1Questions.get(i).getAnswer();
                String studentAnswer = (i < 8) ? correctAnswer : String.valueOf((Integer.parseInt(correctAnswer) % 5) + 1);
                detail.setStudentAnswer(studentAnswer);
                detail.setIsCorrect(correctAnswer.equals(studentAnswer));
                studentSubmissionDetailRepository.save(detail);
            }

            // student2의 test1 제출 (90점)
            StudentSubmission submission2 = new StudentSubmission();
            submission2.setStudent(student2);
            submission2.setTest(test1);
            submission2.setTotalScore(90);
            submission2.setSubmittedAt(LocalDateTime.now().minusDays(30));
            submission2 = studentSubmissionRepository.save(submission2);

            for (int i = 0; i < test1Questions.size(); i++) {
                StudentSubmissionDetail detail = new StudentSubmissionDetail();
                detail.setSubmission(submission2);
                detail.setQuestion(test1Questions.get(i));
                String correctAnswer = test1Questions.get(i).getAnswer();
                String studentAnswer = (i < 9) ? correctAnswer : "5";
                detail.setStudentAnswer(studentAnswer);
                detail.setIsCorrect(correctAnswer.equals(studentAnswer));
                studentSubmissionDetailRepository.save(detail);
            }

            // student3의 test2 제출 (87점)
            StudentSubmission submission3 = new StudentSubmission();
            submission3.setStudent(student3);
            submission3.setTest(test2);
            submission3.setTotalScore(87);
            submission3.setSubmittedAt(LocalDateTime.now().minusDays(28));
            submission3 = studentSubmissionRepository.save(submission3);

            for (int i = 0; i < test2Questions.size(); i++) {
                StudentSubmissionDetail detail = new StudentSubmissionDetail();
                detail.setSubmission(submission3);
                detail.setQuestion(test2Questions.get(i));
                String correctAnswer = test2Questions.get(i).getAnswer();
                String studentAnswer = (i < 13) ? correctAnswer : "3";
                detail.setStudentAnswer(studentAnswer);
                detail.setIsCorrect(correctAnswer.equals(studentAnswer));
                studentSubmissionDetailRepository.save(detail);
            }

            // student4의 test3 제출 (75점)
            StudentSubmission submission4 = new StudentSubmission();
            submission4.setStudent(student4);
            submission4.setTest(test3);
            submission4.setTotalScore(75);
            submission4.setSubmittedAt(LocalDateTime.now().minusDays(25));
            submission4 = studentSubmissionRepository.save(submission4);

            for (int i = 0; i < test3Questions.size(); i++) {
                StudentSubmissionDetail detail = new StudentSubmissionDetail();
                detail.setSubmission(submission4);
                detail.setQuestion(test3Questions.get(i));
                String correctAnswer = test3Questions.get(i).getAnswer();
                String studentAnswer = (i < 15) ? correctAnswer : "2";
                detail.setStudentAnswer(studentAnswer);
                detail.setIsCorrect(correctAnswer.equals(studentAnswer));
                studentSubmissionDetailRepository.save(detail);
            }

            // student5의 test3 제출 (85점)
            StudentSubmission submission5 = new StudentSubmission();
            submission5.setStudent(student5);
            submission5.setTest(test3);
            submission5.setTotalScore(85);
            submission5.setSubmittedAt(LocalDateTime.now().minusDays(25));
            submission5 = studentSubmissionRepository.save(submission5);

            for (int i = 0; i < test3Questions.size(); i++) {
                StudentSubmissionDetail detail = new StudentSubmissionDetail();
                detail.setSubmission(submission5);
                detail.setQuestion(test3Questions.get(i));
                String correctAnswer = test3Questions.get(i).getAnswer();
                String studentAnswer = (i < 17) ? correctAnswer : "4";
                detail.setStudentAnswer(studentAnswer);
                detail.setIsCorrect(correctAnswer.equals(studentAnswer));
                studentSubmissionDetailRepository.save(detail);
            }

            // student6의 test4 제출 (92점)
            StudentSubmission submission6 = new StudentSubmission();
            submission6.setStudent(student6);
            submission6.setTest(test4);
            submission6.setTotalScore(92);
            submission6.setSubmittedAt(LocalDateTime.now().minusDays(20));
            submission6 = studentSubmissionRepository.save(submission6);

            for (int i = 0; i < test4Questions.size(); i++) {
                StudentSubmissionDetail detail = new StudentSubmissionDetail();
                detail.setSubmission(submission6);
                detail.setQuestion(test4Questions.get(i));
                String correctAnswer = test4Questions.get(i).getAnswer();
                String studentAnswer = (i < 11) ? correctAnswer : "1";
                detail.setStudentAnswer(studentAnswer);
                detail.setIsCorrect(correctAnswer.equals(studentAnswer));
                studentSubmissionDetailRepository.save(detail);
            }

            // student7의 test5 제출 (88점)
            StudentSubmission submission7 = new StudentSubmission();
            submission7.setStudent(student7);
            submission7.setTest(test5);
            submission7.setTotalScore(88);
            submission7.setSubmittedAt(LocalDateTime.now().minusDays(15));
            submission7 = studentSubmissionRepository.save(submission7);

            for (int i = 0; i < test5Questions.size(); i++) {
                StudentSubmissionDetail detail = new StudentSubmissionDetail();
                detail.setSubmission(submission7);
                detail.setQuestion(test5Questions.get(i));
                String correctAnswer = test5Questions.get(i).getAnswer();
                String studentAnswer = (i < 22) ? correctAnswer : "3";
                detail.setStudentAnswer(studentAnswer);
                detail.setIsCorrect(correctAnswer.equals(studentAnswer));
                studentSubmissionDetailRepository.save(detail);
            }

            // student8의 test5 제출 (96점)
            StudentSubmission submission8 = new StudentSubmission();
            submission8.setStudent(student8);
            submission8.setTest(test5);
            submission8.setTotalScore(96);
            submission8.setSubmittedAt(LocalDateTime.now().minusDays(15));
            submission8 = studentSubmissionRepository.save(submission8);

            for (int i = 0; i < test5Questions.size(); i++) {
                StudentSubmissionDetail detail = new StudentSubmissionDetail();
                detail.setSubmission(submission8);
                detail.setQuestion(test5Questions.get(i));
                String correctAnswer = test5Questions.get(i).getAnswer();
                String studentAnswer = (i < 24) ? correctAnswer : "2";
                detail.setStudentAnswer(studentAnswer);
                detail.setIsCorrect(correctAnswer.equals(studentAnswer));
                studentSubmissionDetailRepository.save(detail);
            }

            log.info("Created {} student submissions with details", 8);

            // 6. 숙제 데이터 생성 (Lesson과 연결) - 모두 수학 숙제
            // Lesson1에 여러 숙제 추가 (학생별로 다른 숙제 할당 예시)
            Homework homework1 = new Homework();
            homework1.setTitle("방정식 기본 문제");
            homework1.setQuestionCount(20);
            homework1.setMemo("1차 방정식과 2차 방정식 기본 문제입니다.");
            homework1.setDueDate(LocalDate.now().plusDays(7));
            homework1.setAcademy(academy1);
            homework1.setAcademyClass(class1);
            homework1.setLesson(lesson1);
            homeworkRepository.save(homework1);

            Homework homework1a = new Homework();
            homework1a.setTitle("방정식 심화 문제");
            homework1a.setQuestionCount(25);
            homework1a.setMemo("1차/2차 방정식 심화 문제입니다.");
            homework1a.setDueDate(LocalDate.now().plusDays(7));
            homework1a.setAcademy(academy1);
            homework1a.setAcademyClass(class1);
            homework1a.setLesson(lesson1); // 같은 수업에 연결
            homeworkRepository.save(homework1a);

            Homework homework1b = new Homework();
            homework1b.setTitle("방정식 복습 문제");
            homework1b.setQuestionCount(15);
            homework1b.setMemo("지난 주 배운 내용 복습 문제입니다.");
            homework1b.setDueDate(LocalDate.now().plusDays(7));
            homework1b.setAcademy(academy1);
            homework1b.setAcademyClass(class1);
            homework1b.setLesson(lesson1); // 같은 수업에 연결
            homeworkRepository.save(homework1b);

            Homework homework2 = new Homework();
            homework2.setTitle("함수의 극한과 연속성");
            homework2.setQuestionCount(25);
            homework2.setMemo("극한값 계산 문제입니다. 개념 정리 노트와 함께 제출하세요.");
            homework2.setDueDate(LocalDate.now().plusDays(5));
            homework2.setAcademy(academy1);
            homework2.setAcademyClass(class2);
            homework2.setLesson(lesson2);
            homeworkRepository.save(homework2);

            Homework homework3 = new Homework();
            homework3.setTitle("미분 기본 문제");
            homework3.setQuestionCount(40);
            homework3.setMemo("다항함수의 미분 문제입니다. 과정을 자세히 적어주세요.");
            homework3.setDueDate(LocalDate.now().plusDays(6));
            homework3.setAcademy(academy1);
            homework3.setAcademyClass(class3);
            homework3.setLesson(lesson3);
            homeworkRepository.save(homework3);

            Homework homework4 = new Homework();
            homework4.setTitle("적분의 활용");
            homework4.setQuestionCount(35);
            homework4.setMemo("정적분의 활용 문제입니다. 넓이와 부피 계산 문제를 풀어오세요.");
            homework4.setDueDate(LocalDate.now().plusDays(4));
            homework4.setAcademy(academy2);
            homework4.setAcademyClass(class4);
            homework4.setLesson(lesson4);
            homeworkRepository.save(homework4);

            Homework homework5 = new Homework();
            homework5.setTitle("확률과 통계 심화");
            homework5.setQuestionCount(50);
            homework5.setMemo("확률분포와 통계적 추정 문제입니다. 이번 주 금요일까지 완료.");
            homework5.setDueDate(LocalDate.now().plusDays(3));
            homework5.setAcademy(academy2);
            homework5.setAcademyClass(class5);
            homework5.setLesson(lesson5);
            homeworkRepository.save(homework5);

            Homework homework6 = new Homework();
            homework6.setTitle("기하와 벡터");
            homework6.setQuestionCount(45);
            homework6.setMemo("공간도형과 공간좌표 문제입니다. 그림과 함께 풀이를 제출하세요.");
            homework6.setDueDate(LocalDate.now().plusDays(8));
            homework6.setAcademy(academy1);
            homework6.setAcademyClass(class1);
            homework6.setLesson(lesson6);
            homeworkRepository.save(homework6);

            Homework homework7 = new Homework();
            homework7.setTitle("수능 대비 종합 문제");
            homework7.setQuestionCount(60);
            homework7.setMemo("수능 기출 문제 모음입니다. 오답 노트 작성 필수.");
            homework7.setDueDate(LocalDate.now().plusDays(10));
            homework7.setAcademy(academy2);
            homework7.setAcademyClass(class6);
            homework7.setLesson(lesson7);
            homeworkRepository.save(homework7);

            log.info("Created {} homeworks", 9); // homework1, 1a, 1b + 6개 = 9개

            // 7. 학생-숙제 할당 및 완성도 데이터 생성
            // Lesson1(class1) 학생들 - 각각 다른 숙제 할당
            StudentHomework sh1 = new StudentHomework();
            sh1.setStudent(student1); // 김민준
            sh1.setHomework(homework1); // 기본 문제
            sh1.setIncorrectCount(3); // 20문제 중 3개 오답 (85% 정답률)
            studentHomeworkRepository.save(sh1);

            StudentHomework sh2 = new StudentHomework();
            sh2.setStudent(student2); // 이서연
            sh2.setHomework(homework1a); // 심화 문제
            sh2.setIncorrectCount(5); // 25문제 중 5개 오답 (80% 정답률)
            studentHomeworkRepository.save(sh2);

            StudentHomework sh3 = new StudentHomework();
            sh3.setStudent(student3); // 박지호
            sh3.setHomework(homework1b); // 복습 문제
            sh3.setIncorrectCount(1); // 15문제 중 1개 오답 (93% 정답률)
            studentHomeworkRepository.save(sh3);

            // Lesson6(class1) student1 - 다른 수업의 숙제
            StudentHomework sh_lesson6 = new StudentHomework();
            sh_lesson6.setStudent(student1);
            sh_lesson6.setHomework(homework6);
            sh_lesson6.setIncorrectCount(14); // 45문제 중 14개 오답 (69% 정답률)
            studentHomeworkRepository.save(sh_lesson6);

            StudentHomework sh4 = new StudentHomework();
            sh4.setStudent(student3);
            sh4.setHomework(homework2);
            sh4.setIncorrectCount(3); // 25문제 중 3개 오답 (88% 정답률)
            studentHomeworkRepository.save(sh4);

            StudentHomework sh5 = new StudentHomework();
            sh5.setStudent(student4);
            sh5.setHomework(homework3);
            sh5.setIncorrectCount(10); // 40문제 중 10개 오답 (75% 정답률)
            studentHomeworkRepository.save(sh5);

            StudentHomework sh6 = new StudentHomework();
            sh6.setStudent(student5);
            sh6.setHomework(homework3);
            sh6.setIncorrectCount(5); // 40문제 중 5개 오답 (88% 정답률)
            studentHomeworkRepository.save(sh6);

            StudentHomework sh7 = new StudentHomework();
            sh7.setStudent(student6);
            sh7.setHomework(homework4);
            sh7.setIncorrectCount(3); // 35문제 중 3개 오답 (91% 정답률)
            studentHomeworkRepository.save(sh7);

            StudentHomework sh8 = new StudentHomework();
            sh8.setStudent(student7);
            sh8.setHomework(homework5);
            sh8.setIncorrectCount(10); // 50문제 중 10개 오답 (80% 정답률)
            studentHomeworkRepository.save(sh8);

            StudentHomework sh9 = new StudentHomework();
            sh9.setStudent(student8);
            sh9.setHomework(homework5);
            sh9.setIncorrectCount(1); // 50문제 중 1개 오답 (98% 정답률)
            studentHomeworkRepository.save(sh9);

            StudentHomework sh10 = new StudentHomework();
            sh10.setStudent(student9);
            sh10.setHomework(homework7);
            sh10.setIncorrectCount(21); // 60문제 중 21개 오답 (65% 정답률)
            studentHomeworkRepository.save(sh10);

            log.info("Created {} student homework records", 10);

            log.info("Sample data initialization completed successfully!");
            log.info("===================================================");
            log.info("Summary:");
            log.info("- Teachers: 1");
            log.info("- Academies: 2 (수학 전문)");
            log.info("- Classes: 6 (모두 수학반)");
            log.info("- Students: 9");
            log.info("- Lessons: 8");
            log.info("- Tests: 5 (모두 수학 시험)");
            log.info("- Test Questions: 82");
            log.info("- Student Submissions: 8");
            log.info("- Homeworks: 9 (모두 수학 숙제, lesson1에 3개 숙제 등록 예시)");
            log.info("- Student Homework Records: 11 (학생별 다른 숙제 할당 예시)");
            log.info("===================================================");
        };
    }
}
