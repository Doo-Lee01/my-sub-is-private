package io.github.doolee01.msp.repository;

import java.util.List;
import java.util.Optional;

import io.github.doolee01.msp.domain.Account;
import io.github.doolee01.msp.domain.Handle;

/**
 * 계정을 "어딘가에" 저장하고 꺼내오는 창구예요. (Repository 패턴)
 *
 * 인터페이스로 만든 이유
 *  - 서비스(InstaService)는 "저장해줘, 찾아줘"만 알면 되고, 실제로 어디에 저장하는지는 몰라도 돼요.
 *  - 그래서 구현체를 갈아끼울 수 있어요.
 *      MemoryAccountRepository : 프로그램 메모리(HashMap)에 저장 → 껐다 켜면 사라짐. 연습·테스트용
 *      JdbcAccountRepository   : PostgreSQL(Supabase)에 저장 → 껐다 켜도 남아 있음. 실제 서비스용
 *  - 서비스 코드는 한 줄도 안 바꾸고 저장 방식만 바꿀 수 있어요. 이게 다형성의 진짜 쓸모예요.
 */
public interface AccountRepository {

    /** 새 계정이면 번호(id)를 붙여 저장하고, 이미 있는 계정이면 차단 목록을 최신으로 맞춰요 */
    Account save(Account account);

    /** Optional = "있을 수도, 없을 수도 있는 값"을 담는 상자. null 대신 써요 */
    Optional<Account> findByHandle(Handle handle);

    List<Account> findAll();

    void deleteAll();
}
