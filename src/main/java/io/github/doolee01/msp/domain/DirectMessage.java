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

    /**
     * 글자 수. String.length()가 아니라 codePointCount를 써요.
     * length()는 이모지 하나(💅)를 2로 세지만, DB(varchar)는 1글자로 세요.
     * 둘이 다르게 세면 "자바는 통과, DB는 거절" 같은 어긋남이 생겨요.
     */
    public int length() {
        return content.codePointCount(0, content.length());
    }

    /**
     * 기록용 복사본. 너무 긴 메시지(TOO_LONG)도 "거절된 시도"로 남겨야 하는데,
     * DB 칸(varchar 500)보다 길면 저장 자체가 실패해요. 그래서 앞 500자만 남긴 복사본을 만들어요.
     * 원본은 바꾸지 않아요 (필드가 전부 final).
     */
    public DirectMessage truncatedForStorage() {
        if (length() <= MAX_LENGTH) {
            return this;
        }
        int end = content.offsetByCodePoints(0, MAX_LENGTH);   // 이모지를 반으로 자르지 않게 글자 단위로 계산
        return new DirectMessage(sender, receiver, content.substring(0, end), hour);
    }

    public boolean isBlank() {
        return content.trim().isEmpty();
    }
}
