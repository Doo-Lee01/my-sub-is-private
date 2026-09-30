package io.github.doolee01.msp.domain;

/** 아이디 규칙에 맞지 않을 때 던지는 예외 */
public class InvalidHandleException extends InstaException {

    public InvalidHandleException(String message) {
        super(message);
    }
}
