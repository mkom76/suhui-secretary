import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const client = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Students API
export const studentAPI = {
  getStudents: (params) => client.get('/students', { params }),
  getStudent: (id) => client.get(`/students/${id}`),
  createStudent: (data) => client.post('/students', data),
  updateStudent: (id, data) => client.put(`/students/${id}`, data),
  deleteStudent: (id) => client.delete(`/students/${id}`),
};

// Tests API
export const testAPI = {
  getTests: (params) => client.get('/tests', { params }),
  getTest: (id) => client.get(`/tests/${id}`),
  createTest: (data) => client.post('/tests', data),
  updateTest: (id, data) => client.put(`/tests/${id}`, data),
  deleteTest: (id) => client.delete(`/tests/${id}`),
  getTestStats: (id) => client.get(`/tests/${id}/stats`),
  getTestQuestions: (id) => client.get(`/tests/${id}/questions`),
  saveTestAnswers: (id, data) => client.put(`/tests/${id}/answers`, data),
};

// Submissions API
export const submissionAPI = {
  submitAnswers: (studentId, testId, answers) => 
    client.post('/submissions', answers, { params: { studentId, testId } }),
  getSubmission: (id) => client.get(`/submissions/${id}`),
  getSubmissionByStudentAndTest: (studentId, testId) => 
    client.get('/submissions', { params: { studentId, testId } }),
  getStudentSubmissions: (studentId) => client.get(`/submissions/student/${studentId}`),
  getSubmissionFeedbacks: (id) => client.get(`/submissions/${id}/feedback`),
  createFeedback: (submissionId, data) => client.post(`/submissions/${submissionId}/feedback`, data),
};

// Feedback API
export const feedbackAPI = {
  getStudentFeedbacks: (studentId) => client.get(`/feedback/student/${studentId}`),
  updateFeedback: (id, data) => client.put(`/feedback/${id}`, data),
  deleteFeedback: (id) => client.delete(`/feedback/${id}`),
};

export default client;