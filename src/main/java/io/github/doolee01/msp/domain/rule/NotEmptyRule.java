package io.github.doolee01.msp.domain.rule;

import io.github.doolee01.msp.domain.Account;
import io.github.doolee01.msp.domain.DirectMessage;
import io.github.doolee01.msp.domain.DmResult;

/** 규칙 2: 빈 메시지(공백만 있는 것 포함)는 보낼 수 없어요 */
public class NotEmptyRule implements DmRule {

    @Override
    public String description() {
        return "메시지에 내용이 있는가";
    }

    @Override
    public boolean isViolatedBy(Account receiver, DirectMessage dm) {
        return dm.isBlank();
    }

    @Override
    public DmResult resultWhenViolated() {
        return DmResult.EMPTY_MESSAGE;
    }
}
