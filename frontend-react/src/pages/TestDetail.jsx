import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { testAPI } from '../api/client';
import { Button, Card, Spinner } from 'flowbite-react';
import { HiArrowLeft, HiClipboardList } from 'react-icons/hi';

function TestDetail() {
  const { id } = useParams();
  const navigate = useNavigate();

  const { data: stats, isLoading } = useQuery({
    queryKey: ['test-stats', id],
    queryFn: () => testAPI.getTestStats(id),
  });

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

  const statsData = stats?.data;

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      {/* 헤더 */}
      <Card>
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 flex items-center gap-3">
              <HiClipboardList className="h-8 w-8 text-purple-600" />
              시험 상세 정보
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

      {/* 통계 정보 */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card>
          <div className="text-center">
            <div className="text-3xl font-bold text-blue-600 mb-2">
              {statsData?.totalSubmissions || 0}
            </div>
            <div className="text-gray-600">총 제출 수</div>
          </div>
        </Card>
        
        <Card>
          <div className="text-center">
            <div className="text-3xl font-bold text-green-600 mb-2">
              {statsData?.averageScore || 0}점
            </div>
            <div className="text-gray-600">평균 점수</div>
          </div>
        </Card>
        
        <Card>
          <div className="text-center">
            <div className="text-3xl font-bold text-purple-600 mb-2">
              {statsData?.totalQuestions || 0}문제
            </div>
            <div className="text-gray-600">총 문제 수</div>
          </div>
        </Card>
      </div>

      {/* 상세 정보 */}
      <Card>
        <h2 className="text-xl font-semibold mb-4">시험 상세 정보</h2>
        {statsData ? (
          <div className="space-y-3">
            <div className="flex justify-between">
              <span className="text-gray-600">시험명:</span>
              <span className="font-medium">{statsData.testTitle}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">생성일:</span>
              <span className="font-medium">{new Date(statsData.createdAt).toLocaleDateString('ko-KR')}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">최고 점수:</span>
              <span className="font-medium text-green-600">{statsData.maxScore}점</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">최저 점수:</span>
              <span className="font-medium text-red-600">{statsData.minScore}점</span>
            </div>
          </div>
        ) : (
          <p className="text-gray-500">시험 정보가 없습니다.</p>
        )}
      </Card>
    </div>
  );
}

export default TestDetail;