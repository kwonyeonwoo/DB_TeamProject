# Free Tier Deployment Change Plan

작성일: 2026-05-21

## 목적

Vercel Hobby, Render Free Web Service, Aiven MySQL Free 조합으로 시연 배포를 진행하기 전에 현재 코드에서 필요한 변경 사항을 정리한다.

이 문서는 배포 준비 계획이며, 백엔드 기능 구현 범위가 아니다. 실제 코드 변경은 별도 작업으로 진행한다.

이 문서의 체크리스트를 위에서부터 실행하면 시연 배포를 재현할 수 있도록, 서비스별 프로젝트 루트, 필수 환경변수, 배포 후 검증 API까지 함께 명시한다.

## 대상 배포 구조

```text
[Vercel Hobby]
React/Vite frontend
Root Directory: frontend
/api/* requests rewritten to Render

        ↓

[Render Free Web Service]
Spring Boot backend
Docker deployment

        ↓

[Aiven MySQL Free]
External MySQL database
```

## 현재 코드 상태 요약

- 프론트엔드 API 클라이언트는 기본 API base URL을 `/api`로 사용한다.
  - 대상 파일: `frontend/src/api/client.ts`
  - 현재 설정: `VITE_API_BASE_URL`이 없으면 `/api` 사용
- 프론트엔드는 Vite 앱이며 빌드 산출물은 기본적으로 `dist` 디렉터리에 생성된다.
  - 대상 파일: `frontend/package.json`
  - 빌드 명령: `npm run build`
- Vite 개발 서버는 로컬 개발용 프록시를 이미 가지고 있다.
  - 대상 파일: `frontend/vite.config.ts`
  - `/api`, `/uploads` 요청을 기본적으로 `http://localhost:8080`으로 프록시
- 프론트엔드는 백엔드가 반환하는 `/uploads/*` 첨부파일 URL을 화면에서 `/api/uploads/*`로 변환한다.
  - 대상 파일: `frontend/src/pages/PostDetailPage.tsx`, `frontend/src/pages/PostWritePage.tsx`
  - 따라서 배포 rewrite는 `/api/*`만 잡아도 첨부파일 조회 요청이 Render 백엔드로 전달된다.
- 백엔드는 이미 `/api` context path를 사용한다.
  - 대상 파일: `backend/src/main/resources/application.yml`
  - 현재 설정: `server.servlet.context-path: /api`
- 백엔드는 MySQL JDBC, Flyway, JPA 구성이 이미 포함되어 있다.
  - 대상 파일: `backend/build.gradle`
  - 포함 항목: `flyway-core`, `flyway-mysql`, `mysql-connector-j`
- 현재 저장소에는 Vercel 배포 설정 파일과 Dockerfile이 없다.
  - 없음: `frontend/vercel.json`
  - 없음: `backend/Dockerfile`

## 필수 변경 사항

### 1. Vercel rewrite 설정 추가

추가 파일:

```text
frontend/vercel.json
```

목적:

- Vercel에 배포된 React/Vite 앱에서 `/api/*` 요청을 Render 백엔드로 전달한다.
- 브라우저 주소는 Vercel 도메인을 유지하고, 실제 API 요청만 Render로 프록시한다.
- React Router 새로고침 대응을 위해 SPA fallback을 둔다.

Vercel 프로젝트 설정:

- Import 대상 repository는 현재 저장소로 선택한다.
- Root Directory: `frontend`
- Framework Preset: `Vite`
- Install Command: `npm ci`
- Build Command: `npm run build`
- Output Directory: `dist`
- Environment Variables:
  - `VITE_API_BASE_URL=/api`
  - `VITE_USE_MOCK_API=false`
  - `VITE_USE_DEMO_AUTH_FALLBACK=false`

예시:

```json
{
  "$schema": "https://openapi.vercel.sh/vercel.json",
  "rewrites": [
    {
      "source": "/api/:path*",
      "destination": "https://YOUR-RENDER-SERVICE.onrender.com/api/:path*"
    },
    {
      "source": "/(.*)",
      "destination": "/index.html"
    }
  ]
}
```

주의:

- `YOUR-RENDER-SERVICE`는 Render에서 생성된 실제 서비스 이름으로 교체한다.
- 프론트엔드의 `VITE_API_BASE_URL`은 배포 환경에서도 `/api`로 유지하는 것을 권장한다.
- `VITE_API_BASE_URL`을 Render 절대 URL로 바꾸면 쿠키 기반 세션/CSRF 흐름에서 cross-site 이슈가 커진다.
- `frontend/vercel.json`은 Vercel Root Directory가 `frontend`일 때 프로젝트 루트의 `vercel.json`으로 인식된다. Root Directory를 저장소 루트로 두면 이 파일을 읽지 못할 수 있다.
- `/api/*` rewrite는 외부 origin으로 프록시되는 요청이므로, 배포 후 Render 응답 지연과 Vercel proxied request timeout을 함께 확인한다.

### 2. Render Docker 배포 파일 추가

추가 파일:

```text
backend/Dockerfile
```

목적:

- Render에서 Spring Boot 백엔드를 Docker Web Service로 빌드 및 실행한다.
- Java 17 런타임을 사용한다.

예시:

```dockerfile
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
COPY src src
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Render 설정:

- Service Type: Web Service
- Runtime: Docker
- Root Directory: `backend`
- Instance Type: Free
- Health Check Path: `/api/posts`
- Environment Variables:
  - `DB_URL`
  - `DB_USERNAME`
  - `DB_PASSWORD`
  - `SPRING_PROFILES_ACTIVE`

Render profile 권장:

- 실제 배포 보안 기준: `SPRING_PROFILES_ACTIVE`를 비워 두거나 운영용 profile을 별도로 만든다.
- 시연 안정성을 우선해 CSRF를 우회해야 하는 경우에만 `SPRING_PROFILES_ACTIVE=demo`를 사용한다.
- `demo` 또는 `local-demo` profile은 CSRF 검증을 비활성화하므로, 실제 운영 배포로 설명하면 안 된다.

### 3. Render PORT 환경변수 대응

수정 파일:

```text
backend/src/main/resources/application.yml
```

현재 백엔드는 `server.servlet.context-path`만 지정되어 있다. Render는 실행 포트를 `PORT` 환경변수로 전달하므로 Spring Boot가 이 값을 사용하도록 `server.port`를 추가한다.

수정 예시:

```yaml
server:
  port: ${PORT:8080}
  servlet:
    context-path: /api
```

목적:

- 로컬에서는 기본 `8080`으로 실행한다.
- Render에서는 Render가 주입한 `PORT`로 실행한다.

### 4. Aiven MySQL 연결 환경변수 설정

코드 수정은 원칙적으로 필요하지 않다. 현재 `application.yml`은 환경변수 기반 DB 연결을 지원한다.

Render 환경변수:

```text
DB_URL=jdbc:mysql://<AIVEN_HOST>:<AIVEN_PORT>/<AIVEN_DATABASE>?sslmode=require&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
DB_USERNAME=<AIVEN_USERNAME>
DB_PASSWORD=<AIVEN_PASSWORD>
```

일반적인 Aiven 기본값 예시:

```text
DB_USERNAME=avnadmin
```

주의:

- Aiven 콘솔의 Connection information에서 host, port, database, username, password를 확인한다.
- Aiven MySQL은 TLS 연결이 필요하므로 JDBC URL에 `sslmode=require`를 포함한다.
- Flyway가 애플리케이션 시작 시 마이그레이션을 실행하므로, 연결 계정은 테이블 생성/변경 권한이 있어야 한다.
- 최초 배포 전 Aiven DB에 기존 테이블이 있으면 Flyway baseline/마이그레이션 충돌이 날 수 있으므로, 비어 있는 database를 사용한다.

## 권장 변경 사항

### 1. 배포 문서 또는 README 업데이트

수정 후보:

```text
backend/README.md
frontend/README.md
```

내용:

- Vercel 배포 설정
- Render 배포 설정
- Aiven 환경변수 예시
- 무료 플랜 제한과 시연 전 확인 절차

현재 `backend/README.md`는 인코딩 자체보다 내용 최신화가 필요하다. 예를 들어 "기능 API는 아직 구현하지 않았다"는 설명은 현재 코드 상태와 맞지 않으므로, 배포 문서 작성 시 함께 갱신하는 것이 좋다.

### 2. 운영 프로필 분리 검토

수정 후보:

```text
backend/src/main/resources/application-prod.yml
```

목적:

- 배포 환경 전용 설정을 기본 설정과 분리한다.
- 예: 로그 레벨, 업로드 크기 제한, 세션 쿠키 설정 등

현재 필수는 아니지만, 장기 운영을 고려하면 분리하는 편이 안전하다.

## 무료 플랜 관련 리스크

### Render Free Web Service

- 15분 동안 요청이 없으면 서비스가 sleep 상태가 된다.
- sleep 이후 첫 요청은 응답까지 지연될 수 있다.
- Free instance 사양은 낮으므로 Spring Boot 초기 기동이 느릴 수 있다.
- 로컬 파일시스템은 영구 저장소가 아니다. Free Web Service에서는 재배포, 재시작, spin down 이후 업로드 파일 보존을 기대하면 안 된다.

현재 백엔드 파일 업로드는 로컬 파일시스템에 저장된다.

관련 파일:

```text
backend/src/main/java/com/academicshare/backend/post/service/PostFileStorage.java
backend/src/main/java/com/academicshare/backend/post/config/PostUploadResourceConfig.java
```

영향:

- 게시글 첨부파일은 Render 재시작, 재배포, sleep 이후 사라질 수 있다.
- 시연 중 "업로드 직후 조회"를 보여주는 것은 가능하지만, 장기 보존 기능으로 설명하면 안 된다.
- 파일 업로드를 시연 범위에 포함한다면 발표 직전 새로 업로드한 파일로만 검증한다.

대응 선택지:

- 시연에서는 파일 업로드 기능을 제한적으로만 사용한다.
- 파일 보존이 필요하면 S3, Cloudflare R2 같은 외부 object storage 도입을 별도 기능으로 계획한다.

### Vercel Hobby

- 개인/비상업 용도에 맞는 플랜이다.
- `/api/*` rewrite는 가능하지만 외부 백엔드 응답이 너무 느리면 timeout이 발생할 수 있다.
- API 요청은 Vercel을 경유하므로, Vercel과 Render 양쪽의 사용량 제한을 모두 고려해야 한다.
- 현재 배포 목적이 수업/시연이라면 Hobby 사용이 적절하지만, 상업적 또는 조직 운영 목적이면 Pro 이상 사용 여부를 별도 판단해야 한다.

### Aiven MySQL Free

- 무료 플랜 저장 용량이 작으므로 대용량 데이터나 파일 바이너리를 DB에 넣지 않는다.
- 현재 스키마는 Flyway 마이그레이션으로 생성되므로 최초 배포 시 DB가 비어 있어야 안전하다.
- 외부 DB 연결이므로 Render outbound traffic과 DB 접속 제한 정책을 확인해야 한다.

## 배포 전 확인 체크리스트

- [ ] Vercel 프로젝트 Root Directory를 `frontend`로 설정
- [ ] Vercel Framework Preset을 `Vite`로 설정
- [ ] Vercel Build Command를 `npm run build`로 설정
- [ ] Vercel Output Directory를 `dist`로 설정
- [ ] Vercel 환경변수 `VITE_API_BASE_URL=/api` 등록
- [ ] Vercel 환경변수 `VITE_USE_MOCK_API=false` 등록
- [ ] Vercel 환경변수 `VITE_USE_DEMO_AUTH_FALLBACK=false` 등록
- [ ] `frontend/vercel.json` 추가
- [ ] `backend/Dockerfile` 추가
- [ ] `backend/src/main/resources/application.yml`에 `server.port: ${PORT:8080}` 추가
- [ ] Aiven MySQL Free 서비스 생성
- [ ] Aiven DB connection 정보 확보
- [ ] Aiven database가 Flyway 최초 실행에 적합하게 비어 있는지 확인
- [ ] Render Web Service 생성
- [ ] Render Root Directory를 `backend`로 설정
- [ ] Render Runtime을 Docker로 설정
- [ ] Render Health Check Path를 `/api/posts`로 설정
- [ ] Render 환경변수 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` 등록
- [ ] Render에서 `SPRING_PROFILES_ACTIVE` 사용 여부 결정
- [ ] Render 배포 후 `https://<RENDER_SERVICE>.onrender.com/api/posts` 응답 확인
- [ ] Vercel의 rewrite destination을 실제 Render URL로 설정
- [ ] Vercel 배포 후 `/api/posts`가 Vercel 도메인에서 Render로 프록시되는지 확인
- [ ] Vercel 배포 후 회원가입 확인
- [ ] Vercel 배포 후 로그인 확인
- [ ] Vercel 배포 후 게시글 목록 API 확인
- [ ] CSRF를 유지하는 배포라면 게시글 작성, 댓글 작성, 일정 추가 중 하나 이상의 상태 변경 요청 성공 확인
- [ ] CSRF 우회 profile을 사용하는 배포라면 시연 전용 설정임을 발표/문서에서 명확히 구분
- [ ] 첨부파일 기능을 시연 범위에 포함할지 결정
- [ ] 첨부파일을 시연한다면 업로드 직후 이미지 미리보기/다운로드가 되는지 확인
- [ ] Render 재시작 이후 첨부파일 보존을 시연하거나 보장한다고 설명하지 않기
- [ ] 발표 직전 Render 서비스 cold start 방지를 위해 한 번 접속

## 검증 명령

프론트엔드 빌드:

```powershell
cd frontend
npm run build
```

백엔드 테스트:

```powershell
cd backend
.\gradlew.bat test
```

백엔드 로컬 실행:

```powershell
cd backend
.\gradlew.bat bootRun
```

배포 후 수동 확인 URL:

```text
Render 직접 확인:
https://<RENDER_SERVICE>.onrender.com/api/posts

Vercel rewrite 확인:
https://<VERCEL_DOMAIN>/api/posts
```

배포 후 브라우저 확인 흐름:

1. Vercel 도메인 접속
2. 회원가입
3. 로그인
4. 게시글 목록 조회
5. 게시글 작성 또는 댓글 작성으로 상태 변경 API 확인
6. 첨부파일 시연 포함 시 파일 업로드 직후 미리보기/다운로드 확인

## 이번 검토에서 확인한 결과

- `npm run build` 성공
- `.\gradlew.bat test` 성공
- git working tree는 문서 작성 전 기준 clean 상태였고, 현재 이 문서가 새 파일로 추가된 상태다.

## 남은 결정 사항

1. Render 서비스명을 확정해야 Vercel rewrite destination을 확정할 수 있다.
2. Aiven database 이름을 확정해야 `DB_URL`을 확정할 수 있다.
3. 첨부파일 영구 보존이 시연 요구사항인지 결정해야 한다.
4. 운영 배포에서 `demo` 또는 `local-demo` CSRF 우회 프로필을 사용할지 결정해야 한다.
   - 단순 시연 편의만 보면 사용할 수 있다.
   - 실제 배포 보안 기준으로는 사용하지 않는 것이 맞다.
5. 장기 운영을 염두에 두면 `application-prod.yml`을 만들지 결정해야 한다.
   - 단순 무료 시연 배포에는 필수는 아니다.
   - 운영 로그 레벨, 업로드 제한, 세션 쿠키 정책을 분리하려면 별도 profile이 안전하다.
