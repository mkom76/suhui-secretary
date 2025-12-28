import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { submissionAPI, feedbackAPI } from '../api/client';
import { Button, Card, TextInput, Textarea, Table, Spinner, Alert, Modal, Label } from 'flowbite-react';
import { HiArrowLeft, HiPlus, HiTrash, HiUser, HiX, HiCheck } from 'react-icons/hi';

function TestFeedback() {
  const { id } = useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [newFeedback, setNewFeedback] = useState({ submissionId: '', teacherName: '', content: '' });
  const [error, setError] = useState('');

  const { data: submissions, isLoading } = useQuery({
    queryKey: ['test-submissions', id],
    queryFn: () => submissionAPI.getByTestId(id),
  });

  const { data: feedbacks, isLoading: feedbacksLoading } = useQuery({
    queryKey: ['test-feedbacks', id],
    queryFn: () => feedbackAPI.getByTestId(id),
  });

  const addFeedbackMutation = useMutation({
    mutationFn: feedbackAPI.create,
    onSuccess: () => {
      queryClient.invalidateQueries(['test-feedbacks', id]);
      setIsModalOpen(false);
      setNewFeedback({ submissionId: '', teacherName: '', content: '' });
      setError('');
    },
    onError: () => {
      setError('피드백 추가에 실패했습니다.');
    },
  });

  const deleteFeedbackMutation = useMutation({
    mutationFn: feedbackAPI.delete,
    onSuccess: () => {
      queryClient.invalidateQueries(['test-feedbacks', id]);
    },
  });

  const handleAddFeedback = () => {
    if (!newFeedback.submissionId || !newFeedback.teacherName || !newFeedback.content) {
      setError('모든 필드를 입력해주세요.');
      return;
    }
    addFeedbackMutation.mutate(newFeedback);
  };

  if (isLoading || feedbacksLoading) {
    return (
      <div className="flex items-center justify-center min-h-96">
        <div className="text-center">
          <Spinner size="xl" className="mb-4" />
          <p className="text-gray-600">피드백 정보를 불러오는 중...</p>
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
              <HiUser className="h-8 w-8 text-purple-600" />
              피드백 관리
            </h1>
          </div>
          <div className="flex gap-3">
            <Button
              gradientDuoTone="purpleToBlue"
              onClick={() => setIsModalOpen(true)}
            >
              <HiPlus className="mr-2 h-4 w-4" />
              피드백 추가
            </Button>
            <Button
              color="gray"
              onClick={() => navigate('/tests')}
            >
              <HiArrowLeft className="mr-2 h-4 w-4" />
              목록으로
            </Button>
          </div>
        </div>
      </Card>

      {/* 피드백 목록 */}
      <Card>
        <h2 className="text-xl font-semibold mb-4">피드백 목록</h2>
        <div className="overflow-x-auto">
          <Table hoverable>
            <Table.Head>
              <Table.HeadCell>학생</Table.HeadCell>
              <Table.HeadCell>선생님</Table.HeadCell>
              <Table.HeadCell>피드백 내용</Table.HeadCell>
              <Table.HeadCell>작성일</Table.HeadCell>
              <Table.HeadCell>
                <span className="sr-only">Actions</span>
              </Table.HeadCell>
            </Table.Head>
            <Table.Body className="divide-y">
              {feedbacks?.content?.length === 0 ? (
                <Table.Row>
                  <Table.Cell colSpan={5} className="text-center py-12">
                    <div className="text-gray-500">
                      <p>등록된 피드백이 없습니다</p>
                    </div>
                  </Table.Cell>
                </Table.Row>
              ) : (
                feedbacks?.content?.map((feedback) => (
                  <Table.Row key={feedback.id}>
                    <Table.Cell className="font-medium">
                      {feedback.submission?.student?.name || '알 수 없음'}
                    </Table.Cell>
                    <Table.Cell>
                      <span className="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm font-medium">
                        {feedback.teacherName}
                      </span>
                    </Table.Cell>
                    <Table.Cell className="max-w-xs truncate">
                      {feedback.content}
                    </Table.Cell>
                    <Table.Cell>
                      {new Date(feedback.createdAt).toLocaleDateString('ko-KR')}
                    </Table.Cell>
                    <Table.Cell>
                      <Button
                        size="sm"
                        color="failure"
                        onClick={() => {
                          if (confirm('이 피드백을 삭제하시겠습니까?')) {
                            deleteFeedbackMutation.mutate(feedback.id);
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

      {/* 피드백 추가 모달 */}
      <Modal show={isModalOpen} onClose={() => setIsModalOpen(false)} size="md">
        <Modal.Header>
          <div className="flex items-center gap-2">
            <HiUser className="h-5 w-5" />
            새 피드백 추가
          </div>
        </Modal.Header>
        <Modal.Body>
          <div className="space-y-6">
            {error && (
              <Alert color="failure">
                {error}
              </Alert>
            )}
            
            <div>
              <Label htmlFor="submission" value="학생 선택" className="mb-2 block" />
              <select
                id="submission"
                value={newFeedback.submissionId}
                onChange={(e) => setNewFeedback({...newFeedback, submissionId: e.target.value})}
                className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              >
                <option value="">학생을 선택하세요</option>
                {submissions?.content?.map((submission) => (
                  <option key={submission.id} value={submission.id}>
                    {submission.student?.name} (점수: {submission.totalScore}점)
                  </option>
                ))}
              </select>
            </div>
            
            <div>
              <Label htmlFor="teacher" value="선생님 이름" className="mb-2 block" />
              <TextInput
                id="teacher"
                type="text"
                value={newFeedback.teacherName}
                onChange={(e) => setNewFeedback({...newFeedback, teacherName: e.target.value})}
                placeholder="선생님 이름을 입력하세요"
                required
              />
            </div>
            
            <div>
              <Label htmlFor="content" value="피드백 내용" className="mb-2 block" />
              <Textarea
                id="content"
                value={newFeedback.content}
                onChange={(e) => setNewFeedback({...newFeedback, content: e.target.value})}
                placeholder="학생에게 전달할 피드백을 입력하세요"
                rows={4}
                required
              />
            </div>
          </div>
        </Modal.Body>
        <Modal.Footer>
          <div className="flex gap-3 w-full">
            <Button
              color="gray"
              onClick={() => setIsModalOpen(false)}
              className="flex-1"
            >
              <HiX className="mr-2 h-4 w-4" />
              취소
            </Button>
            <Button
              gradientDuoTone="purpleToBlue"
              onClick={handleAddFeedback}
              disabled={addFeedbackMutation.isPending}
              className="flex-1"
            >
              {addFeedbackMutation.isPending ? (
                <Spinner size="sm" className="mr-2" />
              ) : (
                <HiCheck className="mr-2 h-4 w-4" />
              )}
              추가
            </Button>
          </div>
        </Modal.Footer>
      </Modal>
    </div>
  );
}

export default TestFeedback;