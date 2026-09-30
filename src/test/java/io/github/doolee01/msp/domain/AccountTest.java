package io.github.doolee01.msp.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.doolee01.msp.domain.rule.DmRules;

class AccountTest {

    private Account me;
    private Handle ex;

    @BeforeEach
    void setUp() {
        me = new Account(new Handle("new_me_2026"), "나", "me", AccountType.MAIN);
        ex = new Handle("ex_boy_99");
    }

    private DmOutcome send(String content, int hour) {
        return me.receive(new DirectMessage(ex, me.getHandle(), content, hour));
    }

    @Test
    @DisplayName("저녁의 평범한 DM은 도착한다")
    void deliversNormalMessage() {
        DmOutcome outcome = send("잘 지내?", 21);
        assertEquals(DmResult.DELIVERED, outcome.getResult());
        assertEquals(5, outcome.getChecks().size());              // 규칙 5개를 모두 통과
    }

    @Test
    @DisplayName("새벽 3시 '자니?'는 자동 차단된다")
    void autoBlocksDawnJani() {
        DmOutcome outcome = send("자니?", 3);
        assertEquals(DmResult.AUTO_BLOCKED, outcome.getResult());
        assertTrue(me.hasBlocked(ex));
    }

    @Test
    @DisplayName("오후 2시 '자니?'는 새벽이 아니라서 도착한다")
    void janiInAfternoonIsDelivered() {
        assertEquals(DmResult.DELIVERED, send("자니?", 14).getResult());
    }

    @Test
    @DisplayName("차단된 사람은 새벽 '자니'를 보내도 '차단 상태'에서 먼저 걸린다 (규칙 순서)")
    void blockedSenderStopsEarly() {
        me.block(ex);
        DmOutcome outcome = send("자니?", 3);
        assertEquals(DmResult.SENDER_BLOCKED, outcome.getResult());
        assertEquals(4, outcome.getChecks().size());              // 5번째 규칙은 검사하지 않음
    }

    @Test
    @DisplayName("시간·빈 메시지·길이 검증")
    void validations() {
        assertEquals(DmResult.INVALID_HOUR, send("hi", 25).getResult());
        assertEquals(DmResult.EMPTY_MESSAGE, send("   ", 12).getResult());
        assertEquals(DmResult.TOO_LONG, send("a".repeat(501), 12).getResult());
    }

    @Test
    @DisplayName("나 자신·중복 차단은 거절, 한도가 차면 더 못 한다")
    void blockRules() {
        Account small = new Account(new Handle("tiny"), "작은계정", "t", AccountType.MAIN,
                new BlockList(2), DmRules.defaults());
        assertEquals(BlockResult.SELF, small.block(new Handle("tiny")));
        assertEquals(BlockResult.BLOCKED, small.block(new Handle("a1")));
        assertEquals(BlockResult.ALREADY_BLOCKED, small.block(new Handle("A1")));
        assertEquals(BlockResult.BLOCKED, small.block(new Handle("a2")));
        assertEquals(BlockResult.LIST_FULL, small.block(new Handle("a3")));
    }

    @Test
    @DisplayName("밖으로 내준 차단 목록은 수정할 수 없다 (v1 배열 getter 문제 해결)")
    void blockedListIsReadOnly() {
        me.block(ex);
        List<Handle> copy = me.getBlockedHandles();
        assertThrows(UnsupportedOperationException.class, () -> copy.clear());
        assertTrue(me.hasBlocked(ex));
    }

    @Test
    @DisplayName("부계는 주인 본인에게만 보인다")
    void subAccountVisibility() {
        Account mySub = new Account(new Handle("real_diary_only"), "비밀", "me", AccountType.SUB);
        Account exAccount = new Account(ex, "전애인", "ex", AccountType.MAIN);
        assertTrue(mySub.canBeSeenBy(me));
        assertFalse(mySub.canBeSeenBy(exAccount));
    }
}
