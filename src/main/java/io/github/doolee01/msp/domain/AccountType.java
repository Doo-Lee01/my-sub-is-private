package io.github.doolee01.msp.domain;

/**
 * 계정 종류 (enum = 정해진 값 몇 개 중 하나만 고를 수 있는 타입)
 *
 * v1에서는 본계/부계를 필드 이름(mainId, subId)으로 구분했어요.
 * v2에서는 계정 하나하나를 Account 객체로 만들고, 종류를 enum으로 표시해요.
 * String으로 "본계"라고 쓰면 오타("본게")를 컴파일러가 못 잡지만, enum은 잡아줘요.
 */
public enum AccountType {

    MAIN("본계"),
    SUB("부계");

    private final String label;

    AccountType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
