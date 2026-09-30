package io.github.doolee01.msp.domain;

/**
 * DM 처리 결과.
 *
 * v1에서는 "도착", "자동차단" 같은 문자열을 돌려줬어요.
 * 문자열은 오타가 나도 컴파일러가 모르고, 결과마다 붙는 설명도 여기저기 흩어져 있었어요.
 * v2에서는 enum으로 만들고, 결과마다 필요한 정보(라벨, 전달 여부, 차단 여부, 설명)를 함께 담았어요.
 */
public enum DmResult {

    DELIVERED("도착", true, false,
            "모든 검사를 통과해서 정상적으로 전달됐어요."),
    INVALID_HOUR("시간 오류", false, false,
            "0~23시가 아닌 시간은 존재하지 않아서 보낼 수 없어요."),
    EMPTY_MESSAGE("빈 메시지", false, false,
            "내용이 없는 메시지는 보낼 수 없어요. 공백만 있어도 비어 있는 것으로 봐요."),
    TOO_LONG("너무 긴 메시지", false, false,
            "메시지는 " + DirectMessage.MAX_LENGTH + "자까지만 보낼 수 있어요."),
    SENDER_BLOCKED("차단 상태", false, false,
            "보낸 계정이 이미 받는 사람의 차단 목록에 있어서 전달되지 않았어요."),
    AUTO_BLOCKED("자동 차단", false, true,
            "새벽 1~5시에 '자니'가 들어간 메시지라서, 전달하지 않고 보낸 계정을 바로 차단했어요.");

    private final String label;
    private final boolean delivered;
    private final boolean blocksSender;
    private final String explanation;

    DmResult(String label, boolean delivered, boolean blocksSender, String explanation) {
        this.label = label;
        this.delivered = delivered;
        this.blocksSender = blocksSender;
        this.explanation = explanation;
    }

    public String getLabel() {
        return label;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public boolean blocksSender() {
        return blocksSender;
    }

    public String getExplanation() {
        return explanation;
    }
}
