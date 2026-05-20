# 시연 CSRF 리스크 리뷰

- Review date: 2026-05-20
- Reviewed commit: `f248665` (`Fix backend CSRF token cookie handling`)
- Related file: `backend/src/main/java/com/academicshare/backend/auth/config/SecurityConfig.java`
- Scope: 시연 환경에서 게시글 작성, 댓글 작성, 일정 추가 등 상태 변경 요청의 `403 Forbidden` 재발 가능성 확인

## Summary

현재 백엔드는 CSRF를 비활성화하지 않고 유지한다. 이는 `docs/normalized/api-contract.md`, `docs/normalized/auth-policy.md`, `docs/normalized/acceptance-criteria.md`에 정리된 공식 계약과 일치한다.

다만 시연 안정성만 놓고 보면 CSRF 토큰 흐름은 여전히 주요 리스크 지점이다. 최근 수정으로 직접 `saveToken`을 중복 호출하던 문제는 제거됐지만, 브라우저에 오래된 `XSRF-TOKEN` 쿠키가 남아 있거나 프론트 요청 헤더 전달이 어긋나면 상태 변경 API는 의도대로 `403 ACCESS_DENIED`를 반환할 수 있다.

## Findings

### DEMO-CSRF-001. 시연 profile 전용 CSRF 비활성화가 없음

- Severity: MAJOR
- Status: CONFIRMED
- Related file: `backend/src/main/java/com/academicshare/backend/auth/config/SecurityConfig.java`

Problem:

현재 `SecurityConfig`에는 `.csrf(csrf -> csrf ...)` 설정이 존재하고, `CookieCsrfTokenRepository`를 사용한다. `csrf.disable()` 호출은 없다. 즉 로컬/시연 profile에서도 CSRF 검증은 활성화되어 있다.

Why it matters:

운영/명세 관점에서는 올바른 보안 동작이지만, 시연 환경에서는 쿠키 상태나 프론트의 `X-XSRF-TOKEN` 헤더 전달이 조금만 어긋나도 게시글 작성, 댓글 작성, 일정 추가 같은 상태 변경 요청이 `403 Forbidden`으로 실패할 수 있다.

Current evidence:

- `SecurityConfig`에서 `CookieCsrfTokenRepository.withHttpOnlyFalse()`를 사용한다.
- `/auth/signup`, `/auth/login`만 CSRF 예외로 둔다.
- 기존 테스트는 CSRF 없는 로그아웃 요청이 `403 ACCESS_DENIED`를 반환하는 동작을 검증한다.

Suggested fix:

시연 안정성을 최우선으로 하면 별도 `demo` 또는 `local-demo` profile에서만 CSRF를 비활성화하는 선택지를 검토한다. 단, 이 경우 명세상 보안 계약과 달라지므로 시연 전용 설정임을 명확히 문서화해야 한다.

Required user decision:

시연 환경에서 보안 계약 유지(CSRF ON)를 우선할지, 시연 성공률(CSRF OFF)을 우선할지 결정이 필요하다.

### DEMO-CSRF-002. CSRF 토큰 materialize 경로가 두 곳에 존재함

- Severity: MAJOR
- Status: PARTIALLY CONFIRMED
- Related file: `backend/src/main/java/com/academicshare/backend/auth/config/SecurityConfig.java`

Problem:

현재 `SecurityConfig`에는 `CsrfCookieFilter`와 `SpaCsrfTokenRequestHandler`가 모두 존재한다. 두 구성 모두 CSRF 토큰을 강제로 로드하기 위해 `getToken()` 또는 `deferredCsrfToken.get().getToken()`을 호출한다.

Why it matters:

현재 코드에는 이전 문제의 직접 원인이던 `generateToken()` 및 `saveToken()` 수동 호출은 없다. 따라서 동일 요청에서 서로 다른 토큰을 직접 저장하는 중복 발급 버그는 제거된 상태다. 그러나 토큰 materialize 책임이 두 곳에 나뉘어 있어, 시연 중 `Invalid CSRF token found`가 다시 발생하면 원인 추적이 복잡해질 수 있다.

Current evidence:

- `.csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())`가 존재한다.
- `.addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)`가 존재한다.
- `SpaCsrfTokenRequestHandler`는 `deferredCsrfToken.get().getToken()`을 호출한다.
- `CsrfCookieFilter`도 `DeferredCsrfToken` 또는 `CsrfToken`을 찾아 `getToken()`을 호출한다.
- 직접 `csrfTokenRepository.saveToken(...)` 호출은 제거되어 있다.

Suggested fix:

CSRF를 유지한다면 토큰 노출 방식은 한 곳으로 단순화하는 것이 좋다. 우선순위는 `SpaCsrfTokenRequestHandler` 중심으로 정리하고, 같은 동작이 안정적으로 검증되면 `CsrfCookieFilter` 제거를 검토한다.

Required user decision:

시연 전 코드 안정화를 위해 CSRF 구성을 단순화할지, 현재 테스트 통과 상태를 유지하고 시연 전 쿠키 삭제 및 실제 브라우저 리허설로 대응할지 결정이 필요하다.

## Recommendation

시연 안정성이 가장 중요하면 다음 중 하나를 선택한다.

1. 시연 전용 profile에서만 CSRF 비활성화
   - 장점: 상태 변경 요청의 시연 실패 가능성이 가장 낮다.
   - 단점: 공식 명세의 CSRF 보안 계약과 달라진다.

2. CSRF 유지 및 시연 전 리허설
   - 장점: 공식 명세와 구현을 유지한다.
   - 단점: 브라우저 쿠키/헤더 상태가 꼬이면 `403`이 재발할 수 있다.
   - 필수 절차: 브라우저 쿠키 삭제, 서버 재시작, 로그인, 게시글 작성, 댓글 작성, 일정 추가를 같은 브라우저에서 순서대로 확인한다.

3. CSRF 유지 및 구성 단순화
   - 장점: 보안 계약을 유지하면서 원인 추적 지점을 줄인다.
   - 단점: 추가 코드 변경과 회귀 테스트가 필요하다.

현재 코드 기준 최소 권장안은 2번이다. 시연 실패 비용이 크다면 1번을 별도 시연 profile로만 적용하는 것이 가장 안정적이다.

