package io.github.doolee01.msp.service;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.doolee01.msp.domain.Account;
import io.github.doolee01.msp.domain.AccountType;
import io.github.doolee01.msp.domain.Handle;
import io.github.doolee01.msp.repository.AccountRepository;
import io.github.doolee01.msp.repository.DuplicateKeyException;
import io.github.doolee01.msp.repository.memory.MemoryMessageRepository;

/**
 * "확인할 땐 없었는데 저장하는 순간 누가 먼저 가입한" 상황을 흉내 내요.
 * 인터페이스 덕분에 이런 가짜 저장소를 테스트에서 쉽게 끼울 수 있어요.
 */
class DuplicateTranslationTest {

    /** 찾으면 항상 없다고 하고, 저장하면 항상 중복이라고 하는 가짜 저장소 */
    private static class RacingRepository implements AccountRepository {
        @Override
        public Account save(Account account) {
            throw new DuplicateKeyException("unique 제약 위반 (흉내)");
        }

        @Override
        public Optional<Account> findByHandle(Handle handle) {
            return Optional.empty();
        }

        @Override
        public List<Account> findAll() {
            return List.of();
        }

        @Override
        public void deleteAll() {
        }
    }

    @Test
    @DisplayName("저장소의 DuplicateKeyException을 서비스가 DuplicateHandleException으로 번역하고 원인을 남긴다")
    void translatesRepositoryException() {
        InstaService service = new InstaService(new RacingRepository(), new MemoryMessageRepository());
        DuplicateHandleException e = assertThrows(DuplicateHandleException.class,
                () -> service.createAccount("new_me_2026", "나", "me", AccountType.MAIN));
        assertInstanceOf(DuplicateKeyException.class, e.getCause());
    }
}
