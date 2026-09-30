package io.github.doolee01.msp.repository.memory;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.doolee01.msp.domain.Account;
import io.github.doolee01.msp.domain.AccountType;
import io.github.doolee01.msp.domain.Handle;
import io.github.doolee01.msp.repository.DuplicateKeyException;

class MemoryAccountRepositoryTest {

    @Test
    @DisplayName("같은 아이디의 새 계정을 두 번 저장하면 DB처럼 DuplicateKeyException")
    void rejectsDuplicateHandle() {
        MemoryAccountRepository repo = new MemoryAccountRepository();
        repo.save(new Account(new Handle("new_me_2026"), "나", "me", AccountType.MAIN));
        assertThrows(DuplicateKeyException.class,
                () -> repo.save(new Account(new Handle("new_me_2026"), "가짜", "fake", AccountType.MAIN)));
    }
}
