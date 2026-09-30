# 📵 제 부계는 private입니다

> 전 애인 인스타 염탐 차단 시스템으로 배우는 **객체지향 설계 · 데이터베이스 · 배포**

### 🖥️ [발표 자료 바로가기](https://doo-lee01.github.io/my-sub-is-private/#1)

| 버전 | 내용 | 위치 |
|---|---|---|
| **v1** | 클래스·생성자·this·캡슐화·배열로 만든 1시간 실습 | [`v1/`](v1) |
| **v2** | 계층 구조 + PostgreSQL(Supabase) + 웹 화면 + 배포 | 이 폴더 전체 |

<br>

## ✨ v2에서 달라진 점

| v1 | v2 | 새로 쓰는 개념 |
|---|---|---|
| 아이디 = `String` | `Handle` 값 객체 (생성자에서 검증, 불변) | `final`, `equals`/`hashCode` |
| `String[5]` + `blockedCount` | `BlockList` (`ArrayList`, 읽기 전용 복사본 제공) | 컬렉션, 방어적 복사 |
| `receiveDM()` 안의 if문 줄줄이 | `DmRule` 인터페이스 + 규칙 클래스 5개 | 인터페이스, 다형성 |
| 결과 = `"자동차단"` 문자열 | `DmResult` enum (라벨·설명 포함) | enum |
| 잘못된 값 → `println` 경고 | `InstaException` 계열 예외 | 예외, 상속 |
| 메모리에만 저장 | `AccountRepository` 인터페이스 → 메모리 / JDBC 구현 | Repository 패턴, JDBC |
| 한 클래스가 판단·출력 모두 | 화면 → 서비스 → 도메인 → 저장소 | 계층 분리 |
| 콘솔 | 콘솔 + 웹 화면 (같은 서비스 공유) | HTTP, JSON |

<br>

## 🏗️ 구조

```
 [콘솔 앱]   [웹 화면 ← 브라우저]
      \          /
     ConsoleApp  WebApp          ← 보여주기만 해요
          \      /
        InstaService              ← 일의 순서 (불러오기 → 판단 맡기기 → 저장)
          /      \
   Account ·      AccountRepository (인터페이스)
   DmRule ·        ├─ MemoryAccountRepository   ← DB 없이 연습
   BlockList       └─ JdbcAccountRepository     ← Supabase(PostgreSQL)
   (규칙 판단)
```

```
src/main/java/io/github/doolee01/msp/
├── domain/        Account, Handle, BlockList, DirectMessage, DmResult, ...
│   └── rule/      DmRule(인터페이스) + 규칙 5개
├── repository/    AccountRepository, MessageRepository(인터페이스)
│   ├── memory/    메모리 구현
│   └── jdbc/      PostgreSQL 구현 + Database(접속 정보)
├── service/       InstaService, DemoData, 예외
├── console/       ConsoleApp
├── web/           WebApp, Json
├── AppContext     어떤 저장소를 쓸지 정하는 조립 공장
└── Launcher       jar 실행 시작점
src/main/resources/
├── public/index.html   웹 화면
└── db/schema.sql       테이블 만드는 SQL
src/test/java/          JUnit 테스트 29개
```

<br>

## ▶️ 실행하기 (이클립스)

1. `File` → `Import` → `Maven` → `Existing Maven Projects` → 이 폴더 선택
2. **DB 없이 바로:** `WebApp.java` 우클릭 → `Run As` → `Java Application` → 브라우저에서 http://localhost:8080
3. **콘솔 버전:** `ConsoleApp.java` 우클릭 → `Run As` → `Java Application`
4. **테스트:** `src/test/java` 우클릭 → `Run As` → `JUnit Test`

환경 변수 `DB_URL`이 없으면 **메모리 모드**, 있으면 **DB 모드**로 실행돼요. 화면 오른쪽 위 배지에서 확인할 수 있어요.

### DB 모드로 실행하기

`Run` → `Run Configurations` → `WebApp` → `Environment` 탭에 추가해요.

| 이름 | 값 (Supabase → Connect → Session pooler에서 확인) |
|---|---|
| `DB_URL` | `jdbc:postgresql://<pooler 호스트>:5432/postgres?sslmode=require` |
| `DB_USER` | `postgres.<프로젝트 ref>` |
| `DB_PASSWORD` | 프로젝트 만들 때 정한 비밀번호 |

> ⚠️ 비밀번호는 절대 코드나 README에 적지 마세요. 환경 변수로만 넣어요.

<br>

## 🌐 API

| 방식 | 주소 | 하는 일 |
|---|---|---|
| GET | `/api/health` | 서버 상태, 저장소 종류 |
| GET | `/api/accounts` | 계정 목록 |
| POST | `/api/dm` | DM 보내기 (`from`, `to`, `content`, `hour`) |
| GET | `/api/profile` | 프로필 방문 (`viewer`, `target`) |
| GET · POST | `/api/blocks` | 차단 목록 보기 · 차단하기 (`owner`, `target`) |
| POST | `/api/blocks/delete` | 차단 풀기 |
| GET | `/api/inbox` · `/api/attempts` | 받은 DM · 모든 시도 기록 |
| POST | `/api/reset` | 데모 데이터로 초기화 |

<br>

## ⚠️ 예외 처리

### 예외 계층

```
Throwable
├── Exception                            ← checked: 처리하지 않으면 컴파일이 안 돼요
│   ├── IOException
│   │   └── BindException                  포트를 이미 누가 쓰고 있을 때 (WebApp)
│   ├── SQLException                       JDBC 오류 (저장소 안에서만 다뤄요)
│   └── DatabaseConnectionException ★     DB 설정·연결 실패 + 해결 방법(hint)
└── RuntimeException                     ← unchecked: 처리를 강제하지 않아요
    ├── IllegalArgumentException           잘못된 요청 값 (PORT, hour 등)
    ├── InstaException ★                   규칙 위반의 부모
    │   ├── InvalidHandleException ★
    │   ├── AccountNotFoundException ★
    │   └── DuplicateHandleException ★
    ├── DataAccessException ★              DB 작업 실패 (SQLException을 감쌈)
    └── DuplicateKeyException ★            저장소의 아이디 중복
★ = 직접 만든 예외
```

### 어디서 무엇을 쓰는지

| 개념 | 위치 | 하는 일 |
|---|---|---|
| checked 예외 + `throws` | `Database.verifyConnection()` → `AppContext.create()` | DB 연결 실패를 부르는 쪽이 반드시 처리하게 강제 |
| `try-catch` 여러 개, 자식 먼저 | `WebApp.main()` | `BindException`을 `IOException`보다 먼저 잡아 포트 충돌을 안내 |
| 멀티 catch `A \| B` | `WebApp.handle()` | `InstaException`과 `IllegalArgumentException`을 한 번에 400으로 |
| `finally` | `WebApp.handle()` | 성공·실패와 상관없이 요청마다 로그 한 줄 (`📨 POST /api/dm → 400 (2ms)`) |
| `try-with-resources` | `Database`, `Jdbc*Repository`, `ConsoleApp.main()` | `Connection`, `ResultSet`, `Scanner`를 자동으로 닫기 |
| 예외 체이닝 (`cause`) | `parsePort()`, `DuplicateHandleException` | 원래 원인을 버리지 않고 함께 넘기기 |
| 예외 변환 | `JdbcAccountRepository` → `InstaService` | `SQLException(23505)` → `DuplicateKeyException` → `DuplicateHandleException` |
| `addSuppressed` | `JdbcAccountRepository.rollbackQuietly()` | 되돌리기마저 실패해도 원래 오류를 잃지 않기 |
| 정상 종료로 처리 | `ConsoleApp.run()` | 입력이 끝나면(`NoSuchElementException`) 오류 대신 조용히 종료 |

### 예외로 만들지 않은 것

차단 목록이 꽉 찬 경우(`BlockResult.LIST_FULL`)나 새벽 "자니"로 차단된 경우(`DmResult.AUTO_BLOCKED`)는 예외가 아니라 enum 결과로 돌려줘요. 서비스가 원래 하는 일 안에서 **예상되는 결과**이기 때문이에요. 예외는 잘못된 입력, 없는 계정, DB 장애처럼 **정상 흐름을 이어갈 수 없을 때**만 써요.

### 실행하면 이렇게 보여요

스택 트레이스 대신 무엇이 잘못됐는지와 고치는 방법을 보여줘요.

```
❌ DB 연결 실패: DB_URL이 jdbc: 로 시작하지 않아요
💡 Supabase에서 복사한 주소 앞에 jdbc: 를 붙이고, 아이디·비밀번호 부분(postgres.xxx:[...]@)은 빼서 DB_USER와 DB_PASSWORD에 따로 넣으세요.

❌ 8080번 포트를 이미 다른 프로그램이 쓰고 있어요
💡 이클립스 Console 창에서 이전에 실행한 서버를 빨간 정지 버튼으로 끄거나, Run Configurations → Environment에 PORT=8081 을 넣고 http://localhost:8081 로 접속하세요
```

<br>

## 🚀 배포

`Dockerfile.vercel` 하나로 Vercel과 Render 모두 배포할 수 있어요. 자세한 순서는 가이드 문서를 참고하세요.

<br>

## 👩‍💻 팀원

| 이름 | 역할 |
|---|---|
| 이름 | |
| 이름 | |

---

**숨길 건 숨기고, 들어오는 건 검증하고, 판단과 저장은 나눈다.** 🔐
