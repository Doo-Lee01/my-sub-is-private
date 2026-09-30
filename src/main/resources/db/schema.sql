-- ============================================================
--  my-sub-is-private v2 · 데이터베이스 설계
--  Supabase 대시보드 → SQL Editor → New query 에 전부 붙여넣고 Run
-- ============================================================

-- 1) 계정: Account 객체 한 개 = 이 표의 한 줄
create table if not exists accounts (
    id            bigint generated always as identity primary key,
    handle        varchar(30) not null unique
                  check (handle ~ '^[a-z0-9._]{1,30}$'),       -- Handle 클래스와 같은 규칙
    display_name  varchar(50) not null,
    owner_key     varchar(30) not null,                       -- 같은 사람의 계정끼리 같은 값
    account_type  varchar(4)  not null check (account_type in ('MAIN', 'SUB')),
    created_at    timestamptz not null default now()
);

-- 2) 차단 목록: BlockList 안의 Handle 하나 = 이 표의 한 줄
--    (차단당한 사람이 우리 서비스에 가입하지 않았을 수도 있어서 아이디 글자로 저장해요)
create table if not exists blocks (
    id              bigint generated always as identity primary key,
    blocker_id      bigint      not null references accounts(id) on delete cascade,
    blocked_handle  varchar(30) not null,
    created_at      timestamptz not null default now(),
    unique (blocker_id, blocked_handle)                       -- 같은 사람을 두 번 차단 못 하게
);

-- 3) DM 기록: 도착한 것뿐 아니라 거절된 시도도 결과와 함께 남겨요
create table if not exists messages (
    id             bigint generated always as identity primary key,
    sender_handle  varchar(30)  not null,
    receiver_id    bigint       not null references accounts(id) on delete cascade,
    content        varchar(500) not null,                     -- DirectMessage.MAX_LENGTH와 같게
    sent_hour      smallint     not null,
    result         varchar(20)  not null check (result in
                   ('DELIVERED','INVALID_HOUR','EMPTY_MESSAGE','TOO_LONG','SENDER_BLOCKED','AUTO_BLOCKED')),
    created_at     timestamptz  not null default now()
);

create index if not exists messages_receiver_idx on messages (receiver_id, id);

-- 4) 보안: Supabase는 public 스키마의 표를 인터넷 API(Data API)로도 열어줘요.
--    우리는 자바 서버가 DB에 직접 접속하므로, 인터넷 API로는 아무도 못 읽게 RLS를 켜둬요.
--    (정책을 하나도 만들지 않으면 anon 키로는 읽기·쓰기가 전부 막혀요)
alter table accounts enable row level security;
alter table blocks   enable row level security;
alter table messages enable row level security;
