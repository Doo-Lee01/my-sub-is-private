package io.github.doolee01.msp.repository.jdbc;

/**
 * DB 작업 중 생긴 문제를 감싸는 예외.
 * SQLException은 "반드시 try-catch 해야 하는" 예외라서, 그대로 두면 서비스 코드까지 전부 throws를 달아야 해요.
 * 저장소 안에서 RuntimeException으로 한 번 감싸서, 바깥은 DB 사정을 몰라도 되게 했어요.
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
