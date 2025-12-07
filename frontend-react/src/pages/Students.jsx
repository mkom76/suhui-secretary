import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { studentAPI } from '../api/client';
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
  HiUserAdd, 
  HiPencil, 
  HiTrash, 
  HiUser, 
  HiAcademicCap,
  HiOfficeBuilding,
  HiChevronLeft,
  HiChevronRight,
  HiX,
  HiCheck
} from 'react-icons/hi';

function Students() {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingStudent, setEditingStudent] = useState(null);
  const [formData, setFormData] = useState({ name: '', grade: '', school: '' });
  const [searchName, setSearchName] = useState('');
  const [page, setPage] = useState(0);
  const [error, setError] = useState('');
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ['students', searchName, page],
    queryFn: () => studentAPI.getStudents({ name: searchName, page, size: 10 }),
  });

  const createMutation = useMutation({
    mutationFn: studentAPI.createStudent,
    onSuccess: () => {
      queryClient.invalidateQueries(['students']);
      closeModal();
      setError('');
    },
    onError: (err) => {
      setError('학생 추가에 실패했습니다.');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => studentAPI.updateStudent(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['students']);
      closeModal();
      setError('');
    },
    onError: (err) => {
      setError('학생 정보 수정에 실패했습니다.');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: studentAPI.deleteStudent,
    onSuccess: () => {
      queryClient.invalidateQueries(['students']);
    },
    onError: (err) => {
      setError('학생 삭제에 실패했습니다.');
    },
  });

  const openModal = (student = null) => {
    if (student) {
      setEditingStudent(student);
      setFormData({ name: student.name, grade: student.grade, school: student.school });
    } else {
      setEditingStudent(null);
      setFormData({ name: '', grade: '', school: '' });
    }
    setError('');
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setEditingStudent(null);
    setFormData({ name: '', grade: '', school: '' });
    setError('');
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!formData.name.trim() || !formData.grade.trim() || !formData.school.trim()) {
      setError('모든 필드를 입력해주세요.');
      return;
    }
    
    if (editingStudent) {
      updateMutation.mutate({ id: editingStudent.id, data: formData });
    } else {
      createMutation.mutate(formData);
    }
  };

  const handleDelete = (studentId, studentName) => {
    if (confirm(`정말로 "${studentName}" 학생을 삭제하시겠습니까?`)) {
      deleteMutation.mutate(studentId);
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-96">
        <div className="text-center">
          <Spinner size="xl" className="mb-4" />
          <p className="text-gray-600">학생 정보를 불러오는 중...</p>
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
              <div className="p-2 bg-blue-500 rounded-lg">
                <HiUser className="h-8 w-8 text-white" />
              </div>
              학생 리스트
            </h1>
            <p className="text-gray-600">
              학원에 등록된 학생들의 정보를 관리합니다
            </p>
          </div>
          <div className="mt-4 sm:mt-0">
            <Button
              gradientDuoTone="purpleToBlue"
              onClick={() => openModal()}
              size="lg"
            >
              <HiUserAdd className="mr-2 h-5 w-5" />
              학생 추가
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
            placeholder="학생 이름으로 검색..."
            value={searchName}
            onChange={(e) => {
              setSearchName(e.target.value);
              setPage(0);
            }}
            className="pl-10"
            sizing="lg"
          />
        </div>
      </Card>

      {/* 학생 리스트 */}
      <Card>
        <div className="overflow-x-auto">
          <Table hoverable>
            <Table.Head>
              <Table.HeadCell className="w-16">번호</Table.HeadCell>
              <Table.HeadCell>이름</Table.HeadCell>
              <Table.HeadCell>학년</Table.HeadCell>
              <Table.HeadCell>학교</Table.HeadCell>
              <Table.HeadCell>
                <span className="sr-only">Actions</span>
              </Table.HeadCell>
            </Table.Head>
            <Table.Body className="divide-y">
              {data?.content?.length === 0 ? (
                <Table.Row>
                  <Table.Cell colSpan={5} className="text-center py-12">
                    <div className="flex flex-col items-center justify-center text-gray-500">
                      <HiUser className="h-12 w-12 mb-4 text-gray-300" />
                      <p>검색 결과가 없습니다</p>
                    </div>
                  </Table.Cell>
                </Table.Row>
              ) : (
                data?.content?.map((student, index) => (
                  <Table.Row key={student.id} className="bg-white hover:bg-gray-50">
                    <Table.Cell className="font-medium text-gray-900">
                      {data.pageable.pageNumber * data.pageable.pageSize + index + 1}
                    </Table.Cell>
                    <Table.Cell className="font-semibold text-gray-900">
                      <div className="flex items-center gap-2">
                        <HiUser className="h-4 w-4 text-gray-400" />
                        {student.name}
                      </div>
                    </Table.Cell>
                    <Table.Cell>
                      <Badge color="info" icon={HiAcademicCap} size="sm">
                        {student.grade}
                      </Badge>
                    </Table.Cell>
                    <Table.Cell>
                      <div className="flex items-center gap-2">
                        <HiOfficeBuilding className="h-4 w-4 text-gray-400" />
                        <span className="text-gray-700">{student.school}</span>
                      </div>
                    </Table.Cell>
                    <Table.Cell>
                      <div className="flex gap-2">
                        <Button
                          size="sm"
                          color="gray"
                          onClick={() => openModal(student)}
                        >
                          <HiPencil className="h-4 w-4" />
                        </Button>
                        <Button
                          size="sm"
                          color="failure"
                          onClick={() => handleDelete(student.id, student.name)}
                        >
                          <HiTrash className="h-4 w-4" />
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
                전체 {data.totalElements}명 중{' '}
                {data.pageable.pageNumber * data.pageable.pageSize + 1}-
                {Math.min((data.pageable.pageNumber + 1) * data.pageable.pageSize, data.totalElements)}명
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

      {/* 모달 */}
      <Modal show={isModalOpen} onClose={closeModal} size="md">
        <Modal.Header>
          <div className="flex items-center gap-2">
            <HiUserAdd className="h-5 w-5" />
            {editingStudent ? '학생 정보 수정' : '새 학생 추가'}
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
              <Label htmlFor="name" value="이름" className="mb-2 block" />
              <TextInput
                id="name"
                type="text"
                value={formData.name}
                onChange={(e) => setFormData({...formData, name: e.target.value})}
                placeholder="학생 이름을 입력하세요"
                required
                icon={HiUser}
              />
            </div>
            
            <div>
              <Label htmlFor="grade" value="학년" className="mb-2 block" />
              <TextInput
                id="grade"
                type="text"
                value={formData.grade}
                onChange={(e) => setFormData({...formData, grade: e.target.value})}
                placeholder="예: 고1, 중3"
                required
                icon={HiAcademicCap}
              />
            </div>
            
            <div>
              <Label htmlFor="school" value="학교" className="mb-2 block" />
              <TextInput
                id="school"
                type="text"
                value={formData.school}
                onChange={(e) => setFormData({...formData, school: e.target.value})}
                placeholder="학교명을 입력하세요"
                required
                icon={HiOfficeBuilding}
              />
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
              {editingStudent ? '수정' : '저장'}
            </Button>
          </div>
        </Modal.Footer>
      </Modal>
    </div>
  );
}

export default Students;