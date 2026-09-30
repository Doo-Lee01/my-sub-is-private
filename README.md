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
src/test/java/          JUnit 테스트 17개
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
