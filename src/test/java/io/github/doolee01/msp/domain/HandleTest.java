package io.github.doolee01.msp.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 테스트 코드 = "자동 리허설"
 * 이클립스에서 이 파일 우클릭 → Run As → JUnit Test 하면 초록 막대가 떠야 해요.
 * 코드를 고친 뒤에도 한 번에 돌려서, 예전 기능이 망가지지 않았는지 확인할 수 있어요.
 */
class HandleTest {

    @Test
    @DisplayName("대문자와 @, 앞뒤 공백은 정리해서 저장한다")
    void normalizes() {
        assertEquals("ex_boy_99", new Handle("  @Ex_Boy_99 ").getValue());
    }

    @Test
    @DisplayName("글자가 같으면 서로 다른 객체여도 같은 아이디다 (equals)")
    void equality() {
        assertEquals(new Handle("new_me_2026"), new Handle("NEW_ME_2026"));
    }

    @Test
    @DisplayName("규칙에 맞지 않는 아이디는 예외를 던진다")
    void rejectsInvalid() {
        assertThrows(InvalidHandleException.class, () -> new Handle(""));
        assertThrows(InvalidHandleException.class, () -> new Handle("hello world"));
        assertThrows(InvalidHandleException.class, () -> new Handle("자니"));
        assertThrows(InvalidHandleException.class, () -> new Handle(".dot"));
        assertThrows(InvalidHandleException.class, () -> new Handle("a..b"));
        assertThrows(InvalidHandleException.class, () -> new Handle("a".repeat(31)));
    }
}
