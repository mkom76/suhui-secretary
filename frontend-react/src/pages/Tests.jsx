import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { testAPI } from '../api/client';
import {
  Button,
  Card,
  TextInput,
  Label,
  Modal,
  Table,
  Badge,
  Spinner,
  Alert
} from 'flowbite-react';
import {
  HiSearch,
  HiPlus,
  HiPencil,
  HiTrash,
  HiClipboardList,
  HiEye,
  HiChartBar,
  HiUser,
  HiCalendar,
  HiChevronLeft,
  HiChevronRight,
  HiX,
  HiCheck,
  HiDocument
} from 'react-icons/hi';

function Tests() {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingTest, setEditingTest] = useState(null);
  const [formData, setFormData] = useState({ title: '' });
  const [searchTitle, setSearchTitle] = useState('');
  const [page, setPage] = useState(0);
  const [error, setError] = useState('');
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const { data, isLoading } = useQuery({
    queryKey: ['tests', searchTitle, page],
    queryFn: () => testAPI.getTests({ title: searchTitle, page, size: 10 }),
  });

  const createMutation = useMutation({
    mutationFn: testAPI.createTest,
    onSuccess: () => {
      queryClient.invalidateQueries(['tests']);
      closeModal();
      setError('');
    },
    onError: (err) => {
      setError('시험 생성에 실패했습니다.');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => testAPI.updateTest(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['tests']);
      closeModal();
      setError('');
    },
    onError: (err) => {
      setError('시험 정보 수정에 실패했습니다.');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: testAPI.deleteTest,
    onSuccess: () => {
      queryClient.invalidateQueries(['tests']);
    },
    onError: (err) => {
      setError('시험 삭제에 실패했습니다.');
    },
  });

  const openModal = (test = null) => {
    if (test) {
      setEditingTest(test);
      setFormData({ title: test.title });
    } else {
      setEditingTest(null);
      setFormData({ title: '' });
    }
    setError('');
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setEditingTest(null);
    setFormData({ title: '' });
    setError('');
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!formData.title.trim()) {
      setError('시험 제목을 입력해주세요.');
      return;
    }
    
    if (editingTest) {
      updateMutation.mutate({ id: editingTest.id, data: formData });
    } else {
      createMutation.mutate(formData);
    }
  };

  const handleDelete = (testId, testTitle) => {
    if (confirm(`정말로 "${testTitle}" 시험을 삭제하시겠습니까?`)) {
      deleteMutation.mutate(testId);
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-96">
        <div className="text-center">
          <Spinner size="xl" className="mb-4" />
          <p className="text-gray-600">시험 정보를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6 max-w-7xl mx-auto">
      {/* 에러 메시지 */}
      {error && (
        <Alert color="failure" onDismiss={() => setError('')}>
          {error}
        </Alert>
      )}

      {/* 헤더 섹션 */}
      <Card>
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 flex items-center gap-3 mb-2">
              <div className="p-2 bg-purple-500 rounded-lg">
                <HiClipboardList className="h-8 w-8 text-white" />
              </div>
              시험 관리
            </h1>
            <p className="text-gray-600">
              학원에서 진행하는 시험을 관리하고 결과를 확인합니다
            </p>
          </div>
          <div className="mt-4 sm:mt-0">
            <Button
              gradientDuoTone="purpleToBlue"
              onClick={() => openModal()}
              size="lg"
            >
              <HiPlus className="mr-2 h-5 w-5" />
              새 시험 생성
            </Button>
          </div>
        </div>
      </Card>

      {/* 검색 섹션 */}
      <Card>
        <div className="relative">
          <div className="absolute inset-y-0 left-0 flex items-center pl-3">
            <HiSearch className="h-5 w-5 text-gray-400" />
          </div>
          <TextInput
            type="text"
            placeholder="시험 제목으로 검색..."
            value={searchTitle}
            onChange={(e) => {
              setSearchTitle(e.target.value);
              setPage(0);
            }}
            className="pl-10"
            sizing="lg"
          />
        </div>
      </Card>

      {/* 시험 리스트 */}
      <Card>
        <div className="overflow-x-auto">
          <Table hoverable>
            <Table.Head>
              <Table.HeadCell className="w-16">번호</Table.HeadCell>
              <Table.HeadCell>시험 제목</Table.HeadCell>
              <Table.HeadCell>생성일</Table.HeadCell>
              <Table.HeadCell>상태</Table.HeadCell>
              <Table.HeadCell>
                <span className="sr-only">Actions</span>
              </Table.HeadCell>
            </Table.Head>
            <Table.Body className="divide-y">
              {data?.content?.length === 0 ? (
                <Table.Row>
                  <Table.Cell colSpan={5} className="text-center py-12">
                    <div className="flex flex-col items-center justify-center text-gray-500">
                      <HiClipboardList className="h-12 w-12 mb-4 text-gray-300" />
                      <p>검색 결과가 없습니다</p>
                    </div>
                  </Table.Cell>
                </Table.Row>
              ) : (
                data?.content?.map((test, index) => (
                  <Table.Row key={test.id} className="bg-white hover:bg-gray-50">
                    <Table.Cell className="font-medium text-gray-900">
                      {data.pageable.pageNumber * data.pageable.pageSize + index + 1}
                    </Table.Cell>
                    <Table.Cell className="font-semibold text-gray-900">
                      <div className="flex items-center gap-2">
                        <HiDocument className="h-4 w-4 text-gray-400" />
                        {test.title}
                      </div>
                    </Table.Cell>
                    <Table.Cell>
                      <div className="flex items-center gap-2 text-sm text-gray-600">
                        <HiCalendar className="h-4 w-4" />
                        {formatDate(test.createdAt)}
                      </div>
                    </Table.Cell>
                    <Table.Cell>
                      <Badge color="success" icon={() => <div className="h-2 w-2 rounded-full bg-white animate-pulse" />}>
                        활성
                      </Badge>
                    </Table.Cell>
                    <Table.Cell>
                      <div className="flex gap-1 flex-wrap">
                        <Button
                          size="xs"
                          color="blue"
                          onClick={() => navigate(`/tests/${test.id}`)}
                          className="min-w-0"
                        >
                          <HiEye className="h-3 w-3" />
                        </Button>
                        <Button
                          size="xs"
                          color="green"
                          onClick={() => navigate(`/tests/${test.id}/answers`)}
                          className="min-w-0"
                        >
                          <HiChartBar className="h-3 w-3" />
                        </Button>
                        <Button
                          size="xs"
                          color="purple"
                          onClick={() => navigate(`/tests/${test.id}/feedback`)}
                          className="min-w-0"
                        >
                          <HiUser className="h-3 w-3" />
                        </Button>
                        <Button
                          size="xs"
                          color="gray"
                          onClick={() => openModal(test)}
                          className="min-w-0"
                        >
                          <HiPencil className="h-3 w-3" />
                        </Button>
                        <Button
                          size="xs"
                          color="failure"
                          onClick={() => handleDelete(test.id, test.title)}
                          className="min-w-0"
                        >
                          <HiTrash className="h-3 w-3" />
                        </Button>
                      </div>
                    </Table.Cell>
                  </Table.Row>
                ))
              )}
            </Table.Body>
          </Table>

          {/* 페이지네이션 */}
          {data && data.totalPages > 1 && (
            <div className="flex items-center justify-between p-4 border-t">
              <p className="text-sm text-gray-700">
                전체 {data.totalElements}개 중{' '}
                {data.pageable.pageNumber * data.pageable.pageSize + 1}-
                {Math.min((data.pageable.pageNumber + 1) * data.pageable.pageSize, data.totalElements)}개
              </p>
              <div className="flex gap-3 items-center">
                <Button
                  size="sm"
                  color="gray"
                  onClick={() => setPage(p => Math.max(0, p - 1))}
                  disabled={page === 0}
                >
                  <HiChevronLeft className="h-4 w-4 mr-1" />
                  이전
                </Button>
                <span className="px-3 py-1 bg-gray-100 rounded-lg text-sm font-medium">
                  {page + 1} / {data.totalPages}
                </span>
                <Button
                  size="sm"
                  color="gray"
                  onClick={() => setPage(p => p + 1)}
                  disabled={page >= data.totalPages - 1}
                >
                  다음
                  <HiChevronRight className="h-4 w-4 ml-1" />
                </Button>
              </div>
            </div>
          )}
        </div>
      </Card>

      {/* 액션 버튼 설명 */}
      <Card className="bg-blue-50 border-blue-200">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4 text-sm">
          <div className="flex items-center gap-2">
            <div className="p-1 bg-blue-500 rounded">
              <HiEye className="h-3 w-3 text-white" />
            </div>
            <span className="text-blue-700">상세보기</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="p-1 bg-green-500 rounded">
              <HiChartBar className="h-3 w-3 text-white" />
            </div>
            <span className="text-green-700">정답 관리</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="p-1 bg-purple-500 rounded">
              <HiUser className="h-3 w-3 text-white" />
            </div>
            <span className="text-purple-700">피드백</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="p-1 bg-gray-500 rounded">
              <HiPencil className="h-3 w-3 text-white" />
            </div>
            <span className="text-gray-700">수정</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="p-1 bg-red-500 rounded">
              <HiTrash className="h-3 w-3 text-white" />
            </div>
            <span className="text-red-700">삭제</span>
          </div>
        </div>
      </Card>

      {/* 모달 */}
      <Modal show={isModalOpen} onClose={closeModal} size="md">
        <Modal.Header>
          <div className="flex items-center gap-2">
            <HiClipboardList className="h-5 w-5" />
            {editingTest ? '시험 정보 수정' : '새 시험 생성'}
          </div>
        </Modal.Header>
        <Modal.Body>
          <form onSubmit={handleSubmit} className="space-y-6">
            {error && (
              <Alert color="failure">
                {error}
              </Alert>
            )}
            
            <div>
              <Label htmlFor="title" value="시험 제목" className="mb-2 block" />
              <TextInput
                id="title"
                type="text"
                value={formData.title}
                onChange={(e) => setFormData({...formData, title: e.target.value})}
                placeholder="예: 2024년 1차 모의고사"
                required
                icon={HiDocument}
                sizing="lg"
              />
              <p className="mt-2 text-sm text-gray-600">
                명확하고 구체적인 제목을 입력하세요
              </p>
            </div>
          </form>
        </Modal.Body>
        <Modal.Footer>
          <div className="flex gap-3 w-full">
            <Button
              color="gray"
              onClick={closeModal}
              className="flex-1"
            >
              <HiX className="mr-2 h-4 w-4" />
              취소
            </Button>
            <Button
              gradientDuoTone="purpleToBlue"
              onClick={handleSubmit}
              disabled={createMutation.isPending || updateMutation.isPending}
              className="flex-1"
            >
              {(createMutation.isPending || updateMutation.isPending) ? (
                <Spinner size="sm" className="mr-2" />
              ) : (
                <HiCheck className="mr-2 h-4 w-4" />
              )}
              {editingTest ? '수정' : '생성'}
            </Button>
          </div>
        </Modal.Footer>
      </Modal>
    </div>
  );
}

export default Tests;