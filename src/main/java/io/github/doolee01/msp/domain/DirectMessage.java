package io.github.doolee01.msp.domain;

/**
 * DM 한 통을 나타내요.
 *
 * v1에서는 receiveDM(sender, message, hour)처럼 값을 따로따로 넘겼어요.
 * v2에서는 "DM"이라는 개념 자체를 객체로 만들었어요.
 * → 저장(DB), 검사(규칙), 화면 표시에서 똑같은 객체를 주고받을 수 있어요.
 *
 * 시각(hour)이 0~23인지, 내용이 비었는지는 여기서 막지 않고 규칙(DmRule)이 검사해요.
 * 잘못된 DM도 "왜 거절됐는지" 설명하고 기록으로 남기기 위해서예요.
 */
public class DirectMessage {

    public static final int MAX_LENGTH = 500;

    private final Handle sender;
    private final Handle receiver;
    private final String content;
    private final int hour;

    public DirectMessage(Handle sender, Handle receiver, String content, int hour) {
        if (sender == null || receiver == null) {
            throw new IllegalArgumentException("보내는 사람과 받는 사람은 꼭 있어야 해요");
        }
        this.sender = sender;
        this.receiver = receiver;
        this.content = (content == null) ? "" : content;
        this.hour = hour;
    }

    public Handle getSender() {
        return sender;
    }

    public Handle getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }

    public int getHour() {
        return hour;
    }

    public boolean isBlank() {
        return content.trim().isEmpty();
    }
}
