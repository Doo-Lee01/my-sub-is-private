package io.github.doolee01.msp.repository;

/**
 * 저장소에 같은 아이디가 이미 있을 때 던지는 예외.
 *
 * 메모리 저장소든 JDBC 저장소든 똑같이 이 예외를 던져요.
 * 그래서 서비스는 "어떤 저장소인지" 몰라도 중복을 똑같이 처리할 수 있어요.
 * (JDBC의 SQLException을 그대로 올려 보내면 서비스가 DB 사정을 알아야 하거든요)
 */
public class DuplicateKeyException extends RuntimeException {

    public DuplicateKeyException(String message) {
        super(message);
    }

    public DuplicateKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}
