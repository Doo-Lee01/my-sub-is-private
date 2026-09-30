package io.github.doolee01.msp.domain.rule;

import io.github.doolee01.msp.domain.Account;
import io.github.doolee01.msp.domain.DirectMessage;
import io.github.doolee01.msp.domain.DmResult;

/**
 * DM 검사 규칙 "하나"를 나타내는 인터페이스예요. (인터페이스 + 다형성)
 *
 * v1에서는 receiveDM() 안에 if문이 줄줄이 있었어요.
 * 규칙을 하나 추가하려면 receiveDM()을 직접 고쳐야 했죠.
 *
 * v2에서는 규칙마다 클래스를 하나씩 만들고, 모두 이 인터페이스를 구현해요.
 * Account는 "DmRule 목록"을 순서대로 돌면서 검사만 해요.
 * → 새 규칙(예: 욕설 필터)을 추가해도 Account 코드는 한 줄도 안 바뀌어요.
 */
public interface DmRule {

    /** 화면에 보여줄 검사 항목 설명 (예: "보낸 시각이 0~23시 사이인가") */
    String description();

    /** 이 규칙을 어겼으면 true */
    boolean isViolatedBy(Account receiver, DirectMessage dm);

    /** 어겼을 때의 결과 */
    DmResult resultWhenViolated();
}
