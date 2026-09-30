package io.github.doolee01.msp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.doolee01.msp.domain.BlockResult;
import io.github.doolee01.msp.domain.DmResult;
import io.github.doolee01.msp.repository.memory.MemoryAccountRepository;
import io.github.doolee01.msp.repository.memory.MemoryMessageRepository;

/** 메모리 저장소를 끼워서 서비스 흐름 전체를 검사해요. DB 없이도 돌아가요 */
class InstaServiceTest {

    private InstaService service;

    @BeforeEach
    void setUp() {
        service = new InstaService(new MemoryAccountRepository(), new MemoryMessageRepository());
        DemoData.seed(service);
    }

    @Test
    @DisplayName("도착한 DM만 받은 편지함에 쌓이고, 거절된 시도는 기록에만 남는다")
    void inboxAndAttempts() {
        service.sendDm(DemoData.EX, DemoData.ME, "잘 지내?", 21);
        service.sendDm(DemoData.EX, DemoData.ME, "자니?", 3);
        assertEquals(1, service.getInbox(DemoData.ME).size());
        assertEquals(2, service.getAttempts(DemoData.ME).size());
        assertEquals(DmResult.AUTO_BLOCKED, service.getAttempts(DemoData.ME).get(0).getResult());
    }

    @Test
    @DisplayName("본계가 차단돼도 부계로는 연락이 닿는다 → 부계도 새벽에 '자니'면 차단")
    void subAccountStory() {
        service.sendDm(DemoData.EX, DemoData.ME, "자니?", 3);
        assertEquals(DmResult.SENDER_BLOCKED, service.sendDm(DemoData.EX, DemoData.ME, "왜", 14).getResult());
        assertEquals(DmResult.DELIVERED, service.sendDm(DemoData.EX_SUB, DemoData.ME, "나야", 14).getResult());
        assertEquals(DmResult.AUTO_BLOCKED, service.sendDm(DemoData.EX_SUB, DemoData.ME, "자니", 4).getResult());
        assertEquals(2, service.getAccount(DemoData.ME).getBlockedCount());
    }

    @Test
    @DisplayName("차단당하면 프로필이 안 보이고, 내 부계는 원래 안 보인다")
    void profiles() {
        assertTrue(service.visitProfile(DemoData.EX, DemoData.ME).isVisible());
        assertFalse(service.visitProfile(DemoData.EX, DemoData.ME_SUB).isVisible());
        assertTrue(service.visitProfile(DemoData.ME, DemoData.ME_SUB).isVisible());
        service.block(DemoData.ME, DemoData.EX);
        assertFalse(service.visitProfile(DemoData.EX, DemoData.ME).isVisible());
    }

    @Test
    @DisplayName("내 부계는 '내 계정'이라 차단할 수 없다")
    void cannotBlockOwnSub() {
        assertEquals(BlockResult.SELF, service.block(DemoData.ME, DemoData.ME_SUB));
    }

    @Test
    @DisplayName("없는 계정, 중복 아이디는 예외")
    void errors() {
        assertThrows(AccountNotFoundException.class, () -> service.sendDm("nobody", DemoData.ME, "hi", 12));
        assertThrows(DuplicateHandleException.class,
                () -> service.createAccount(DemoData.ME, "가짜", "fake",
                        io.github.doolee01.msp.domain.AccountType.MAIN));
    }

    @Test
    @DisplayName("차단을 풀면 다시 DM이 도착한다")
    void unblock() {
        service.block(DemoData.ME, DemoData.EX);
        assertTrue(service.unblock(DemoData.ME, DemoData.EX));
        assertEquals(DmResult.DELIVERED, service.sendDm(DemoData.EX, DemoData.ME, "고마워", 15).getResult());
    }
}
