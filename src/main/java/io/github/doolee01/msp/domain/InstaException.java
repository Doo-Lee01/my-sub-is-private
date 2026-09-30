package io.github.doolee01.msp.domain;

/**
 * 이 프로그램에서 "규칙 위반"으로 생기는 모든 예외의 부모 클래스예요. (상속)
 *
 * v1에서는 잘못된 값이 들어오면 System.out.println으로 경고만 찍고 넘어갔어요.
 * 그러면 부른 쪽은 실패했는지 알 수가 없어요.
 * v2에서는 예외를 던져서, 부른 쪽(콘솔 화면, 웹 API)이 알아서 처리하게 해요.
 *
 * RuntimeException을 상속했기 때문에 try-catch를 강제하지 않아요.
 */
public class InstaException extends RuntimeException {

    public InstaException(String message) {
        super(message);
    }

    /**
     * 다른 예외 때문에 생긴 예외일 때 원인(cause)을 함께 넘겨요. (예외 체이닝)
     * 원인을 버리지 않아야 나중에 로그에서 "왜" 실패했는지 끝까지 따라갈 수 있어요.
     */
    public InstaException(String message, Throwable cause) {
        super(message, cause);
    }
}
