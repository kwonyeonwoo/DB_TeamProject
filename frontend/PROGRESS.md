# Frontend Progress

이 문서는 `frontend/init` 브랜치에서 프론트엔드 구현 진행 상황을 기록한다.

## 기준

- 저장소: `kwonyeonwoo/DB_TeamProject`
- 브랜치: `frontend/init`
- 프론트엔드 경로: `frontend`
- 명세 기준: `docs/normalized` 및 `docs/source`의 요구사항, API, 화면 흐름 문서

## 완료된 단계

### 1단계: 프로젝트 실행/구조 안정화

- Vite 기본 예제 화면을 제거했다.
- React Router 기반 라우팅 구조를 구성했다.
- 공통 레이아웃과 상단 메뉴를 구성했다.
- 주요 화면의 임시 UI를 문서 기준 화면 흐름에 맞춰 배치했다.
- 깨진 한글 텍스트와 유효하지 않은 JSX를 정리했다.

주요 파일:

- `frontend/src/App.tsx`
- `frontend/src/components/Layout.tsx`
- `frontend/src/pages/MainPage.tsx`
- `frontend/src/pages/PostListPage.tsx`
- `frontend/src/pages/PostDetailPage.tsx`
- `frontend/src/pages/PostWritePage.tsx`
- `frontend/src/pages/SchedulePage.tsx`
- `frontend/src/pages/GroupPage.tsx`
- `frontend/src/pages/AdminReportPage.tsx`
- `frontend/src/App.css`
- `frontend/src/index.css`

### 2단계: 인증

- 서버 세션 기반 인증 흐름을 전제로 API 함수 구조를 만들었다.
- 브라우저 저장소에 토큰을 저장하지 않는 방향으로 구성했다.
- 공통 API 클라이언트에 `withCredentials: true`를 적용했다.
- 현재 사용자 조회, 로그인, 회원가입, 로그아웃, 내 정보 수정, 회원 탈퇴 함수 구조를 추가했다.
- `AuthProvider`로 사용자 상태를 관리한다.
- 비로그인 사용자, 로그인 사용자, 관리자 전용 라우트를 구분했다.
- 로그인/회원가입 폼 제출 흐름을 연결했다.
- 마이페이지에서 내 정보 수정과 회원 탈퇴 요청 흐름을 연결했다.
- 백엔드 미완성 상황을 고려해 `VITE_USE_MOCK_API=true`일 때만 동작하는 임시 mock 인증 흐름을 추가했다.

주요 파일:

- `frontend/src/api/client.ts`
- `frontend/src/api/auth.ts`
- `frontend/src/api/errors.ts`
- `frontend/src/contexts/AuthProvider.tsx`
- `frontend/src/contexts/authContext.ts`
- `frontend/src/contexts/useAuth.ts`
- `frontend/src/components/AuthRoutes.tsx`
- `frontend/src/pages/LoginPage.tsx`
- `frontend/src/pages/SignupPage.tsx`
- `frontend/src/pages/MyPage.tsx`
- `frontend/.env.example`

## 검증 기록

- `npm.cmd run lint` 통과
- `npm.cmd run build` 통과
- 개발 서버 `/login` 경로 응답 확인

## 현재 상태

- 프론트엔드 구조와 인증 흐름은 연결되어 있다.
- 실제 백엔드 API 연동 전까지는 mock 전환 옵션을 사용할 수 있다.
- 게시글, 댓글, 알림, 일정, 그룹, 신고 관리는 아직 임시 UI 또는 자리 표시자 상태다.

## 다음 단계

다음 구현 순서는 `3단계: 게시글`이다.

