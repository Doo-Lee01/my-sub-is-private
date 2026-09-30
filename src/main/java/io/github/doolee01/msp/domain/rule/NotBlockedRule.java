package io.github.doolee01.msp.domain.rule;

import io.github.doolee01.msp.domain.Account;
import io.github.doolee01.msp.domain.DirectMessage;
import io.github.doolee01.msp.domain.DmResult;

/** 규칙 4: 이미 차단한 계정의 메시지는 받지 않아요 */
public class NotBlockedRule implements DmRule {

    @Override
    public String description() {
        return "보낸 계정이 차단 목록에 없는가";
    }

    @Override
    public boolean isViolatedBy(Account receiver, DirectMessage dm) {
        return receiver.hasBlocked(dm.getSender());
    }

    @Override
    public DmResult resultWhenViolated() {
        return DmResult.SENDER_BLOCKED;
    }
}
