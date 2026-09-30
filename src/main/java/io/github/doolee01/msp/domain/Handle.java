package io.github.doolee01.msp.domain;

/**
 * 인스타 아이디(@handle)를 나타내는 "값 객체"예요.
 *
 * v1에서는 아이디를 그냥 String으로 다뤄서, 검증을 깜빡하면 이상한 아이디가 그대로 들어갔어요.
 * v2에서는 Handle 객체를 만들 때 생성자에서 딱 한 번 검증하고,
 * 한 번 만들어진 Handle은 절대 바뀌지 않게(final) 했어요.
 * → "Handle 타입이면 무조건 올바른 아이디"라고 믿고 쓸 수 있어요.
 *
 * 규칙 (실제 인스타 규칙을 단순화)
 *  - 1~30자
 *  - 영문 소문자, 숫자, 마침표(.), 밑줄(_)만 가능 (대문자는 소문자로 바꿔 저장)
 *  - 마침표로 시작하거나 끝날 수 없고, 마침표 두 개를 연속으로 쓸 수 없음
 */
public final class Handle {

    public static final int MAX_LENGTH = 30;

    private final String value;

    public Handle(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new InvalidHandleException("아이디가 비어 있어요");
        }
        String candidate = raw.trim().toLowerCase();
        if (candidate.startsWith("@")) {
            candidate = candidate.substring(1);   // "@ex_boy_99" 처럼 @를 붙여 입력해도 괜찮아요
        }
        validate(candidate);
        this.value = candidate;
    }

    private static void validate(String candidate) {
        if (candidate.isEmpty()) {
            throw new InvalidHandleException("아이디가 비어 있어요");
        }
        if (candidate.length() > MAX_LENGTH) {
            throw new InvalidHandleException("아이디는 " + MAX_LENGTH + "자까지만 돼요: " + candidate);
        }
        for (int i = 0; i < candidate.length(); i++) {
            char c = candidate.charAt(i);
            boolean allowed = (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '.' || c == '_';
            if (!allowed) {
                throw new InvalidHandleException("아이디에는 영문, 숫자, 마침표(.), 밑줄(_)만 쓸 수 있어요: " + candidate);
            }
        }
        if (candidate.startsWith(".") || candidate.endsWith(".")) {
            throw new InvalidHandleException("아이디는 마침표로 시작하거나 끝날 수 없어요: " + candidate);
        }
        if (candidate.contains("..")) {
            throw new InvalidHandleException("마침표를 연속으로 쓸 수 없어요: " + candidate);
        }
    }

    public String getValue() {
        return value;
    }

    /** 화면에 보여줄 때는 @를 붙여요 */
    public String display() {
        return "@" + value;
    }

    /*
     * equals와 hashCode를 직접 정의한 이유
     * List.contains(), Map.get() 같은 메서드는 내부에서 equals()로 "같은지"를 판단해요.
     * 정의하지 않으면 "같은 글자의 아이디"라도 서로 다른 객체면 다르다고 판단해요.
     * → 차단 목록 검사가 제대로 동작하려면 꼭 필요해요.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Handle)) {
            return false;
        }
        return value.equals(((Handle) other).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return display();
    }
}
