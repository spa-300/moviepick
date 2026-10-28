# MoviePick

영화 탐색 · 리뷰 커뮤니티 플랫폼

## 기술 스택
Java 17 · Spring Boot 4.1.1 · Spring Data JPA · MySQL 8.0 · Redis 7.2 · Docker · Gradle

## 실행 방법 (로컬 개발)
팀 전원이 같은 Docker MySQL·Redis를 쓴다. PC에 설치된 MySQL(3306)·Redis(6379)와 겹치지 않게 MySQL은 3307, Redis는 6380으로 뜬다.

1. Docker Desktop을 켠다
2. 프로젝트 맨 위에서 `.env.example`을 `.env`로 복사한다 (Windows: `Copy-Item .env.example .env`, Mac: `cp .env.example .env`)
   - `JWT_SECRET`, `TMDB_ACCESS_TOKEN`, PortOne 값 등 비밀값은 `.env`에 채운다. 받는 곳은 파일 안 안내 참고. `.env`는 저장소에 올라가지 않는다
3. (선택) 시연용 영화 데이터 파일 `movies_data.sql`을 팀에서 받아 `db/seed/`에 둔다. 없으면 영화 없이 시작한다
4. `docker compose up -d` → `docker compose ps`에서 두 컨테이너가 `healthy`인지 확인
   - 처음 만들어질 때 `db/schema.sql` → `db/data.sql`(관리자 계정) → `db/seed/*.sql` 순서로 자동 실행된다
5. `MoviePickApplication` 실행 (또는 `./gradlew bootRun`). 앱은 `.env`를 읽어 Docker DB에 붙는다

### 이전 방식에서 옮겨 올 때
- **`application-local.yml`은 더 이상 필요 없다.** 남아 있으면 그 안의 `spring.datasource`가 우선해서 예전 DB(3306)에 붙으므로, datasource 부분을 지우거나 파일째 삭제한다
- 그 파일이나 다른 곳에 있던 비밀값(TMDB 토큰, PortOne 값 등)은 `.env`로 옮긴다
- IntelliJ 실행 설정(Run/Debug Configurations)의 환경 변수에 같은 값(`JWT_SECRET`, `TMDB_ACCESS_TOKEN` 등)이 있으면 지운다. **환경 변수가 `.env`보다 우선**해서 `.env`를 고쳐도 반영되지 않는다

### DB 구조가 바뀌었을 때
- DB 구조는 `db/schema.sql` 하나로 관리하고, 앱은 `ddl-auto: validate`로 엔티티와 DB가 같은지만 확인한다
- 엔티티를 바꾼 사람은 같은 PR에서 `db/schema.sql`도 고친다
- dev를 받은 뒤 앱이 validate 오류(`Schema-validation: ...`)로 켜지지 않으면 DB를 새로 만든다
  ```bash
  docker compose down -v
  ```
  ```bash
  docker compose up -d
  ```
  `-v`를 빼면 예전 DB가 남아 새 구조가 적용되지 않는다. 직접 넣은 데이터는 지워진다

## 개발 참고
- 응답은 `ApiResponse`, 예외는 `BusinessException` + 도메인별 `ErrorCode`를 사용한다
- 테스트는 H2로 돈다 (`src/test/resources/config/application.yml`)
