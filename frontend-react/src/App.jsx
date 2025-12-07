import { BrowserRouter as Router, Routes, Route, Link, useLocation } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Card } from 'flowbite-react';
import Students from './pages/Students';
import Tests from './pages/Tests';
import TestDetail from './pages/TestDetail';
import TestAnswers from './pages/TestAnswers';
import TestFeedback from './pages/TestFeedback';
import { HiAcademicCap, HiHome, HiUser, HiClipboardList } from 'react-icons/hi';

const queryClient = new QueryClient();

function Navigation() {
  const location = useLocation();

  const isActive = (path) => {
    if (path === '/') {
      return location.pathname === path;
    }
    return location.pathname.startsWith(path);
  };

  return (
    <nav className="sticky top-0 z-50 shadow-sm bg-white border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex items-center">
            <Link to="/" className="flex items-center space-x-3">
              <div className="p-2 bg-gradient-to-r from-blue-600 to-purple-600 rounded-lg">
                <HiAcademicCap className="h-8 w-8 text-white" />
              </div>
              <span className="self-center whitespace-nowrap text-xl font-semibold text-gray-900">
                학원 관리 시스템
              </span>
            </Link>
          </div>
          
          <div className="flex items-center space-x-8">
            <Link
              to="/"
              className={`flex items-center space-x-2 px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                isActive('/') 
                  ? 'text-blue-600 bg-blue-50' 
                  : 'text-gray-700 hover:text-blue-600 hover:bg-gray-50'
              }`}
            >
              <HiHome className="h-4 w-4" />
              <span>홈</span>
            </Link>
            <Link
              to="/students"
              className={`flex items-center space-x-2 px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                isActive('/students') 
                  ? 'text-blue-600 bg-blue-50' 
                  : 'text-gray-700 hover:text-blue-600 hover:bg-gray-50'
              }`}
            >
              <HiUser className="h-4 w-4" />
              <span>학생 관리</span>
            </Link>
            <Link
              to="/tests"
              className={`flex items-center space-x-2 px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                isActive('/tests') 
                  ? 'text-blue-600 bg-blue-50' 
                  : 'text-gray-700 hover:text-blue-600 hover:bg-gray-50'
              }`}
            >
              <HiClipboardList className="h-4 w-4" />
              <span>시험 관리</span>
            </Link>
          </div>
        </div>
      </div>
    </nav>
  );
}

function HomePage() {
  return (
    <div className="min-h-[80vh] flex items-center justify-center bg-gray-50">
      <div className="text-center max-w-4xl mx-auto px-4 py-16">
        <div className="mb-12">
          <div className="inline-block p-6 bg-gradient-to-br from-blue-600 to-purple-600 rounded-3xl shadow-2xl mb-8">
            <HiAcademicCap className="h-24 w-24 text-white" />
          </div>
          <h1 className="text-6xl font-extrabold mb-6 bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
            학원 관리 시스템
          </h1>
          <p className="text-xl text-gray-600 mb-12 max-w-2xl mx-auto">
            효율적인 학생 관리와 시험 평가를 위한 완벽한 솔루션으로 학원 운영을 간소화하세요
          </p>
        </div>
        
        <div className="grid md:grid-cols-2 gap-8 max-w-3xl mx-auto mb-12">
          <Link to="/students">
            <Card className="h-full hover:shadow-xl transition-all duration-300 border-0 shadow-lg bg-gradient-to-br from-blue-50 to-blue-100 hover:from-blue-100 hover:to-blue-200">
              <div className="flex flex-col items-center p-6">
                <div className="p-4 bg-blue-500 rounded-2xl mb-6">
                  <HiUser className="h-12 w-12 text-white" />
                </div>
                <h3 className="text-2xl font-bold text-gray-900 mb-3">학생 관리</h3>
                <p className="text-gray-700 text-center">
                  학생 정보 등록, 수정, 조회 및 검색 기능으로 체계적인 학생 관리
                </p>
              </div>
            </Card>
          </Link>
          
          <Link to="/tests">
            <Card className="h-full hover:shadow-xl transition-all duration-300 border-0 shadow-lg bg-gradient-to-br from-purple-50 to-purple-100 hover:from-purple-100 hover:to-purple-200">
              <div className="flex flex-col items-center p-6">
                <div className="p-4 bg-purple-500 rounded-2xl mb-6">
                  <HiClipboardList className="h-12 w-12 text-white" />
                </div>
                <h3 className="text-2xl font-bold text-gray-900 mb-3">시험 관리</h3>
                <p className="text-gray-700 text-center">
                  시험 생성, 정답 관리, 성적 확인 및 피드백 제공까지 원스톱 서비스
                </p>
              </div>
            </Card>
          </Link>
        </div>

        <Card className="max-w-4xl mx-auto bg-gradient-to-r from-gray-50 to-gray-100 border-0 shadow-lg">
          <div className="p-8">
            <h4 className="text-2xl font-bold text-gray-900 mb-6 text-center">주요 기능</h4>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              <div className="text-center">
                <div className="h-12 w-12 bg-blue-500 rounded-full flex items-center justify-center mx-auto mb-3">
                  <span className="text-white font-bold">1</span>
                </div>
                <h5 className="font-semibold text-gray-900 mb-2">학생 정보 관리</h5>
                <p className="text-sm text-gray-600">체계적인 학생 데이터베이스</p>
              </div>
              <div className="text-center">
                <div className="h-12 w-12 bg-purple-500 rounded-full flex items-center justify-center mx-auto mb-3">
                  <span className="text-white font-bold">2</span>
                </div>
                <h5 className="font-semibold text-gray-900 mb-2">시험 문제 생성</h5>
                <p className="text-sm text-gray-600">간편한 시험 출제 도구</p>
              </div>
              <div className="text-center">
                <div className="h-12 w-12 bg-green-500 rounded-full flex items-center justify-center mx-auto mb-3">
                  <span className="text-white font-bold">3</span>
                </div>
                <h5 className="font-semibold text-gray-900 mb-2">성적 자동 계산</h5>
                <p className="text-sm text-gray-600">정확한 채점 및 분석</p>
              </div>
              <div className="text-center">
                <div className="h-12 w-12 bg-orange-500 rounded-full flex items-center justify-center mx-auto mb-3">
                  <span className="text-white font-bold">4</span>
                </div>
                <h5 className="font-semibold text-gray-900 mb-2">피드백 관리</h5>
                <p className="text-sm text-gray-600">개인별 맞춤 지도</p>
              </div>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <Router>
        <div className="min-h-screen bg-gray-50">
          <Navigation />
          <main>
            <Routes>
              <Route path="/" element={<HomePage />} />
              <Route path="/students" element={<Students />} />
              <Route path="/tests" element={<Tests />} />
              <Route path="/tests/:id" element={<TestDetail />} />
              <Route path="/tests/:id/answers" element={<TestAnswers />} />
              <Route path="/tests/:id/feedback" element={<TestFeedback />} />
            </Routes>
          </main>
        </div>
      </Router>
    </QueryClientProvider>
  );
}

export default App;