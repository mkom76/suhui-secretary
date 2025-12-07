# 학원 학생 성적 관리 시스템 - 개발 로그

## 프로젝트 개요
학원에서 학생들의 성적을 관리하는 웹 애플리케이션

**기술 스택**
- Backend: Spring Boot 3.x, SQLite, JPA
- Frontend: Vue 3, Element Plus, Vite
- 상태관리: Pinia

## 데이터베이스 구조

### 엔티티
1. **Student** (학생)
   - id, name, grade (학년), school (학교명)

2. **Test** (시험)
   - id, title, createdAt

3. **TestQuestion** (시험 문제)
   - id, test, number (문제 번호), answer (정답)

4. **StudentSubmission** (학생 답안 제출)
   - id, student, test, totalScore

5. **StudentSubmissionDetail** (답안 상세)
   - id, submission, question, studentAnswer, isCorrect

6. **TeacherFeedback** (선생님 피드백)
   - id, submission, teacherName, content, createdAt

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
   - grade, school 필드 사용

2. **API 호출**
   - 문제 삭제: `/api/questions/{id}`
   - 시험별 제출: `/api/submissions/test/{testId}`
   - 시험별 피드백: `/api/feedback/test/{testId}`

3. **프론트엔드 컴포넌트**
   - 아이콘 사용 시 `<el-icon>` 래퍼로 감싸기
   - Element Plus 아이콘은 전역 등록됨 (main.ts)

4. **정답 관리 UX**
   - 일괄 추가 후 인라인 편집 방식
   - 미저장 문제는 클라이언트 측에서만 관리

## 향후 개선 사항

- [ ] 학생 제출 기능 구현
- [ ] 문제별 통계 시각화
- [ ] 엑셀 내보내기 기능
- [ ] 학생/시험 검색 필터 개선
- [ ] 대시보드 통계 페이지
- [ ] 사용자 인증/권한 관리

## 참고

- SQLite 파일: `./academy.db`
- 백엔드는 Spring Boot 3.x 사용
- 프론트엔드는 Vue 3 Composition API 사용
- Element Plus UI 라이브러리 사용
