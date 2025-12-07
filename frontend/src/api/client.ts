import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const client = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Types
interface Student {
  id?: number;
  name: string;
  grade: string;
  school: string;
  academy: string;
  createdAt?: string;
  updatedAt?: string;
}

interface Test {
  id?: number;
  title: string;
  questionCount?: number;
  createdAt?: string;
  updatedAt?: string;
}

interface Question {
  id?: number;
  number: number;
  answer: string;
  points: number;
  createdAt?: string;
  updatedAt?: string;
}

interface Submission {
  id?: number;
  student?: Student;
  test?: Test;
  testId?: number;
  testTitle?: string;
  totalScore: number;
  submittedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

interface Feedback {
  id?: number;
  submission?: Submission;
  teacherName: string;
  content: string;
  createdAt?: string;
  updatedAt?: string;
}

// Students API
export const studentAPI = {
  getStudents: (params?: any) => client.get('/students', { params }),
  getStudent: (id: number) => client.get(`/students/${id}`),
  createStudent: (data: Student) => client.post('/students', data),
  updateStudent: (id: number, data: Student) => client.put(`/students/${id}`, data),
  deleteStudent: (id: number) => client.delete(`/students/${id}`),
};

// Tests API
export const testAPI = {
  getTests: (params?: any) => client.get('/tests', { params }),
  getTest: (id: number) => client.get(`/tests/${id}`),
  createTest: (data: Test) => client.post('/tests', data),
  updateTest: (id: number, data: Test) => client.put(`/tests/${id}`, data),
  deleteTest: (id: number) => client.delete(`/tests/${id}`),
  getTestStats: (id: number) => client.get(`/tests/${id}/stats`),
  getTestQuestions: (id: number) => client.get(`/tests/${id}/questions`),
  addQuestion: (testId: number, data: Omit<Question, 'id'>) => 
    client.post(`/tests/${testId}/questions`, data),
  deleteQuestion: (id: number) => client.delete(`/questions/${id}`),
};

// Submissions API
export const submissionAPI = {
  submitAnswers: (studentId: number, testId: number, answers: any) =>
    client.post('/submissions', answers, { params: { studentId, testId } }),
  getSubmission: (id: number) => client.get(`/submissions/${id}`),
  getByTestId: (testId: number) => client.get(`/submissions/test/${testId}`),
  getStudentSubmissions: (studentId: number) => client.get(`/submissions/student/${studentId}`),
  getSubmissionFeedbacks: (id: number) => client.get(`/submissions/${id}/feedback`),
};

// Feedback API
export const feedbackAPI = {
  getByTestId: (testId: number) => client.get(`/feedback/test/${testId}`),
  create: (data: Omit<Feedback, 'id' | 'submission' | 'createdAt'> & { submissionId: number }) => 
    client.post('/feedback', data),
  delete: (id: number) => client.delete(`/feedback/${id}`),
  getStudentFeedbacks: (studentId: number) => client.get(`/feedback/student/${studentId}`),
  updateFeedback: (id: number, data: Feedback) => client.put(`/feedback/${id}`, data),
};

export default client;

export type { Student, Test, Question, Submission, Feedback };