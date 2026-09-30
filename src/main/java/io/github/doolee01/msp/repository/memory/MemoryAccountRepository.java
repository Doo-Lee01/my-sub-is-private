package io.github.doolee01.msp.repository.memory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.github.doolee01.msp.domain.Account;
import io.github.doolee01.msp.domain.Handle;
import io.github.doolee01.msp.repository.AccountRepository;
import io.github.doolee01.msp.repository.DuplicateKeyException;

/**
 * 메모리(HashMap)에 계정을 저장하는 구현체.
 * DB 없이도 바로 실행해볼 수 있고, 테스트가 빨라요. 대신 프로그램을 끄면 전부 사라져요.
 *
 * synchronized: 웹 서버는 요청을 여러 스레드가 동시에 처리해서,
 * 두 요청이 동시에 Map을 고치면 데이터가 꼬일 수 있어요. 한 번에 하나씩만 들어오게 막아요.
 */
public class MemoryAccountRepository implements AccountRepository {

    private final Map<Handle, Account> store = new LinkedHashMap<>();
    private long nextId = 1;

    @Override
    public synchronized Account save(Account account) {
        if (account.getId() == null) {
            if (store.containsKey(account.getHandle())) {
                // DB의 unique 제약과 똑같이 동작하게 맞춰요. 저장소를 바꿔도 결과가 같아야 하니까요
                throw new DuplicateKeyException("이미 저장된 아이디예요: " + account.getHandle());
            }
            account.assignId(nextId++);
        }
        store.put(account.getHandle(), account);
        return account;
    }

    @Override
    public synchronized Optional<Account> findByHandle(Handle handle) {
        return Optional.ofNullable(store.get(handle));
    }

    @Override
    public synchronized List<Account> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public synchronized void deleteAll() {
        store.clear();
        nextId = 1;
    }
}
