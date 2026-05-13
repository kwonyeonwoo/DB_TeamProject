# Academic Share Backend

Spring Boot 기반 백엔드 프로젝트 골격입니다. 현재 범위는 공통 기반 생성이며, 인증/회원/게시글 같은 기능 API는 아직 구현하지 않았습니다.

## 기술 스택

- Java 17+
- Spring Boot
- Spring Web
- Spring Data JPA
- MySQL 또는 MariaDB
- Gradle
- JUnit 5

## 프로젝트 구조

```text
backend
├─ build.gradle
├─ settings.gradle
├─ src
│  ├─ main
│  │  ├─ java/com/academicshare/backend
│  │  │  ├─ AcademicShareApplication.java
│  │  │  ├─ auth
│  │  │  ├─ comment
│  │  │  ├─ common
│  │  │  │  ├─ error
│  │  │  │  ├─ exception
│  │  │  │  └─ response
│  │  │  ├─ group
│  │  │  ├─ notification
│  │  │  ├─ post
│  │  │  ├─ report
│  │  │  ├─ schedule
│  │  │  └─ user
│  │  └─ resources
│  │     ├─ application.yml
│  │     └─ application-local.yml.example
│  └─ test
│     ├─ java/com/academicshare/backend
│     └─ resources/application-test.yml
└─ README.md
```

## 실행 방법

Gradle Wrapper가 포함되어 있어 로컬 Gradle 설치 없이 실행할 수 있습니다.

1. MySQL 또는 MariaDB에 `academic_share` 데이터베이스를 준비합니다.
2. 환경 변수를 설정합니다.

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/academic_share?serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
$env:DB_USERNAME="your_user"
$env:DB_PASSWORD="your_password"
```

3. 백엔드를 실행합니다.

```powershell
cd backend
.\gradlew.bat bootRun
```

API base path는 `/api`입니다. 예를 들어 향후 로그인 API는 `/api/auth/login` 경로를 사용합니다.

## 테스트 방법

테스트는 외부 MySQL 연결 없이 H2 인메모리 DB와 `test` 프로필로 실행되도록 구성했습니다.

```powershell
cd backend
.\gradlew.bat test
```

## 공통 응답 정책

- 실패 응답은 명세에 따라 `code`, `message`를 필수로 포함합니다.
- 입력값별 오류가 필요한 경우에만 `details`를 포함합니다.
- `ItemsResponse`, `PageResponse`는 목록/페이지 응답 계약을 재사용하기 위한 공통 타입입니다.
- 기능 API 구현 시에는 `docs/normalized/api-contract.md`의 개별 응답 형식을 우선합니다.

## DB 설정

기본 DB 설정은 `src/main/resources/application.yml`에 환경 변수 기반으로 작성되어 있습니다. 실제 비밀번호나 시크릿은 파일에 저장하지 않습니다.

로컬 예시는 `src/main/resources/application-local.yml.example`을 참고하세요.
