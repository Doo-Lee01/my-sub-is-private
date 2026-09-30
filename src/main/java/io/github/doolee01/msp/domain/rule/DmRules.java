package io.github.doolee01.msp.domain.rule;

import java.util.List;

/**
 * 기본 규칙 목록. 순서가 중요해요! 위에서부터 차례로 검사하고, 처음 걸린 규칙에서 멈춰요.
 * (차단된 사람이 새벽에 "자니"를 보내면 "자동 차단"이 아니라 "차단 상태"로 끝나요)
 */
public final class DmRules {

    private DmRules() {
        // 객체를 만들 필요가 없는 도우미 클래스라서 생성자를 막아뒀어요
    }

    public static List<DmRule> defaults() {
        return List.of(
                new ValidHourRule(),
                new NotEmptyRule(),
                new MaxLengthRule(),
                new NotBlockedRule(),
                new DawnJaniRule()
        );
    }
}
