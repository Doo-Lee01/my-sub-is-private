package io.github.doolee01.msp.domain;

/** 차단을 시도한 결과 */
public enum BlockResult {

    BLOCKED("차단했어요"),
    ALREADY_BLOCKED("이미 차단한 계정이에요"),
    SELF("내 계정은 차단할 수 없어요"),
    LIST_FULL("차단 목록이 꽉 찼어요");

    private final String message;

    BlockResult(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return this == BLOCKED;
    }
}
