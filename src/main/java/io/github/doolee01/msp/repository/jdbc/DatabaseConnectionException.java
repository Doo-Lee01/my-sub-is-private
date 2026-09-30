package io.github.doolee01.msp.repository.jdbc;

/**
 * DB에 연결할 수 없을 때 던지는 예외. 이 프로젝트의 유일한 "checked 예외"예요.
 *
 * checked 예외(Exception을 상속)로 만든 이유
 *  - DB 연결 실패는 코드가 잘못된 게 아니라, 설정이 틀렸거나 네트워크가 끊기는 등 "바깥 사정" 때문에 생겨요.
 *  - 이런 실패는 부르는 쪽이 반드시 대비해야 해요. 그래서 컴파일러가 try-catch 또는 throws를 강제하게 했어요.
 *  - 반대로 InstaException 같은 규칙 위반은 unchecked(RuntimeException)라서 강제하지 않아요.
 *
 * 메시지(무엇이 잘못됐는지)와 별도로 hint(어떻게 고치는지)를 들고 다녀요.
 */
public class DatabaseConnectionException extends Exception {

    private final String hint;

    public DatabaseConnectionException(String message, String hint) {
        super(message);
        this.hint = hint;
    }

    public DatabaseConnectionException(String message, String hint, Throwable cause) {
        super(message, cause);
        this.hint = hint;
    }

    public String getHint() {
        return hint;
    }
}
