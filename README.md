# MoviePick

영화 탐색 · 리뷰 커뮤니티 플랫폼

## 기술 스택
Java 17 · Spring Boot 4.1.1 · Spring Data JPA · MySQL · Gradle

## 실행 방법
1. 본인 MySQL에 DB 생성
   ```sql
   CREATE DATABASE moviepick DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
   ```
2. `src/main/resources/application-local.yml.example`을 같은 폴더에 `application-local.yml`로 복사하고 MySQL 아이디·비밀번호 입력
3. `MoviePickApplication` 실행 (또는 `./gradlew bootRun`)

## 개발 참고
- 테이블은 엔티티를 기준으로 자동 생성된다 (`ddl-auto: update`)
- 작성자 ID는 요청 헤더 `X-Member-Id`로 전달한다 (예: `X-Member-Id: 1`)
- 응답은 `ApiResponse`, 예외는 `BusinessException` + 도메인별 `ErrorCode`를 사용한다
