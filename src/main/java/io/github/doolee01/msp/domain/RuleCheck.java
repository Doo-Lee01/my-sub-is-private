package io.github.doolee01.msp.domain;

/** 규칙 하나를 검사한 기록 (예: "보낸 시각이 0~23시 사이인가" → 통과) */
public class RuleCheck {

    private final String description;
    private final boolean passed;

    public RuleCheck(String description, boolean passed) {
        this.description = description;
        this.passed = passed;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPassed() {
        return passed;
    }
}
