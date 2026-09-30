package io.github.doolee01.msp.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WebAppTest {

    @Test
    @DisplayName("PORT가 없으면 8080, 있으면 그 숫자를 쓴다")
    void parsesPort() {
        assertEquals(8080, WebApp.parsePort(null));
        assertEquals(8080, WebApp.parsePort("  "));
        assertEquals(8081, WebApp.parsePort(" 8081 "));
    }

    @Test
    @DisplayName("숫자가 아니면 NumberFormatException을 원인으로 담은 IllegalArgumentException")
    void rejectsNonNumber() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> WebApp.parsePort("팔공팔공"));
        assertInstanceOf(NumberFormatException.class, e.getCause());
    }

    @Test
    @DisplayName("1~65535 범위를 벗어나면 거절한다")
    void rejectsOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> WebApp.parsePort("0"));
        assertThrows(IllegalArgumentException.class, () -> WebApp.parsePort("70000"));
    }
}
