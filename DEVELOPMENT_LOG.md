# 학원 학생 성적 관리 시스템 - 개발 로그

## 프로젝트 개요
학원에서 학생들의 성적을 관리하는 웹 애플리케이션

**기술 스택**
- Backend: Spring Boot 3.x, MySQL, JPA/Hibernate
- Frontend: Vue 3, TypeScript, Element Plus, Vite
- 상태관리: Pinia
- 인증: 없음 (향후 구현 예정)

## 데이터베이스 구조

### 엔티티
1. **Student** (학생)
   - id, name, grade (학년), school (학교명), academy (학원명)
   - createdAt, updatedAt (JPA Auditing)

2. **Test** (시험)
   - id, title
   - createdAt, updatedAt (JPA Auditing)

3. **TestQuestion** (시험 문제)
   - id, test, number (문제 번호), answer (정답), points (배점)
   - createdAt, updatedAt (JPA Auditing)

4. **StudentSubmission** (학생 답안 제출)
   - id, student, test, totalScore, submittedAt (응시일)
   - createdAt, updatedAt (JPA Auditing)

5. **StudentSubmissionDetail** (답안 상세)
   - id, submission, question, studentAnswer, isCorrect
   - createdAt, updatedAt (JPA Auditing)

6. **TeacherFeedback** (선생님 피드백)
   - id, submission, teacherName, content
   - createdAt, updatedAt (JPA Auditing)

## API 엔드포인트

### 학생 관리 (`/api/students`)
- `GET /api/students` - 학생 목록 조회
- `GET /api/students/{id}` - 학생 상세 조회
- `POST /api/students` - 학생 등록
- `PUT /api/students/{id}` - 학생 정보 수정
- `DELETE /api/students/{id}` - 학생 삭제

### 시험 관리 (`/api/tests`)
- `GET /api/tests` - 시험 목록 조회
- `GET /api/tests/{id}` - 시험 상세 조회
- `POST /api/tests` - 시험 생성
- `PUT /api/tests/{id}` - 시험 정보 수정
- `DELETE /api/tests/{id}` - 시험 삭제
- `GET /api/tests/{id}/stats` - 시험 통계 조회
- `GET /api/tests/{id}/questions` - 시험 문제 목록 조회
- `POST /api/tests/{id}/questions` - 문제 추가
- `PUT /api/tests/{id}/answers` - 정답 일괄 입력

### 문제 관리 (`/api/questions`)
- `DELETE /api/questions/{id}` - 문제 삭제

### 제출 관리 (`/api/submissions`)
- `POST /api/submissions` - 답안 제출
- `GET /api/submissions/{id}` - 제출 상세 조회
- `GET /api/submissions/test/{testId}` - 시험별 제출 목록
- `GET /api/submissions/student/{studentId}` - 학생별 제출 목록
- `GET /api/submissions/{id}/feedback` - 제출별 피드백 조회

### 피드백 관리 (`/api/feedback`)
- `GET /api/feedback/test/{testId}` - 시험별 피드백 조회
- `GET /api/feedback/student/{studentId}` - 학생별 피드백 조회
- `POST /api/feedback` - 피드백 생성
- `PUT /api/feedback/{id}` - 피드백 수정
- `DELETE /api/feedback/{id}` - 피드백 삭제

## DTO 구조

### TestDto
```java
{
  id: Long
  title: String
  createdAt: LocalDateTime
  questionCount: Integer  // 실제 문제 수 계산
}
```

### StudentDto
```java
{
  id: Long
  name: String
  grade: String     // 학년 (예: "중1", "고2")
  school: String    // 학교명
  academy: String   // 학원명
  createdAt: LocalDateTime
  updatedAt: LocalDateTime
}
```

### TestQuestionDto
```java
{
  id: Long
  number: Integer
  answer: String
  points: Double    // 배점
  createdAt: LocalDateTime
  updatedAt: LocalDateTime
}
```

### StudentSubmissionDto
```java
{
  id: Long
  student: StudentDto      // 학생 객체 포함
  testId: Long
  testTitle: String
  totalScore: Integer
  submittedAt: LocalDateTime  // 응시일
  createdAt: LocalDateTime
  updatedAt: LocalDateTime
  details: List<SubmissionDetailDto>
}
```

### TeacherFeedbackDto
```java
{
  id: Long
  submissionId: Long
  submission: StudentSubmissionDto  // 제출 객체 포함
  teacherName: String
  content: String
  createdAt: LocalDateTime
  updatedAt: LocalDateTime
}
```

### TestStatsDto
```java
{
  testId: Long
  testTitle: String
  averageScore: Double
  maxScore: Integer         // 최고 점수
  studentScores: List<StudentScore>
  questionStats: List<QuestionStat>
}
```

## 주요 개발 작업 내역

### 2025-12-08 작업 내용

#### 1. 학생 엔티티에 학원 필드 추가
- **Student 엔티티**: `academy` 필드 추가 (학원명)
- **영향 범위**:
  - Backend: Student.java, StudentDto.java
  - Frontend: client.ts, StudentsView.vue, StudentDetailView.vue
  - Database: 자동 스키마 생성 (JPA)
  - 샘플 데이터: DataInitializer.java 업데이트

#### 2. 시험 문제에 배점 시스템 추가
- **TestQuestion 엔티티**: `points` 필드 추가 (Double 타입)
- **자동 배점 계산**:
  - 100점을 문제 수로 나누어 균등 배분
  - 소수점 1자리까지 표시
  - 나머지 점수는 마지막 문제에 할당
- **영향 범위**:
  - Backend: TestQuestion.java, TestQuestionDto.java, TestService.java
  - Frontend: Question 인터페이스, TestAnswersView.vue, TestsView.vue
  - UI: 배점 컬럼 추가 (el-input-number, 0.1 step)

#### 3. 정답 관리 페이지 UX 개선
- **문제 추가 방식 변경**:
  - 기존: 일괄 추가 다이얼로그
  - 변경: 단일 문제 추가 버튼
- **저장 방식 개선**:
  - 기존: 인라인 개별 저장
  - 변경: 하단 "전체 저장" 버튼으로 일괄 저장
  - 변경사항 추적 기능 (hasChanges)
- **드래그 순서 변경 제거**:
  - 모든 순서 조정 UI 제거 (드래그, 화살표 버튼)
  - 문제는 추가 순서대로 자동 번호 부여

#### 4. 시험 생성 시 문제 수 지정
- **시험 생성 다이얼로그**:
  - 문제 수 입력 필드 추가 (1~100)
  - 빈 정답의 문제 자동 생성
  - 배점 자동 계산
- **생성 후 동작**:
  - 정답 관리 페이지로 자동 리다이렉트
  - 바로 정답 입력 가능

#### 5. JPA Auditing 도입 (타임스탬프 자동 관리)
- **모든 엔티티에 추가**:
  - `createdAt`: 생성 시간 (자동)
  - `updatedAt`: 수정 시간 (자동)
- **StudentSubmission 특별 필드**:
  - `submittedAt`: 시험 응시 시간 (수동 설정)
- **구현**:
  - JpaAuditingConfig.java 생성
  - @EntityListeners(AuditingEntityListener.class) 추가
  - @CreatedDate, @LastModifiedDate 어노테이션 사용
- **주의사항**:
  - nullable 제약조건 제거 (JPA Auditing 호환성)
  - updatable = false 설정 (createdAt만)

#### 6. 학생 상세 페이지 생성
- **라우팅**: `/students/:id`
- **페이지 구성**:
  - 학생 정보 카드 (이름, 학년, 학교, 학원)
  - 통계 카드 (총 응시 시험, 평균, 최고, 최저 점수)
  - 점수 추이 그래프 (꺾은선 그래프)
  - 응시한 시험 목록 테이블
  - 선생님 피드백 타임라인
- **그래프 기능**:
  - 학생 점수 vs 학원 평균 비교
  - SVG 기반 커스텀 그래프
  - 시간순 정렬 (submittedAt 기준)

#### 7. 인터랙티브 차트 모달 추가
- **모달 기능**:
  - 차트 클릭 또는 "확대하여 보기" 버튼으로 열기
  - 90% 너비, 600px 높이의 큰 차트
- **상호작용 기능**:
  - **확대/축소**: 마우스 휠 (50%~500%)
  - **드래그 이동**: 클릭 & 드래그로 차트 이동
  - **초기화**: 원래 크기 및 위치 복원
- **UI 개선**:
  - 현재 확대 비율 표시
  - 드래그 중 커서 변경 (grab → grabbing)
  - 사용 방법 안내 표시
  - 차트 프리뷰 호버 효과

#### 8. 프론트엔드 버그 수정
- **StudentDetailView.vue JavaScript 파싱 에러 해결**:
  - 문제: SVG 템플릿의 복잡한 산술 표현식
  - 해결: 모든 좌표 계산을 computed property로 이동
  - 템플릿에서는 단순 속성 접근만 사용 (`:cx="point.x"`)

#### 9. ERD 업데이트
- 모든 테이블에 created_at, updated_at 추가
- STUDENTS 테이블에 academy 추가
- TEST_QUESTIONS 테이블에 points 추가
- STUDENT_SUBMISSIONS 테이블에 submitted_at 추가
- 자동 타임스탬프 규칙 문서화

### 2024-12-03 작업 내용

#### 1. 학생 정보 필드 변경
- **변경 전**: email, phone
- **변경 후**: grade (학년), school (학교명)
- 영향 범위:
  - Backend: Student 엔티티, StudentDto
  - Frontend: StudentsView.vue, TestDetailView.vue, client.ts

#### 2. UI/UX 개선

##### 학생 관리 페이지
- 작업 버튼을 아이콘으로 변경
  - 수정: 파란색 원형 버튼 + 펜 아이콘
  - 삭제: 빨간색 원형 버튼 + 휴지통 아이콘

##### 시험 관리 페이지
- 시험명 클릭 시 상세보기 이동 (링크 스타일)
- 작업 버튼을 아이콘으로 변경
  - 정답 관리: 노란색 원형 + 펜 아이콘
  - 피드백: 회색 원형 + 말풍선 아이콘
  - 수정: 파란색 원형 + 펜 아이콘
  - 삭제: 빨간색 원형 + 휴지통 아이콘
- 설명 컬럼 제거 (상세 페이지에서만 표시)
- 문제 수를 실제 등록된 문제 개수로 표시

##### 시험 상세보기 페이지
- 시험 정보 섹션 개선
- 통계: 응시자 수, 평균 점수, 최고 점수
- 학생 정보: email/phone → school/grade로 변경

##### 피드백 관리 페이지
- 수정 기능 추가
  - 수정 버튼: 파란색 원형 + 펜 아이콘
  - 삭제 버튼: 빨간색 원형 + 휴지통 아이콘
- 수정 모드에서는 학생 선택 비활성화

##### 정답 관리 페이지 (대대적 개선)
- **기존**: 개별 문제 추가 폼
- **변경**: n개 문제 일괄 추가
  - "문제 추가" 버튼 클릭 → 다이얼로그
  - 추가할 문제 수 입력 (1~100개)
  - 빈 문제가 테이블에 추가됨
- 인라인 편집 기능
  - 정답 컬럼에서 직접 입력
  - blur 또는 Enter 시 자동 저장
- 상태 표시
  - 저장됨: 초록색 태그
  - 미저장: 노란색 태그
- 삭제 버튼: 빨간색 원형 + 휴지통 아이콘

#### 3. Backend API 추가

##### TestController
- `POST /api/tests/{id}/questions` - 개별 문제 추가
- TestDto에 `questionCount` 필드 추가

##### QuestionController (신규)
- `DELETE /api/questions/{id}` - 문제 삭제

##### SubmissionController
- `GET /api/submissions/test/{testId}` - 시험별 제출 목록

##### FeedbackController
- `GET /api/feedback/test/{testId}` - 시험별 피드백 조회
- `POST /api/feedback` - 피드백 생성

##### TeacherFeedbackRepository
- `findBySubmissionTestId(Long testId)` 메서드 추가

#### 4. DTO 구조 개선
- `StudentSubmissionDto`: student 객체 포함
- `TeacherFeedbackDto`: submission 객체 포함
- `TestStatsDto`: maxScore 필드 추가
- `TestDto`: questionCount 필드 추가 (실제 문제 수 계산)

## 프론트엔드 라우팅

```
/ - 홈
/students - 학생 관리
/students/:id - 학생 상세 (성적 추이, 피드백 등)
/tests - 시험 관리
/tests/:id - 시험 상세보기
/tests/:id/answers - 정답 관리
/tests/:id/feedback - 피드백 관리
```

## 실행 방법

### Backend
```bash
./gradlew bootRun
```
서버: http://localhost:8080

### Frontend
```bash
cd frontend
npm install
npm run dev
```
서버: http://localhost:5173

## 주의사항

1. **데이터 구조 변경**
   - 학생 정보에 email, phone 필드 사용 금지
   - grade, school, academy 필드 사용
   - 모든 엔티티에 createdAt, updatedAt 자동 관리

2. **JPA Auditing**
   - @EntityListeners(AuditingEntityListener.class) 필수
   - createdAt: updatable = false 설정
   - nullable 제약조건 사용 안 함

3. **배점 시스템**
   - TestQuestion의 points는 Double 타입
   - 자동 계산: 100 / 문제 수 (소수점 1자리)
   - 나머지는 마지막 문제에 할당

4. **API 호출**
   - 문제 삭제: `/api/questions/{id}`
   - 시험별 제출: `/api/submissions/test/{testId}`
   - 학생별 제출: `/api/submissions/student/{studentId}`
   - 시험별 피드백: `/api/feedback/test/{testId}`
   - 학생별 피드백: `/api/feedback/student/{studentId}`

5. **프론트엔드 컴포넌트**
   - 아이콘 사용 시 `<el-icon>` 래퍼로 감싸기
   - Element Plus 아이콘은 전역 등록됨 (main.ts)
   - SVG 템플릿에서 복잡한 JavaScript 표현식 사용 금지
   - 모든 계산은 computed property로 처리

6. **차트 기능**
   - SVG 기반 커스텀 차트
   - 모달에서 확대/축소 및 드래그 이동 지원
   - 좌표는 모두 computed property로 사전 계산

## 향후 개선 사항

- [ ] 학생 제출 기능 구현
- [ ] 문제별 통계 시각화
- [ ] 엑셀 내보내기 기능
- [ ] 학생/시험 검색 필터 개선
- [ ] 대시보드 통계 페이지
- [ ] 사용자 인증/권한 관리

## 참고

- 데이터베이스: MySQL (localhost:3306/testdb)
- 백엔드는 Spring Boot 3.x 사용
- JPA ddl-auto: create-drop (개발 환경)
- 프론트엔드는 Vue 3 Composition API + TypeScript
- Element Plus UI 라이브러리 사용
- 샘플 데이터는 DataInitializer.java에서 자동 생성

## 데이터베이스 설정

### application.yml
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/testdb
    username: root
    password: 1234
  jpa:
    hibernate:
      ddl-auto: create-drop  # 개발용 - 시작 시 테이블 재생성
```

### 프로필
- `local`: 로컬 개발 환경 (기본값)
- `prod`: 운영 환경 (DataInitializer 비활성화)
