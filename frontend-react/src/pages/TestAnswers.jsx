import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { testAPI } from '../api/client';
import { Button, Card, TextInput, Table, Spinner, Alert } from 'flowbite-react';
import { HiArrowLeft, HiPlus, HiTrash, HiChartBar } from 'react-icons/hi';

function TestAnswers() {
  const { id } = useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [newQuestion, setNewQuestion] = useState({ number: '', answer: '' });
  const [error, setError] = useState('');

  const { data: questions, isLoading } = useQuery({
    queryKey: ['test-questions', id],
    queryFn: () => testAPI.getTestQuestions(id),
  });

  const addQuestionMutation = useMutation({
    mutationFn: (questionData) => testAPI.addQuestion(id, questionData),
    onSuccess: () => {
      queryClient.invalidateQueries(['test-questions', id]);
      setNewQuestion({ number: '', answer: '' });
      setError('');
    },
    onError: () => {
      setError('문제 추가에 실패했습니다.');
    },
  });

  const deleteQuestionMutation = useMutation({
    mutationFn: testAPI.deleteQuestion,
    onSuccess: () => {
      queryClient.invalidateQueries(['test-questions', id]);
    },
  });

  const handleAddQuestion = () => {
    if (!newQuestion.number || !newQuestion.answer) {
      setError('문제 번호와 정답을 모두 입력해주세요.');
      return;
    }
    addQuestionMutation.mutate(newQuestion);
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-96">
        <div className="text-center">
          <Spinner size="xl" className="mb-4" />
          <p className="text-gray-600">문제 정보를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      {error && (
        <Alert color="failure" onDismiss={() => setError('')}>
          {error}
        </Alert>
      )}

      {/* 헤더 */}
      <Card>
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 flex items-center gap-3">
              <HiChartBar className="h-8 w-8 text-green-600" />
              정답 관리
            </h1>
            <p className="text-gray-600 mt-2">시험 ID: {id}</p>
          </div>
          <Button
            color="gray"
            onClick={() => navigate('/tests')}
          >
            <HiArrowLeft className="mr-2 h-4 w-4" />
            목록으로
          </Button>
        </div>
      </Card>

      {/* 새 문제 추가 */}
      <Card>
        <h2 className="text-xl font-semibold mb-4">새 문제 추가</h2>
        <div className="flex gap-3">
          <TextInput
            type="number"
            placeholder="문제 번호"
            value={newQuestion.number}
            onChange={(e) => setNewQuestion({...newQuestion, number: e.target.value})}
            className="w-32"
          />
          <TextInput
            type="text"
            placeholder="정답"
            value={newQuestion.answer}
            onChange={(e) => setNewQuestion({...newQuestion, answer: e.target.value})}
            className="flex-1"
          />
          <Button
            gradientDuoTone="purpleToBlue"
            onClick={handleAddQuestion}
            disabled={addQuestionMutation.isPending}
          >
            <HiPlus className="mr-2 h-4 w-4" />
            추가
          </Button>
        </div>
      </Card>

      {/* 문제 목록 */}
      <Card>
        <h2 className="text-xl font-semibold mb-4">문제 목록</h2>
        <div className="overflow-x-auto">
          <Table hoverable>
            <Table.Head>
              <Table.HeadCell>문제 번호</Table.HeadCell>
              <Table.HeadCell>정답</Table.HeadCell>
              <Table.HeadCell>
                <span className="sr-only">Actions</span>
              </Table.HeadCell>
            </Table.Head>
            <Table.Body className="divide-y">
              {questions?.data?.length === 0 ? (
                <Table.Row>
                  <Table.Cell colSpan={3} className="text-center py-12">
                    <div className="text-gray-500">
                      <p>등록된 문제가 없습니다</p>
                    </div>
                  </Table.Cell>
                </Table.Row>
              ) : (
                questions?.data?.map((question) => (
                  <Table.Row key={question.id}>
                    <Table.Cell className="font-medium">
                      {question.number}번
                    </Table.Cell>
                    <Table.Cell>
                      <span className="px-3 py-1 bg-green-100 text-green-800 rounded-full text-sm font-medium">
                        {question.answer}
                      </span>
                    </Table.Cell>
                    <Table.Cell>
                      <Button
                        size="sm"
                        color="failure"
                        onClick={() => {
                          if (confirm(`${question.number}번 문제를 삭제하시겠습니까?`)) {
                            deleteQuestionMutation.mutate(question.id);
                          }
                        }}
                      >
                        <HiTrash className="h-4 w-4" />
                      </Button>
                    </Table.Cell>
                  </Table.Row>
                ))
              )}
            </Table.Body>
          </Table>
        </div>
      </Card>
    </div>
  );
}

export default TestAnswers;