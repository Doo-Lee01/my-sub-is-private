# 📵 교수님, 제 부계는 private입니다

> 전 애인 인스타 염탐 차단 시스템으로 배우는 **클래스·객체·캡슐화**

[프레젠테이션 바로가기](https://doo-lee01.github.io/my-sub-is-private/#1)

이별 후 새 출발을 한 주인공. 본계는 그대로 두고, 진짜 일상은 부계에만 올려요.
전 애인은 어떻게든 부계를 찾아내고 새벽에 "자니?"를 보내려 하지만,
**잘 캡슐화된 코드**가 전부 막아냅니다.

Java 객체지향 기초를 복습하기 위해 2인 팀이 60분 동안 설계부터 구현까지 진행한 미니 프로젝트예요.

<br>

## 🎯 다룬 개념

| 인스타 세계 | Java 개념 | 코드 |
|---|---|---|
| 계정을 만드는 틀 | 클래스 | `InstaAccount` |
| 나의 계정, 전 애인 계정 | 객체 | `new InstaAccount(...)` |
| 계정 개설 | 생성자 + `this` | `this.mainId = mainId;` |
| 본계: 아이디는 누구나 앎 | `private` + getter 있음 | `getMainId()` |
| 부계: 존재 자체를 모름 | `private` + getter 없음 | `me.subId` → 컴파일 에러 |
| 차단 목록 (최대 5명) | `private` 배열 + setter 없음 | `blockedIds[]` |
| 새벽 "자니?" 자동 차단 | 검증 메서드 | `receiveDM()` |
| 여러 명 차단 | 배열 + 반복문 + 조건문 | `block()`, `isBlocked()` |

<br>

## 📁 폴더 구조

```
my-sub-is-private/
├── src/
│   ├── InstaAccount.java       # 캡슐화가 잘 된 계정 클래스
│   ├── LeakyInstaAccount.java  # 필드가 전부 public인 비교용 클래스 (4막)
│   ├── Main.java               # 1막~시즌 2 발표 시연 시나리오
│   └── DmSimulator.java        # 직접 입력해보는 DM 시뮬레이터
├── docs/
│   └── index.html              # 발표 슬라이드
├── .gitignore
└── README.md
```

<br>

## 🧩 클래스 설계

```
┌─────────────────────────────────┐
│          InstaAccount           │
├─────────────────────────────────┤
│ - owner     : String            │
│ - mainId    : String   본계     │
│ - subId     : String   부계 🤫  │
│ - blockedIds : String[] 차단 🔒 │
│ - blockedCount : int            │
├─────────────────────────────────┤
│ + getOwner()                    │
│ + getMainId()                   │
│ + getBlockedCount()             │
│ + introduce()                   │
│ + block(targetId)               │
│ + printBlockList()              │
│ + viewFeed(visitor)             │
│ + receiveDM(sender, msg, hour)  │
│ - validateId(id)                │
│ - isBlocked(id)                 │
│ - isMine(id)                    │
│ - isDawn(hour)                  │
└─────────────────────────────────┘
```

- 필드는 **전부 `private`**, 밖에서 꼭 필요한 기능만 `public` 메서드로 열었어요.
- `getSubId()`와 `setBlockedIds()`는 **일부러 만들지 않았어요.** getter/setter를 안 만드는 것도 설계예요.
- 배열을 통째로 돌려주는 `getBlockedIds()`도 없어요. 대신 `getBlockedCount()`로 인원 수만 공개해요.
- 차단은 `block()`에서 **내 계정인지, 이미 차단했는지, 목록이 꽉 찼는지** 검증한 뒤에만 추가돼요.
- 검증 메서드도 `private`이에요. 계정 안에서만 쓰는 도구라 밖에 보여줄 이유가 없어요.

<br>

## 🎬 시연 시나리오

| 막 | 제목 | 보여주는 개념 |
|---|---|---|
| 1막 | 새 출발 | 같은 클래스로 서로 다른 객체 생성, 생성자에서 아이디 검증 |
| 2막 | 새벽 3시의 습격 | 검증 메서드로 "자니?" 자동 차단, 차단 후 "사용자를 찾을 수 없습니다." |
| 3막 | 부계를 찾아서 | `private` 필드 접근 시 컴파일 에러 |
| 4막 | 캡슐화가 무너진 세계 | `public` 필드일 때 부계 노출 + 차단 셀프 해제 |
| 시즌 2 | 부계로 돌아온 전 애인 | 배열로 여러 명 차단, 중복·본인·정원 초과 검증 |
| 관객 참여 | 당신이 전 애인이라면? | `DmSimulator`로 직접 DM을 입력해 검증 로직 체험 |

> 💡 3막은 `Main.java`에 주석 처리된 세 줄을 하나씩 풀면 컴파일 에러를 직접 볼 수 있어요.

<br>

## ▶️ 실행 방법

### 이클립스

1. `File` → `Import` → `General` → `Existing Projects into Workspace`로 가져오거나,
   새 Java Project를 만든 뒤 `src` 폴더에 파일 3개를 넣어요.
2. `Main.java` 우클릭 → `Run As` → `Java Application` (발표 시나리오)
3. `DmSimulator.java` 우클릭 → `Run As` → `Java Application` (직접 입력 모드, 콘솔 창에 입력)

> ⚠️ 한글이나 이모지가 깨지면
> - `Window` → `Preferences` → `General` → `Workspace` → **Text file encoding: UTF-8**
> - `Run` → `Run Configurations` → `Common` 탭 → **Encoding: UTF-8**

### 터미널

```bash
cd src
javac -encoding UTF-8 *.java
java Main          # 발표 시나리오
java DmSimulator   # 직접 입력 모드
```

<br>

## 📺 실행 결과 (2막)

```
💬 [21시] @ex_boy_99: 잘 지내?
   📩 DM 도착
💬 [3시] @ex_boy_99: 자니?
   🚨 새벽 '자니?' 감지!
   🔒 @ex_boy_99 님을 차단했어요 (1/5)
👀 @ex_boy_99 → @new_me_2026 피드 방문
   사용자를 찾을 수 없습니다.
💬 [14시] @ex_boy_99: 왜 차단해...
   📭 전송 실패: 차단된 사용자예요
```

<br>

## 🔍 심화: private은 클래스 단위

Java의 `private`은 **객체마다**가 아니라 **클래스마다** 막아요.
그래서 `InstaAccount` 클래스 **안**에서는 다른 `InstaAccount` 객체의 `subId`도 읽을 수 있어요.

```java
// InstaAccount 클래스 안에 이런 메서드를 만들면
public void spy(InstaAccount target) {
    System.out.println(target.subId);  // 컴파일 OK 😱
}
```

3막에서 막혔던 건 `Main`이라는 **다른 클래스**에서 접근했기 때문이에요.

배열 필드도 조심해야 해요. 필드가 `private`이어도 getter로 배열을 그대로 돌려주면 밖에서 안의 값을 바꿀 수 있어요.

```java
public String[] getBlockedIds() { return blockedIds; }
// 밖에서 me.getBlockedIds()[0] = ""; → 차단이 풀려요 😱
```

결국 진짜 보안은 **클래스를 설계하는 사람의 책임**이에요.

<br>

## 🖥️ 발표 자료

`docs/index.html`을 브라우저로 열면 돼요. (←/→ 이동, `F` 전체 화면)

<br>

## 👩‍💻 팀원

| 이름 | 역할 |
|---|---|
| 이름 | 드라이버 · 코드 작성 · 시연 |
| 이름 | 내비게이터 · 설계 · 심화 리뷰 |

<br>

---

**숨길 건 숨기고, 들어오는 건 검증한다.** 🔐
