package io.github.doolee01.msp.domain.rule;

import io.github.doolee01.msp.domain.Account;
import io.github.doolee01.msp.domain.DirectMessage;
import io.github.doolee01.msp.domain.DmResult;

/** 규칙 3: 메시지 길이 제한 (DB 컬럼 크기와 맞춰요) */
public class MaxLengthRule implements DmRule {

    @Override
    public String description() {
        return "메시지가 " + DirectMessage.MAX_LENGTH + "자 이하인가";
    }

    @Override
    public boolean isViolatedBy(Account receiver, DirectMessage dm) {
        return dm.length() > DirectMessage.MAX_LENGTH;
    }

    @Override
    public DmResult resultWhenViolated() {
        return DmResult.TOO_LONG;
    }
}
