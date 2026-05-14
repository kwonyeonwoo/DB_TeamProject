# Frontend API Integration

이 문서는 프론트엔드에서 백엔드 API를 연결할 때 지켜야 할 기준과 현재 연동 상태를 기록한다.

## 공통 기준

- API 기본 경로는 `/api`다.
- 환경 변수 `VITE_API_BASE_URL`이 있으면 해당 값을 우선 사용한다.
- 서버 세션 기반 인증을 사용한다.
- 모든 인증 요청은 쿠키를 포함해야 하므로 `withCredentials: true`를 사용한다.
- 프론트엔드는 로그인 토큰을 `localStorage` 또는 `sessionStorage`에 저장하지 않는다.
- API 요청/응답 필드명은 명세 기준에 따라 `snake_case`를 유지한다.
- 실패 응답은 `code`, `message`, 선택적 `details` 형식을 기대한다.

## Mock 전환 기준

- `VITE_USE_MOCK_API=true`일 때만 프론트엔드 내부 mock 응답을 사용한다.
- 실제 백엔드 연결 시에는 `VITE_USE_MOCK_API`를 비우거나 `false`로 둔다.
- mock은 화면 흐름 확인용이며, 실제 API 명세를 대체하지 않는다.

예시:

```env
VITE_API_BASE_URL=/api
VITE_USE_MOCK_API=true
```

## 현재 구현된 API 모듈

### 인증

파일: `frontend/src/api/auth.ts`

- `GET /api/users/me`
- `POST /api/auth/login`
- `POST /api/auth/signup`
- `POST /api/auth/logout`
- `PATCH /api/users/me`
- `DELETE /api/users/me`

현재 상태:

- API 함수 구조는 구현되어 있다.
- 세션 쿠키 요청 설정은 적용되어 있다.
- 실제 백엔드 응답 필드는 아직 런타임 검증 전이다.
- mock 모드는 로그인, 회원가입, 로그아웃, 내 정보 수정, 회원 탈퇴 흐름 확인용으로만 사용한다.

## 이후 API 모듈 분리 계획

아래와 같이 기능별 파일을 분리한다.

- `frontend/src/api/posts.ts`
- `frontend/src/api/comments.ts`
- `frontend/src/api/notifications.ts`
- `frontend/src/api/schedules.ts`
- `frontend/src/api/groups.ts`
- `frontend/src/api/reports.ts`

## 백엔드 연동 시 확인할 항목

- 실제 `User` 응답에 `login_id`, `email_address`, `role`, `status`가 모두 포함되는지 확인한다.
- 세션 만료 시 `GET /api/users/me`의 상태 코드와 에러 메시지를 확인한다.
- 회원 탈퇴 후 세션이 실제로 무효화되는지 확인한다.
- 관리자 계정 판별 기준이 `role === 'ADMIN'`과 일치하는지 확인한다.
- API 명세와 실제 응답이 다를 경우 이 문서에 충돌 항목으로 기록한다.

## 명세 충돌 기록

현재까지 프론트엔드 구현 중 새로 확인된 명세 충돌은 없다.

